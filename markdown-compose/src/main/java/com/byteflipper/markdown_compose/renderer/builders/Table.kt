package com.byteflipper.markdown_compose.renderer.builders

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import com.byteflipper.markdown_compose.model.*
import com.byteflipper.markdown_compose.renderer.MarkdownRenderer

private const val TAG = "TableRenderer"

object Table {

    /**
     * Renders a Markdown table using a custom Layout and Canvas, applying styles from the StyleSheet.
     * Supports customizable cell padding, border thickness, color, and outer border rounding.
     *
     * @param tableNode The TableNode containing the table data.
     * @param styleSheet The stylesheet defining table appearance.
     * @param modifier Modifier for layout adjustments.
     */
    @Composable
    fun RenderTable(tableNode: TableNode, styleSheet: MarkdownStyleSheet, modifier: Modifier = Modifier) {
        Log.d(TAG, "Start rendering table with style: ${styleSheet.tableStyle}")

        val textMeasurer: TextMeasurer = rememberTextMeasurer()
        val density = LocalDensity.current
        val tableStyle = styleSheet.tableStyle

        // Convert Dp styles to Px
        val cellPaddingPx = with(density) { tableStyle.cellPadding.toPx() }
        val borderThicknessPx = with(density) { tableStyle.borderThickness.toPx().coerceAtLeast(0.1f) }

        // Pre-measure cells and calculate dimensions
        // The lambda passed to remember is NOT a @Composable context
        val (rowHeightsPx, columnWidthsPx, measuredCells) = remember(tableNode, styleSheet, textMeasurer, cellPaddingPx, density) {
            // Pass the necessary non-composable context into the measurement function
            measureTableContent(tableNode, styleSheet, textMeasurer, density, cellPaddingPx)
        }

        // Add padding to dimensions
        val paddedRowHeightsPx = rowHeightsPx.map { it + (2 * cellPaddingPx) }
        val paddedColumnWidthsPx = columnWidthsPx.map { it + (2 * cellPaddingPx) }
        val totalUnscaledPaddedWidth = paddedColumnWidthsPx.sum()
        val totalUnscaledPaddedHeight = paddedRowHeightsPx.sum()

        Layout(
            content = {
                // Use fillMaxSize() to make the Canvas take the size determined by the Layout's measurePolicy
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val layoutWidth = size.width
                    val layoutHeight = size.height

                    // Calculate scale based *only* on width, height scales proportionally
                    val scale = if (totalUnscaledPaddedWidth > 0f) layoutWidth / totalUnscaledPaddedWidth else 1f
                    //val actualScaledHeight = totalUnscaledPaddedHeight * scale // This might not match layoutHeight if constrained

                    // Recalculate scaled dimensions based on actual layout width and scale
                    val scaledPaddedColumnWidths = paddedColumnWidthsPx.map { it * scale }
                    val scaledPaddedRowHeights = paddedRowHeightsPx.map { it * scale }
                    // Consider border thickness scaling - let's NOT scale it for consistency.
                    val scaledBorderThickness = borderThicknessPx
                    val scaledCellPadding = cellPaddingPx * scale

                    Log.d(TAG, "Canvas Size: $size, Scale: $scale")
                    Log.d(TAG, "Scaled Padded Widths: $scaledPaddedColumnWidths")
                    Log.d(TAG, "Scaled Padded Heights: $scaledPaddedRowHeights")


                    // --- Drawing Logic ---
                    val totalGridWidth = scaledPaddedColumnWidths.sum()
                    // Use the actual height provided to the canvas, which might be constrained
                    val totalGridHeight = scaledPaddedRowHeights.sum().coerceAtMost(layoutHeight)

                    // Prepare outline shape if needed
                    val outlineShape = tableStyle.outerBorderShape
                    val outlinePath = if (outlineShape != null) {
                        Path().apply { addOutline(outlineShape.createOutline(Size(totalGridWidth, totalGridHeight), layoutDirection, density)) }
                    } else null

                    // Clip drawing if shape is defined
                    if (outlinePath != null) {
                        clipPath(outlinePath, clipOp = ClipOp.Intersect) {
                            // Draw grid and content within the clipped area
                            drawGridAndContent(
                                tableNode = tableNode,
                                measuredCells = measuredCells,
                                scaledPaddedColumnWidths = scaledPaddedColumnWidths,
                                scaledPaddedRowHeights = scaledPaddedRowHeights,
                                scaledCellPadding = scaledCellPadding,
                                borderThickness = scaledBorderThickness,
                                borderColor = tableStyle.borderColor,
                                columnAlignments = tableNode.columnAlignments
                            )
                        }
                        // Draw the outline border itself if shape exists
                        if (scaledBorderThickness > 0) {
                            drawPath(
                                path = outlinePath,
                                color = tableStyle.borderColor,
                                style = Stroke(width = scaledBorderThickness)
                            )
                        }
                    } else {
                        // Draw without clipping if no shape
                        drawGridAndContent(
                            tableNode = tableNode,
                            measuredCells = measuredCells,
                            scaledPaddedColumnWidths = scaledPaddedColumnWidths,
                            scaledPaddedRowHeights = scaledPaddedRowHeights,
                            scaledCellPadding = scaledCellPadding,
                            borderThickness = scaledBorderThickness,
                            borderColor = tableStyle.borderColor,
                            columnAlignments = tableNode.columnAlignments
                        )
                        // Draw rectangular outer border if thickness > 0 and no shape
                        if (scaledBorderThickness > 0) {
                            drawRect(
                                color = tableStyle.borderColor,
                                topLeft = Offset.Zero,
                                size = Size(totalGridWidth, totalGridHeight),
                                style = Stroke(width = scaledBorderThickness)
                            )
                        }
                    }
                }
            },
            modifier = modifier,
            measurePolicy = { measurables, constraints ->
                // Calculate the required width based on unscaled padded widths
                val requiredWidth = totalUnscaledPaddedWidth.coerceAtLeast(0f)
                // Calculate the final width based on constraints
                val finalWidth = requiredWidth.coerceIn(constraints.minWidth.toFloat(), constraints.maxWidth.toFloat())

                // Calculate scale based on final width
                val scale = if (totalUnscaledPaddedWidth > 0f) finalWidth / totalUnscaledPaddedWidth else 1f
                // Calculate required height based on final scale
                val requiredHeight = (totalUnscaledPaddedHeight * scale).coerceAtLeast(0f)
                // Calculate the final height based on constraints
                val finalHeight = requiredHeight.coerceIn(constraints.minHeight.toFloat(), constraints.maxHeight.toFloat())

                // Measure the single child (Canvas) with the final determined size
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


    /**
     * Measures cell content to determine required row heights and column widths.
     * This function is NOT @Composable and receives necessary context as parameters.
     *
     * @return Triple containing (list of row heights in Px, list of column widths in Px, map of measured cells)
     */
    private fun measureTableContent(
        tableNode: TableNode,
        styleSheet: MarkdownStyleSheet,
        textMeasurer: TextMeasurer,
        density: androidx.compose.ui.unit.Density, // Pass density if needed for render
        cellPaddingPx: Float // Padding already in Px
    ): Triple<List<Float>, List<Float>, Map<Pair<Int, Int>, TextLayoutResult>> {
        val measuredCells = mutableMapOf<Pair<Int, Int>, TextLayoutResult>()
        val rowHeightsPx = mutableListOf<Float>()
        // Initialize column widths with 0f
        val actualColumnCount = tableNode.rows.maxOfOrNull { it.cells.size } ?: tableNode.columnAlignments.size
        val columnWidthsPx = MutableList(actualColumnCount) { 0f }

        tableNode.rows.forEachIndexed { rowIndex, row ->
            var maxRowHeight = 0f
            row.cells.forEach { cell ->
                val columnIndex = cell.columnIndex
                // Render cell content to AnnotatedString - this itself is NOT composable
                val cellAnnotatedString = MarkdownRenderer.render(cell.content, styleSheet)
                // Measure text - this uses TextMeasurer but is NOT composable
                val textLayoutResult = textMeasurer.measure(cellAnnotatedString).also {
                    measuredCells[rowIndex to columnIndex] = it
                }

                val textHeight = textLayoutResult.size.height.toFloat()
                val textWidth = textLayoutResult.size.width.toFloat()

                if (textHeight > maxRowHeight) {
                    maxRowHeight = textHeight
                }

                if (columnIndex < columnWidthsPx.size) {
                    if (textWidth > columnWidthsPx[columnIndex]) {
                        columnWidthsPx[columnIndex] = textWidth
                    }
                } else {
                    Log.w(TAG, "Cell column index $columnIndex out of bounds during measurement (max $actualColumnCount)")
                }
            }
            rowHeightsPx.add(maxRowHeight) // Store height *without* padding
        }

        return Triple(rowHeightsPx.toList(), columnWidthsPx.toList(), measuredCells.toMap())
    }


    /**
     * Draws the table grid lines (internal horizontal and vertical) and the cell content.
     * Should be called within a DrawScope, potentially clipped.
     */
    private fun DrawScope.drawGridAndContent(
        tableNode: TableNode,
        measuredCells: Map<Pair<Int, Int>, TextLayoutResult>,
        scaledPaddedColumnWidths: List<Float>,
        scaledPaddedRowHeights: List<Float>,
        scaledCellPadding: Float,
        borderThickness: Float,
        borderColor: Color,
        columnAlignments: List<ColumnAlignment>
    ) {
        val totalGridHeight = scaledPaddedRowHeights.sum().coerceAtMost(size.height) // Respect draw scope bounds
        val totalGridWidth = scaledPaddedColumnWidths.sum().coerceAtMost(size.width)

        var yPos = 0f
        // Draw horizontal lines (row separators)
        if (borderThickness > 0) {
            scaledPaddedRowHeights.dropLast(1).forEach { rowHeight ->
                yPos += rowHeight
                val currentY = yPos.coerceAtMost(totalGridHeight) // Ensure line is within bounds
                drawLine(
                    color = borderColor,
                    start = Offset(0f, currentY),
                    end = Offset(totalGridWidth, currentY),
                    strokeWidth = borderThickness
                )
            }
        }


        var xPos = 0f
        // Draw vertical lines (column separators)
        if (borderThickness > 0) {
            scaledPaddedColumnWidths.dropLast(1).forEach { colWidth ->
                xPos += colWidth
                val currentX = xPos.coerceAtMost(totalGridWidth) // Ensure line is within bounds
                drawLine(
                    color = borderColor,
                    start = Offset(currentX, 0f),
                    end = Offset(currentX, totalGridHeight),
                    strokeWidth = borderThickness
                )
            }
        }


        // Draw cell contents
        var currentY = 0f
        tableNode.rows.forEachIndexed { rowIndex, row ->
            var currentX = 0f
            val scaledCurrentRowHeight = scaledPaddedRowHeights.getOrElse(rowIndex) { 0f }

            row.cells.forEach { cell ->
                val columnIndex = cell.columnIndex
                if (columnIndex < scaledPaddedColumnWidths.size) {
                    val scaledCellWidth = scaledPaddedColumnWidths[columnIndex]
                    val textLayoutResult = measuredCells[rowIndex to columnIndex]

                    if (textLayoutResult != null) {
                        val alignment = columnAlignments.getOrElse(columnIndex) { ColumnAlignment.LEFT }

                        val textAvailableWidth = (scaledCellWidth - 2 * scaledCellPadding).coerceAtLeast(0f)
                        val textAvailableHeight = (scaledCurrentRowHeight - 2 * scaledCellPadding).coerceAtLeast(0f)

                        val textWidth = textLayoutResult.size.width.toFloat()
                        val textHeight = textLayoutResult.size.height.toFloat()

                        val textOffsetX = when (alignment) {
                            ColumnAlignment.LEFT -> currentX + scaledCellPadding
                            ColumnAlignment.RIGHT -> currentX + scaledCellWidth - scaledCellPadding - textWidth
                            ColumnAlignment.CENTER -> currentX + (scaledCellWidth - textWidth) / 2f
                        }
                        // Center text vertically within the padded cell height
                        val textOffsetY = currentY + (scaledCurrentRowHeight - textHeight) / 2f

                        // Clip text drawing to cell bounds if necessary? TextMeasurer should handle wrapping.
                        // We coerce the topLeft offset to prevent drawing outside the cell boundaries.
                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(
                                x = textOffsetX.coerceIn(currentX, currentX + scaledCellWidth - textWidth),
                                y = textOffsetY.coerceIn(currentY, currentY + scaledCurrentRowHeight - textHeight)
                            )
                        )
                    } else {
                        Log.w(TAG, "No measured text found for cell ($rowIndex, $columnIndex)")
                    }
                    currentX += scaledCellWidth // Move to next column start
                } else {
                    Log.w(TAG, "Cell column index $columnIndex out of bounds during content drawing")
                }
            }
            currentY += scaledCurrentRowHeight // Move to next row start
        }
    }
}