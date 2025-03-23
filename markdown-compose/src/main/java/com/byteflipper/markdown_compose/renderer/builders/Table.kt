package com.byteflipper.markdown_compose.renderer.builders

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import com.byteflipper.markdown_compose.model.ColumnAlignment
import com.byteflipper.markdown_compose.model.TableNode
import com.byteflipper.markdown_compose.renderer.MarkdownRenderer

private const val TAG = "TableRenderer"

object Table {

    /**
     * Renders a table inside a Composable, with the given table node and text color.
     * This function calculates the table's layout and cell sizes dynamically,
     * and renders the table with proper scaling and alignment.
     * The table grid and content are drawn using `Canvas` for precise control.
     *
     * @param tableNode The TableNode that contains the data to be displayed in the table.
     * @param textColor The color to be used for the table content text.
     * @param modifier The modifier to be applied to the Canvas and Layout components.
     */
    @Composable
    fun RenderTable(tableNode: TableNode, textColor: Color, modifier: Modifier = Modifier) {
        Log.d(TAG, "Начинаем рендеринг таблицы")

        val textMeasurer = rememberTextMeasurer()
        val density = LocalDensity.current

        // Calculating column widths and row heights based on content
        val rowHeights = mutableListOf<Float>()
        var columnWidths = tableNode.columnWidths.toMutableList()

        // If column widths are not defined, calculate them based on content size
        if (columnWidths.isEmpty()) {
            val columnCount = tableNode.columnAlignments.size
            columnWidths = MutableList(columnCount) { 0f }

            // Iterate over rows and calculate max width for each column
            tableNode.rows.forEach { row ->
                row.cells.forEach { cell ->
                    val cellContent = MarkdownRenderer.render(cell.content, textColor)
                    val textLayoutResult = textMeasurer.measure(cellContent)
                    val columnIndex = cell.columnIndex

                    if (columnIndex < columnWidths.size) {
                        val cellWidth = textLayoutResult.size.width.toFloat()
                        if (cellWidth > columnWidths[columnIndex]) {
                            columnWidths[columnIndex] = cellWidth
                        }
                    }
                }
            }
        }

        // Add padding to each column width
        val paddedColumnWidths = columnWidths.map { it + 16f } // 8dp padding on both sides

        // Calculate row heights
        tableNode.rows.forEach { row ->
            var rowMaxHeight = 0f

            row.cells.forEach { cell ->
                val cellContent = MarkdownRenderer.render(cell.content, textColor)
                val textLayoutResult = textMeasurer.measure(cellContent)
                val cellHeight = textLayoutResult.size.height.toFloat()

                if (cellHeight > rowMaxHeight) {
                    rowMaxHeight = cellHeight
                }
            }

            // Add padding to row height
            rowHeights.add(rowMaxHeight + 16f) // 8dp padding on top and bottom
        }

        // Total height of the table
        val tableHeight = rowHeights.sum()

        // Using custom Layout to handle table rendering and size reporting
        Layout(
            content = {
                Canvas(modifier = Modifier.fillMaxWidth()) {
                    val scale = size.width / paddedColumnWidths.sum()

                    // Draw the table grid (borders)
                    drawTableGrid(
                        columnWidths = paddedColumnWidths,
                        rowHeights = rowHeights,
                        scale = scale,
                        borderColor = textColor.copy(alpha = 0.3f)
                    )

                    // Draw cell contents
                    var yOffset = 0f

                    tableNode.rows.forEachIndexed { rowIndex, row ->
                        var xOffset = 0f

                        row.cells.forEach { cell ->
                            val columnIndex = cell.columnIndex
                            if (columnIndex < paddedColumnWidths.size) {
                                val cellWidth = paddedColumnWidths[columnIndex] * scale
                                val cellHeight = rowHeights[rowIndex] * scale

                                // Render cell content
                                val cellContent = MarkdownRenderer.render(cell.content, textColor)
                                val textLayoutResult = textMeasurer.measure(cellContent)

                                // Position the text inside the cell based on alignment
                                val alignment = if (columnIndex < tableNode.columnAlignments.size) {
                                    tableNode.columnAlignments[columnIndex]
                                } else ColumnAlignment.LEFT

                                val textX = when (alignment) {
                                    ColumnAlignment.LEFT -> xOffset + 8f * scale
                                    ColumnAlignment.RIGHT -> xOffset + cellWidth - textLayoutResult.size.width - 8f * scale
                                    ColumnAlignment.CENTER -> xOffset + (cellWidth - textLayoutResult.size.width) / 2
                                }

                                val textY = yOffset + (cellHeight - textLayoutResult.size.height) / 2

                                // Draw the text inside the cell
                                drawText(
                                    textLayoutResult = textLayoutResult,
                                    topLeft = Offset(textX, textY)
                                )

                                xOffset += cellWidth
                            }
                        }

                        yOffset += rowHeights[rowIndex] * scale
                    }
                }
            },
            modifier = modifier,
            measurePolicy = { measurables, constraints ->
                // Measure the content sizes
                val placeables = measurables.map { measurable ->
                    measurable.measure(constraints)
                }

                // Determine the total table height
                val tableHeightPx = tableHeight.toInt()

                // Determine the table width based on available constraints
                val width = constraints.maxWidth

                // Layout content within the calculated table size
                layout(width, tableHeightPx) {
                    placeables.forEach { placeable ->
                        placeable.placeRelative(0, 0)
                    }
                }
            }
        )
    }

    /**
     * Draws the grid lines for the table, including both horizontal and vertical lines.
     *
     * @param columnWidths List of column widths.
     * @param rowHeights List of row heights.
     * @param scale Scaling factor for rendering based on the available width.
     * @param borderColor The color for the table borders.
     */
    private fun DrawScope.drawTableGrid(
        columnWidths: List<Float>,
        rowHeights: List<Float>,
        scale: Float,
        borderColor: Color
    ) {
        var yPos = 0f

        // Draw horizontal lines
        for (i in 0..rowHeights.size) {
            drawLine(
                color = borderColor,
                start = Offset(0f, yPos),
                end = Offset(size.width, yPos),
                strokeWidth = 1f
            )

            if (i < rowHeights.size) {
                yPos += rowHeights[i] * scale
            }
        }

        // Draw vertical lines
        var xPos = 0f
        for (i in 0..columnWidths.size) {
            drawLine(
                color = borderColor,
                start = Offset(xPos, 0f),
                end = Offset(xPos, rowHeights.sum() * scale),
                strokeWidth = 1f
            )

            if (i < columnWidths.size) {
                xPos += columnWidths[i] * scale
            }
        }
    }
}
