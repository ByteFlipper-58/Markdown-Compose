package com.byteflipper.markdown_compose.renderer.builders

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.byteflipper.markdown_compose.model.*
import com.byteflipper.markdown_compose.renderer.FOOTNOTE_REF_TAG // Import footnote tag
import com.byteflipper.markdown_compose.renderer.MarkdownRenderer

private const val TAG = "TableRendererCanvas"

object Table {

    private data class CellLayoutInfo(
        val bounds: Rect,
        val textLayoutResult: TextLayoutResult,
        val textTopLeft: Offset
    )

    /**
     * Renders a Markdown table using Canvas, including support for footnotes within cells.
     *
     * @param tableNode The TableNode data.
     * @param styleSheet The stylesheet.
     * @param modifier Modifier for layout adjustments.
     * @param footnoteReferenceMap Map from footnote ID to its display index for correct rendering.
     * @param linkHandler Callback for external link clicks.
     * @param onFootnoteReferenceClick Callback for footnote reference clicks.
     */
    @Composable
    fun RenderTable(
        tableNode: TableNode,
        styleSheet: MarkdownStyleSheet,
        modifier: Modifier = Modifier,
        footnoteReferenceMap: Map<String, Int>?, // Receive the map
        linkHandler: (url: String) -> Unit,
        onFootnoteReferenceClick: ((identifier: String) -> Unit)? // Receive the callback
    ) {
        Log.d(TAG, "Start rendering table with style: ${styleSheet.tableStyle}")

        val textMeasurer: TextMeasurer = rememberTextMeasurer()
        val density = LocalDensity.current
        val tableStyle = styleSheet.tableStyle

        val cellPaddingPx = with(density) { tableStyle.cellPadding.toPx() }
        val borderThicknessPx = with(density) { tableStyle.borderThickness.toPx().coerceAtLeast(0.1f) }

        // Pre-measure content, passing the footnote map
        val (rowHeightsPx, columnWidthsPx, cellRenderData) = remember(tableNode, styleSheet, textMeasurer, cellPaddingPx, density, footnoteReferenceMap) {
            measureTableContent(tableNode, styleSheet, textMeasurer, cellPaddingPx, footnoteReferenceMap) // Pass the map
        }

        // Calculate dimensions with padding
        val paddedRowHeightsPx = rowHeightsPx.map { it + (2 * cellPaddingPx) }
        val paddedColumnWidthsPx = columnWidthsPx.map { it + (2 * cellPaddingPx) }
        val totalUnscaledPaddedWidth = paddedColumnWidthsPx.sum()
        val totalUnscaledPaddedHeight = paddedRowHeightsPx.sum()

        val cellLayoutInfosState = remember { mutableStateOf<List<CellLayoutInfo>>(emptyList()) }

        Layout(
            content = {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(tableNode, cellLayoutInfosState.value, linkHandler, onFootnoteReferenceClick) { // Add callback to key
                            detectTapGestures(
                                onTap = { tapOffset ->
                                    handleTap(
                                        tapOffset = tapOffset,
                                        cellLayoutInfos = cellLayoutInfosState.value,
                                        linkHandler = linkHandler,
                                        onFootnoteReferenceClick = onFootnoteReferenceClick // Pass callback
                                    )
                                }
                            )
                        }
                ) {
                    // Calculations for scaling
                    val layoutWidth = size.width
                    val layoutHeight = size.height
                    val scale = if (totalUnscaledPaddedWidth > 0f) layoutWidth / totalUnscaledPaddedWidth else 1f

                    val scaledPaddedColumnWidths = paddedColumnWidthsPx.map { it * scale }
                    val scaledPaddedRowHeights = paddedRowHeightsPx.map { it * scale }
                    val scaledBorderThickness = borderThicknessPx // Keep border unscaled
                    val scaledCellPadding = cellPaddingPx * scale

                    val totalGridWidth = scaledPaddedColumnWidths.sum()
                    val totalGridHeight = scaledPaddedRowHeights.sum().coerceAtMost(layoutHeight)

                    // --- Draw Table and Capture Layout Info ---
                    val currentCellLayoutInfos = mutableListOf<CellLayoutInfo>()

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
                            captureLayoutInfoList = currentCellLayoutInfos
                        )
                    }

                    // --- Drawing Logic with optional clipping ---
                    val outlineShape = tableStyle.outerBorderShape
                    val outlinePath = if (outlineShape != null) {
                        Path().apply { addOutline(outlineShape.createOutline(Size(totalGridWidth, totalGridHeight), layoutDirection, density)) }
                    } else null

                    if (outlinePath != null) {
                        clipPath(outlinePath, clipOp = ClipOp.Intersect) {
                            drawAndCapture()
                        }
                        if (scaledBorderThickness > 0) {
                            drawPath(path = outlinePath, color = tableStyle.borderColor, style = Stroke(width = scaledBorderThickness))
                        }
                    } else {
                        drawAndCapture()
                        if (scaledBorderThickness > 0) {
                            drawRect(color = tableStyle.borderColor, topLeft = Offset.Zero, size = Size(totalGridWidth, totalGridHeight), style = Stroke(width = scaledBorderThickness))
                        }
                    }

                    // --- Update state after drawing ---
                    if (cellLayoutInfosState.value != currentCellLayoutInfos) {
                        cellLayoutInfosState.value = currentCellLayoutInfos.toList()
                        Log.d(TAG, "Updated CellLayoutInfo state with ${currentCellLayoutInfos.size} cells")
                    }
                }
            },
            modifier = modifier,
            measurePolicy = { measurables, constraints ->
                // --- Measure Policy ---
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

    private data class CellRenderData(
        val annotatedString: AnnotatedString,
        val textLayoutResult: TextLayoutResult,
        val isHeader: Boolean
    )

    /** Measures cell content, now including footnote map. */
    private fun measureTableContent(
        tableNode: TableNode,
        styleSheet: MarkdownStyleSheet,
        textMeasurer: TextMeasurer,
        cellPaddingPx: Float,
        footnoteReferenceMap: Map<String, Int>? // Receive the map
    ): Triple<List<Float>, List<Float>, Map<Pair<Int, Int>, CellRenderData>> {
        val cellDataMap = mutableMapOf<Pair<Int, Int>, CellRenderData>()
        val rowHeightsPx = mutableListOf<Float>()
        val actualColumnCount = tableNode.rows.maxOfOrNull { it.cells.size } ?: tableNode.columnAlignments.size
        val columnWidthsPx = MutableList(actualColumnCount) { 0f }

        tableNode.rows.forEachIndexed { rowIndex, row ->
            var maxRowHeight = 0f
            val isHeaderRow = row.isHeader
            val baseTextStyle = styleSheet.textStyle
            val cellTextStyle = if (isHeaderRow) baseTextStyle.copy(fontWeight = FontWeight.Bold) else baseTextStyle
            // Create a stylesheet specific for this row's style (header or normal)
            // This ensures nodes *inside* the cell inherit the correct base style
            val rowStyleSheet = styleSheet.copy(textStyle = cellTextStyle)

            for (colIndex in 0 until actualColumnCount) {
                val cellNode = row.cells.getOrNull(colIndex)
                val cellKey = Pair(rowIndex, colIndex)

                // Render cell content WITH footnote map
                val annotatedString = if (cellNode != null) {
                    // Use the row-specific stylesheet and pass the footnote map
                    MarkdownRenderer.render(cellNode.content, rowStyleSheet, footnoteReferenceMap)
                } else {
                    AnnotatedString("")
                }

                // Measure the final AnnotatedString
                val textLayoutResult = textMeasurer.measure(annotatedString)
                cellDataMap[cellKey] = CellRenderData(annotatedString, textLayoutResult, isHeaderRow)

                // Update dimensions based on measurement
                val textHeight = textLayoutResult.size.height.toFloat()
                val textWidth = textLayoutResult.size.width.toFloat()
                maxRowHeight = maxOf(maxRowHeight, textHeight)
                columnWidthsPx[colIndex] = maxOf(columnWidthsPx[colIndex], textWidth)
            }
            rowHeightsPx.add(maxRowHeight) // Store height *without* padding
        }
        return Triple(rowHeightsPx.toList(), columnWidthsPx.toList(), cellDataMap.toMap())
    }

    /** Draws the grid lines and cell content. */
    private fun DrawScope.drawGridAndContent(
        tableNode: TableNode,
        cellRenderData: Map<Pair<Int, Int>, CellRenderData>,
        scaledPaddedColumnWidths: List<Float>,
        scaledPaddedRowHeights: List<Float>,
        scaledCellPadding: Float,
        borderThickness: Float,
        borderColor: Color,
        columnAlignments: List<ColumnAlignment>,
        captureLayoutInfoList: MutableList<CellLayoutInfo> // List to capture layout info
    ) {
        captureLayoutInfoList.clear() // Clear before populating

        val totalGridHeight = scaledPaddedRowHeights.sum().coerceAtMost(size.height)
        val totalGridWidth = scaledPaddedColumnWidths.sum().coerceAtMost(size.width)

        // --- Draw Grid Lines ---
        var yPos = 0f
        if (borderThickness > 0) {
            // Draw horizontal lines
            scaledPaddedRowHeights.dropLast(1).forEach { rowHeight ->
                yPos += rowHeight
                val currentY = yPos.coerceAtMost(totalGridHeight)
                drawLine(color = borderColor, start = Offset(0f, currentY), end = Offset(totalGridWidth, currentY), strokeWidth = borderThickness)
            }
            // Draw vertical lines
            var xPos = 0f
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
                val scaledCellWidth = scaledPaddedColumnWidths.getOrElse(columnIndex) { 0f } // Use getOrElse

                if (data != null) {
                    val textLayoutResult = data.textLayoutResult
                    val alignment = columnAlignments.getOrElse(columnIndex) { ColumnAlignment.LEFT }
                    val textWidth = textLayoutResult.size.width.toFloat()
                    val textHeight = textLayoutResult.size.height.toFloat()

                    // Calculate text position based on alignment and padding
                    val textOffsetX = when (alignment) {
                        ColumnAlignment.LEFT -> currentX + scaledCellPadding
                        ColumnAlignment.RIGHT -> currentX + scaledCellWidth - scaledCellPadding - textWidth
                        ColumnAlignment.CENTER -> currentX + (scaledCellWidth - textWidth) / 2f
                    }.coerceIn(currentX, currentX + scaledCellWidth - textWidth) // Ensure text doesn't overflow cell bounds horizontally

                    // Calculate vertical center position
                    val verticalCenterOffset = (scaledCurrentRowHeight - textHeight) / 2f
                    val textOffsetY = (currentY + verticalCenterOffset)
                         // Ensure text doesn't overflow cell bounds vertically
                        .coerceIn(currentY, currentY + scaledCurrentRowHeight - textHeight.coerceAtLeast(0f)) // Ensure textHeight is not negative

                    // Add detailed logging for vertical alignment calculation
                    Log.v(TAG, "Cell($rowIndex, $columnIndex): currentY=$currentY, rowH=$scaledCurrentRowHeight, textH=$textHeight, centerOffset=$verticalCenterOffset, finalOffsetY=$textOffsetY")

                    val textDrawOffset = Offset(textOffsetX, textOffsetY)

                    // Draw the text
                    drawText(textLayoutResult = textLayoutResult, topLeft = textDrawOffset)

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
                            textTopLeft = textDrawOffset // Store the actual draw position
                        )
                    )
                    // --- End Capture ---

                } else {
                    // Still advance X even if cell data is missing, to keep grid aligned
                    Log.w(TAG, "No render data found for cell ($rowIndex, $columnIndex)")
                }
                currentX += scaledCellWidth // Move to next column start
            } // End column loop
            currentY += scaledCurrentRowHeight // Move to next row start
        } // End row loop
        Log.v(TAG, "Captured layout info for ${captureLayoutInfoList.size} cells")
    }


    /** Handles tap gestures, now checking for both link and footnote annotations. */
    private fun handleTap(
        tapOffset: Offset,
        cellLayoutInfos: List<CellLayoutInfo>,
        linkHandler: (url: String) -> Unit,
        onFootnoteReferenceClick: ((identifier: String) -> Unit)? // Receive callback
    ) {
        Log.d(TAG, "Tap detected at: $tapOffset. Checking ${cellLayoutInfos.size} cells.")
        val tappedCellInfo = cellLayoutInfos.find { tapOffset in it.bounds }

        if (tappedCellInfo != null) {
            Log.d(TAG, "Tap inside cell bounds: ${tappedCellInfo.bounds}")
            // Calculate offset relative to the text's actual drawn top-left corner
            val textRelativeOffset = tapOffset - tappedCellInfo.textTopLeft

            // Check if the tap is within the measured text dimensions
            if (textRelativeOffset.x >= 0 && textRelativeOffset.y >= 0 &&
                textRelativeOffset.x <= tappedCellInfo.textLayoutResult.size.width &&
                textRelativeOffset.y <= tappedCellInfo.textLayoutResult.size.height) {
                val characterOffset = try {
                    tappedCellInfo.textLayoutResult.getOffsetForPosition(textRelativeOffset)
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting offset for position: $textRelativeOffset in cell: ${tappedCellInfo.bounds}", e)
                    -1 // Indicate error or offset outside valid range
                }

                if (characterOffset >= 0) {
                    Log.d(TAG, "Tap maps to character offset: $characterOffset")
                    val text = tappedCellInfo.textLayoutResult.layoutInput.text // Get the AnnotatedString

                    // Check for Link annotation first
                    text.getStringAnnotations(tag = Link.URL_TAG, start = characterOffset, end = characterOffset)
                        .firstOrNull()?.let { annotation ->
                            Log.i(TAG, "Link clicked in Table (Canvas): ${annotation.item}")
                            linkHandler(annotation.item)
                            return // Handled link
                        }

                    // If no link found, check for Footnote annotation
                    text.getStringAnnotations(tag = FOOTNOTE_REF_TAG, start = characterOffset, end = characterOffset)
                        .firstOrNull()?.let { annotation ->
                            Log.i(TAG, "Footnote ref [^${annotation.item}] clicked in Table (Canvas)")
                            onFootnoteReferenceClick?.invoke(annotation.item) // Call the footnote callback
                            return // Handled footnote
                        }

                    Log.d(TAG, "No link or footnote annotation found at offset $characterOffset")
                } else {
                    Log.w(TAG, "Tap was within text layout, but getOffsetForPosition returned invalid offset for relative: $textRelativeOffset")
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
