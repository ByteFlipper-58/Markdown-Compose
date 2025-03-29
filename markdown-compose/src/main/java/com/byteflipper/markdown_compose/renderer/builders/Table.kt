package com.byteflipper.markdown_compose.renderer.builders

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures // Import for tap detection
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput // Import for modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.* // Import AnnotatedString, etc.
import androidx.compose.ui.text.font.FontWeight // Import FontWeight for header style
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.byteflipper.markdown_compose.model.*
import com.byteflipper.markdown_compose.renderer.MarkdownRenderer

private const val TAG = "TableRendererCanvas"

object Table {

    // Store information about cell bounds and layout for click detection
    private data class CellLayoutInfo(
        val bounds: Rect, // Scaled bounds relative to Canvas top-left
        val textLayoutResult: TextLayoutResult,
        val textTopLeft: Offset // Scaled top-left of the drawn text within the bounds
    )

    /**
     * Renders a Markdown table using a custom Layout and Canvas, applying styles from the StyleSheet.
     * Supports customizable cell padding, border thickness, color, and outer border rounding.
     * Includes click handling for links within cells using manual hit testing.
     *
     * @param tableNode The TableNode containing the table data.
     * @param styleSheet The stylesheet defining table appearance.
     * @param modifier Modifier for layout adjustments.
     * @param linkHandler Callback for when a link inside a table cell is clicked.
     */
    @Composable
    fun RenderTable(
        tableNode: TableNode,
        styleSheet: MarkdownStyleSheet,
        modifier: Modifier = Modifier,
        linkHandler: (url: String) -> Unit // Add link handler parameter
    ) {
        Log.d(TAG, "Start rendering table with style: ${styleSheet.tableStyle}")

        val textMeasurer: TextMeasurer = rememberTextMeasurer()
        val density = LocalDensity.current
        val tableStyle = styleSheet.tableStyle

        // Convert Dp styles to Px
        val cellPaddingPx = with(density) { tableStyle.cellPadding.toPx() }
        val borderThicknessPx = with(density) { tableStyle.borderThickness.toPx().coerceAtLeast(0.1f) }

        // Pre-measure cells and calculate dimensions
        val (rowHeightsPx, columnWidthsPx, cellRenderData) = remember(tableNode, styleSheet, textMeasurer, cellPaddingPx, density) {
            measureTableContent(tableNode, styleSheet, textMeasurer, cellPaddingPx)
        }

        // Add padding to dimensions
        val paddedRowHeightsPx = rowHeightsPx.map { it + (2 * cellPaddingPx) }
        val paddedColumnWidthsPx = columnWidthsPx.map { it + (2 * cellPaddingPx) }
        val totalUnscaledPaddedWidth = paddedColumnWidthsPx.sum()
        val totalUnscaledPaddedHeight = paddedRowHeightsPx.sum()

        // Mutable state to hold the calculated layout info for hit testing
        // Needs to be updated within the Canvas draw scope
        val cellLayoutInfosState = remember { mutableStateOf<List<CellLayoutInfo>>(emptyList()) }

        Layout(
            content = {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        // Add pointerInput for tap detection
                        .pointerInput(tableNode, cellLayoutInfosState.value, linkHandler) {
                            detectTapGestures(
                                onTap = { tapOffset ->
                                    handleTap(
                                        tapOffset = tapOffset,
                                        cellLayoutInfos = cellLayoutInfosState.value,
                                        linkHandler = linkHandler
                                    )
                                }
                            )
                        }
                ) {
                    // --- START: Calculations needed for BOTH drawing and click info ---
                    val layoutWidth = size.width
                    val layoutHeight = size.height
                    val scale = if (totalUnscaledPaddedWidth > 0f) layoutWidth / totalUnscaledPaddedWidth else 1f

                    val scaledPaddedColumnWidths = paddedColumnWidthsPx.map { it * scale }
                    val scaledPaddedRowHeights = paddedRowHeightsPx.map { it * scale }
                    val scaledBorderThickness = borderThicknessPx // Keep border unscaled for consistency
                    val scaledCellPadding = cellPaddingPx * scale

                    val totalGridWidth = scaledPaddedColumnWidths.sum()
                    val totalGridHeight = scaledPaddedRowHeights.sum().coerceAtMost(layoutHeight)
                    // --- END: Calculations ---


                    // --- Draw Table and Capture Layout Info ---
                    val currentCellLayoutInfos = mutableListOf<CellLayoutInfo>() // Temp list for this draw pass

                    val drawAndCapture: DrawScope.() -> Unit = {
                        drawGridAndContent(
                            tableNode = tableNode,
                            cellRenderData = cellRenderData,
                            scaledPaddedColumnWidths = scaledPaddedColumnWidths,
                            scaledPaddedRowHeights = scaledPaddedRowHeights,
                            scaledCellPadding = scaledCellPadding,
                            borderThickness = scaledBorderThickness,
                            borderColor = tableStyle.borderColor,
                            columnAlignments = tableNode.columnAlignments,
                            // Pass the list to capture info
                            captureLayoutInfoList = currentCellLayoutInfos
                        )
                    }

                    // --- Drawing Logic (clipping or direct draw) ---
                    val outlineShape = tableStyle.outerBorderShape
                    val outlinePath = if (outlineShape != null) {
                        Path().apply { addOutline(outlineShape.createOutline(Size(totalGridWidth, totalGridHeight), layoutDirection, density)) }
                    } else null

                    if (outlinePath != null) {
                        // Draw clipped
                        clipPath(outlinePath, clipOp = ClipOp.Intersect) {
                            drawAndCapture() // Execute drawing and info capture
                        }
                        // Draw outer border after clipping
                        if (scaledBorderThickness > 0) {
                            drawPath(
                                path = outlinePath,
                                color = tableStyle.borderColor,
                                style = Stroke(width = scaledBorderThickness)
                            )
                        }
                    } else {
                        // Draw unclipped
                        drawAndCapture() // Execute drawing and info capture
                        // Draw outer border if no shape
                        if (scaledBorderThickness > 0) {
                            drawRect(
                                color = tableStyle.borderColor,
                                topLeft = Offset.Zero,
                                size = Size(totalGridWidth, totalGridHeight),
                                style = Stroke(width = scaledBorderThickness)
                            )
                        }
                    }

                    // Update the state AFTER drawing is complete for this frame
                    // This ensures the hit testing uses the layout info from the last successful draw
                    if (cellLayoutInfosState.value != currentCellLayoutInfos) { // Avoid redundant updates
                        cellLayoutInfosState.value = currentCellLayoutInfos.toList() // Assign immutable list
                        Log.d(TAG, "Updated CellLayoutInfo state with ${currentCellLayoutInfos.size} cells")
                    }
                }
            },
            modifier = modifier,
            measurePolicy = { measurables, constraints ->
                // --- Measure Policy (Remains the same) ---
                val requiredWidth = totalUnscaledPaddedWidth.coerceAtLeast(0f)
                val finalWidth = requiredWidth.coerceIn(constraints.minWidth.toFloat(), constraints.maxWidth.toFloat())
                val scale = if (totalUnscaledPaddedWidth > 0f) finalWidth / totalUnscaledPaddedWidth else 1f
                val requiredHeight = (totalUnscaledPaddedHeight * scale).coerceAtLeast(0f)
                val finalHeight = requiredHeight.coerceIn(constraints.minHeight.toFloat(), constraints.maxHeight.toFloat())

                val placeable = measurables.first().measure(
                    Constraints.fixed(finalWidth.toInt(), finalHeight.toInt())
                )
                Log.d(TAG, "Layout - Constraints: $constraints, Required W/H: $requiredWidth/$requiredHeight, Final W/H: ${finalWidth.toInt()}/${finalHeight.toInt()}")
                layout(finalWidth.toInt(), finalHeight.toInt()) {
                    placeable.placeRelative(0, 0)
                }
            }
        )
    }

    // Renamed MeasuredCellData for clarity
    private data class CellRenderData(
        val annotatedString: AnnotatedString,
        val textLayoutResult: TextLayoutResult,
        val isHeader: Boolean
    )

    /**
     * Measures cell content. Returns Triple (row heights, col widths, map of render data).
     */
    private fun measureTableContent(
        tableNode: TableNode,
        styleSheet: MarkdownStyleSheet,
        textMeasurer: TextMeasurer,
        cellPaddingPx: Float
    ): Triple<List<Float>, List<Float>, Map<Pair<Int, Int>, CellRenderData>> {
        val cellDataMap = mutableMapOf<Pair<Int, Int>, CellRenderData>()
        val rowHeightsPx = mutableListOf<Float>()
        val actualColumnCount = tableNode.rows.maxOfOrNull { it.cells.size } ?: tableNode.columnAlignments.size
        val columnWidthsPx = MutableList(actualColumnCount) { 0f }

        tableNode.rows.forEachIndexed { rowIndex, row ->
            var maxRowHeight = 0f
            val isHeaderRow = row.isHeader

            // Determine text style based on header status
            val baseTextStyle = styleSheet.textStyle
            val cellTextStyle = if (isHeaderRow) {
                baseTextStyle.copy(fontWeight = FontWeight.Bold)
            } else {
                baseTextStyle
            }
            // Apply style to children rendering for consistent measuring
            val rowStyleSheet = styleSheet.copy(textStyle = cellTextStyle)

            for (colIndex in 0 until actualColumnCount) {
                val cellNode = row.cells.getOrNull(colIndex) // Handle rows with fewer cells
                val cellKey = Pair(rowIndex, colIndex)

                val annotatedString = if (cellNode != null) {
                    MarkdownRenderer.render(cellNode.content, rowStyleSheet) // Use row-specific style sheet
                } else {
                    AnnotatedString("")
                }

                val textLayoutResult = textMeasurer.measure(annotatedString)

                cellDataMap[cellKey] = CellRenderData(annotatedString, textLayoutResult, isHeaderRow)

                val textHeight = textLayoutResult.size.height.toFloat()
                val textWidth = textLayoutResult.size.width.toFloat()

                if (textHeight > maxRowHeight) {
                    maxRowHeight = textHeight
                }

                if (textWidth > columnWidthsPx[colIndex]) {
                    columnWidthsPx[colIndex] = textWidth
                }
            }
            rowHeightsPx.add(maxRowHeight) // Store height *without* padding
        }

        return Triple(rowHeightsPx.toList(), columnWidthsPx.toList(), cellDataMap.toMap())
    }


    /**
     * Draws the grid and content, AND captures layout info for hit testing.
     */
    private fun DrawScope.drawGridAndContent(
        tableNode: TableNode,
        cellRenderData: Map<Pair<Int, Int>, CellRenderData>, // Use CellRenderData
        scaledPaddedColumnWidths: List<Float>,
        scaledPaddedRowHeights: List<Float>,
        scaledCellPadding: Float,
        borderThickness: Float,
        borderColor: Color,
        columnAlignments: List<ColumnAlignment>,
        // List to populate with layout info for click handling
        captureLayoutInfoList: MutableList<CellLayoutInfo>
    ) {
        captureLayoutInfoList.clear() // Clear previous info before drawing

        val totalGridHeight = scaledPaddedRowHeights.sum().coerceAtMost(size.height)
        val totalGridWidth = scaledPaddedColumnWidths.sum().coerceAtMost(size.width)

        // --- Draw Grid Lines (remains the same) ---
        var yPos = 0f
        if (borderThickness > 0) {
            scaledPaddedRowHeights.dropLast(1).forEach { rowHeight ->
                yPos += rowHeight
                val currentY = yPos.coerceAtMost(totalGridHeight)
                drawLine(color = borderColor, start = Offset(0f, currentY), end = Offset(totalGridWidth, currentY), strokeWidth = borderThickness)
            }
        }
        var xPos = 0f
        if (borderThickness > 0) {
            scaledPaddedColumnWidths.dropLast(1).forEach { colWidth ->
                xPos += colWidth
                val currentX = xPos.coerceAtMost(totalGridWidth)
                drawLine(color = borderColor, start = Offset(currentX, 0f), end = Offset(currentX, totalGridHeight), strokeWidth = borderThickness)
            }
        }

        // --- Draw Cell Contents and Capture Info ---
        var currentY = 0f
        tableNode.rows.forEachIndexed { rowIndex, row ->
            var currentX = 0f
            val scaledCurrentRowHeight = scaledPaddedRowHeights.getOrElse(rowIndex) { 0f }

            for (columnIndex in 0 until scaledPaddedColumnWidths.size) {
                val cellKey = Pair(rowIndex, columnIndex)
                val data = cellRenderData[cellKey]

                if (data != null) {
                    val scaledCellWidth = scaledPaddedColumnWidths[columnIndex]
                    val textLayoutResult = data.textLayoutResult
                    val alignment = columnAlignments.getOrElse(columnIndex) { ColumnAlignment.LEFT }

                    val textWidth = textLayoutResult.size.width.toFloat()
                    val textHeight = textLayoutResult.size.height.toFloat()

                    // Calculate text position based on alignment and padding
                    val textOffsetX = when (alignment) {
                        ColumnAlignment.LEFT -> currentX + scaledCellPadding
                        ColumnAlignment.RIGHT -> currentX + scaledCellWidth - scaledCellPadding - textWidth
                        ColumnAlignment.CENTER -> currentX + (scaledCellWidth - textWidth) / 2f
                    }
                    val textOffsetY = currentY + (scaledCurrentRowHeight - textHeight) / 2f // Center vertically

                    val textDrawOffset = Offset(
                        x = textOffsetX.coerceIn(currentX, currentX + scaledCellWidth - textWidth),
                        y = textOffsetY.coerceIn(currentY, currentY + scaledCurrentRowHeight - textHeight)
                    )

                    // Draw the text
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = textDrawOffset
                    )

                    // --- Capture Layout Info ---
                    val cellBounds = Rect(
                        left = currentX,
                        top = currentY,
                        right = currentX + scaledCellWidth,
                        bottom = currentY + scaledCurrentRowHeight
                    )
                    captureLayoutInfoList.add(
                        CellLayoutInfo(
                            bounds = cellBounds,
                            textLayoutResult = textLayoutResult,
                            textTopLeft = textDrawOffset
                        )
                    )
                    // --- End Capture ---

                } else {
                    Log.w(TAG, "No render data found for cell ($rowIndex, $columnIndex)")
                }
                currentX += scaledPaddedColumnWidths.getOrElse(columnIndex){ 0f } // Move to next column start
            } // End column loop
            currentY += scaledCurrentRowHeight // Move to next row start
        } // End row loop
        Log.v(TAG, "Captured layout info for ${captureLayoutInfoList.size} cells")
    }


    /**
     * Handles tap gestures on the Canvas for link detection.
     */
    private fun handleTap(
        tapOffset: Offset,
        cellLayoutInfos: List<CellLayoutInfo>,
        linkHandler: (url: String) -> Unit
    ) {
        Log.d(TAG, "Tap detected at: $tapOffset. Checking ${cellLayoutInfos.size} cells.")

        // Find the cell that was tapped
        val tappedCellInfo = cellLayoutInfos.find { tapOffset in it.bounds }

        if (tappedCellInfo != null) {
            Log.d(TAG, "Tap inside cell bounds: ${tappedCellInfo.bounds}")
            // Calculate tap offset relative to the text's top-left corner
            val textRelativeOffset = tapOffset - tappedCellInfo.textTopLeft

            // Check if tap is within the text layout bounds (optional but good practice)
            if (textRelativeOffset.x >= 0 && textRelativeOffset.y >= 0 &&
                textRelativeOffset.x <= tappedCellInfo.textLayoutResult.size.width &&
                textRelativeOffset.y <= tappedCellInfo.textLayoutResult.size.height)
            {
                // Get character offset for the tap position
                val characterOffset = try {
                    tappedCellInfo.textLayoutResult.getOffsetForPosition(textRelativeOffset)
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting offset for position: $textRelativeOffset", e)
                    -1 // Indicate error
                }


                if (characterOffset >= 0) {
                    Log.d(TAG, "Tap maps to character offset: $characterOffset")
                    // Check for link annotation at that offset
                    tappedCellInfo.textLayoutResult.layoutInput.text
                        .getStringAnnotations(tag = Link.URL_TAG, start = characterOffset, end = characterOffset)
                        .firstOrNull()
                        ?.let { annotation ->
                            Log.i(TAG, "Link clicked in Table (Canvas): ${annotation.item}")
                            linkHandler(annotation.item)
                            // return // Found and handled link, stop checking other cells (though technically impossible)
                        } ?: run {
                        Log.d(TAG, "No link annotation found at offset $characterOffset")
                    }
                } else {
                    Log.w(TAG, "Tap was within text bounds but getOffsetForPosition failed or returned negative.")
                }
            } else {
                Log.d(TAG, "Tap inside cell bounds, but outside text layout area. Relative offset: $textRelativeOffset")
            }
        } else {
            Log.d(TAG, "Tap outside any cell bounds.")
        }
    }

    // Helper extension for checking if offset is within Rect bounds
    private operator fun Rect.contains(offset: Offset): Boolean {
        return offset.x >= left && offset.x < right && offset.y >= top && offset.y < bottom
    }
}