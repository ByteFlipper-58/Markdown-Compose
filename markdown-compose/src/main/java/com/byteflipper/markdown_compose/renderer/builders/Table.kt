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
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.byteflipper.markdown_compose.model.ColumnAlignment
import com.byteflipper.markdown_compose.model.TableNode
import com.byteflipper.markdown_compose.renderer.MarkdownRenderer

private const val TAG = "TableRenderer"

object Table {

    // Define default padding in Dp
    private val DefaultCellPadding: Dp = 8.dp

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
        Log.d(TAG, "Start rendering table")

        val textMeasurer: TextMeasurer = rememberTextMeasurer()

        val density = LocalDensity.current
        val cellPaddingPx: Float = with(density) { DefaultCellPadding.toPx() }

        // Store measured text results to avoid re-measuring
        val measuredCells = mutableMapOf<Pair<Int, Int>, TextLayoutResult>()

        // Calculating column widths and row heights based on content
        val rowHeights = mutableListOf<Float>()
        var columnWidths = tableNode.columnWidths.toMutableList() // Use predefined if available

        val columnCount = tableNode.columnAlignments.size
        // Find the maximum number of cells in any row to determine actual columns needed
        val actualColumnCount = tableNode.rows.maxOfOrNull { it.cells.size } ?: columnCount

        // Ensure columnWidths has enough entries if calculated dynamically
        if (columnWidths.isEmpty()) {
            // Initialize with 0f for all actual columns
            columnWidths = MutableList(actualColumnCount) { 0f }
        } else if (columnWidths.size < actualColumnCount) {
            // Pad existing columnWidths if rows have more columns than initially specified
            columnWidths.addAll(List(actualColumnCount - columnWidths.size) { 0f })
        }


        // Iterate over rows to measure content and calculate dimensions
        tableNode.rows.forEachIndexed { rowIndex, row ->
            var rowMaxHeight = 0f
            row.cells.forEach { cell ->
                val cellContent = MarkdownRenderer.render(cell.content, textColor)
                // Measure text and store result
                val textLayoutResult = textMeasurer.measure(cellContent).also {
                    measuredCells[rowIndex to cell.columnIndex] = it
                }

                val cellHeight = textLayoutResult.size.height.toFloat()
                if (cellHeight > rowMaxHeight) {
                    rowMaxHeight = cellHeight
                }

                // If widths need calculating, find max width per column
                if (tableNode.columnWidths.isEmpty()) {
                    val columnIndex = cell.columnIndex
                    if (columnIndex < columnWidths.size) { // Check bounds
                        val cellWidth = textLayoutResult.size.width.toFloat()
                        if (cellWidth > columnWidths[columnIndex]) {
                            columnWidths[columnIndex] = cellWidth
                        }
                    } else {
                        Log.w(TAG, "Cell column index ${cell.columnIndex} is out of bounds for calculated column widths size ${columnWidths.size}. Row content: $cellContent")
                    }
                }
            }
            // Add padding to row height (top and bottom)
            rowHeights.add(rowMaxHeight + (2 * cellPaddingPx))
        }

        // *** FIX 2: Type inference should now work correctly ***
        // Add padding to each column width (left and right)
        val paddedColumnWidths: List<Float> = columnWidths.map { it + (2 * cellPaddingPx) }
        val totalPaddedWidth: Float = paddedColumnWidths.sum()

        // Using custom Layout to handle table rendering and size reporting
        Layout(
            content = {
                Canvas(modifier = Modifier.fillMaxWidth()) {
                    // If totalPaddedWidth is zero, avoid division by zero
                    val scale = if (totalPaddedWidth > 0f) size.width / totalPaddedWidth else 1f

                    // Scaled dimensions
                    val scaledRowHeights: List<Float> = rowHeights.map { it * scale }
                    val scaledPaddedColumnWidths: List<Float> = paddedColumnWidths.map { it * scale }

                    Log.d(TAG, "Scale: $scale, Width: ${size.width}, TotalPaddedWidth: $totalPaddedWidth")
                    Log.d(TAG, "Scaled Row Heights: $scaledRowHeights")
                    Log.d(TAG, "Scaled Padded Column Widths: $scaledPaddedColumnWidths")

                    // Draw the table grid (borders)
                    drawTableGrid(
                        scaledColumnWidths = scaledPaddedColumnWidths,
                        scaledRowHeights = scaledRowHeights,
                        borderColor = textColor.copy(alpha = 0.3f) // Use a visible border color
                    )

                    // Draw cell contents
                    var yOffset = 0f

                    tableNode.rows.forEachIndexed { rowIndex, row ->
                        var xOffset = 0f
                        val scaledCurrentRowHeight = scaledRowHeights.getOrElse(rowIndex) { 0f }

                        row.cells.forEach { cell ->
                            val columnIndex = cell.columnIndex
                            if (columnIndex < scaledPaddedColumnWidths.size) { // Check bounds
                                val scaledCellWidth = scaledPaddedColumnWidths[columnIndex]
                                // Use pre-measured result
                                val textLayoutResult = measuredCells[rowIndex to columnIndex]

                                if (textLayoutResult != null) {
                                    // Position the text inside the cell based on alignment
                                    val alignment = tableNode.columnAlignments.getOrElse(columnIndex) { ColumnAlignment.LEFT }

                                    // Calculate horizontal position
                                    val textX: Float = when (alignment) {
                                        ColumnAlignment.LEFT -> xOffset + (cellPaddingPx * scale) // Apply padding offset
                                        ColumnAlignment.RIGHT -> xOffset + scaledCellWidth - textLayoutResult.size.width - (cellPaddingPx * scale) // Subtract text width and padding
                                        ColumnAlignment.CENTER -> xOffset + (scaledCellWidth - textLayoutResult.size.width) / 2f // Center between padded bounds
                                    }

                                    // Calculate vertical position (center within the cell height)
                                    val textY: Float = yOffset + (scaledCurrentRowHeight - textLayoutResult.size.height) / 2f

                                    // Draw the text inside the cell
                                    drawText(
                                        textLayoutResult = textLayoutResult,
                                        // Ensure text doesn't draw outside cell bounds due to measurement/float issues
                                        topLeft = Offset(textX.coerceAtLeast(xOffset), textY.coerceAtLeast(yOffset))
                                    )
                                } else {
                                    Log.w(TAG, "No measured text found for cell ($rowIndex, $columnIndex)")
                                }
                                xOffset += scaledCellWidth // Move to the next column position
                            } else {
                                Log.w(TAG, "Cell column index ${cell.columnIndex} is out of bounds for scaled column widths size ${scaledPaddedColumnWidths.size} during drawing.")
                            }
                        }
                        yOffset += scaledCurrentRowHeight // Move to the next row position
                    }
                }
            },
            modifier = modifier,
            // Explicitly type the lambda parameter if necessary, though inference should work
            measurePolicy = { measurables, constraints: Constraints ->
                // Measure the Canvas itself to occupy the constraints' width
                val placeables = measurables.map { measurable ->
                    // Allow canvas to determine its height based on drawing, constrained by width
                    measurable.measure(constraints.copy(minHeight = 0))
                }

                // *** FIX 3: The compareTo error should be resolved by fixing toPx ***
                // Determine the final scaled height based on the actual width constraint
                val finalScale: Float = if (totalPaddedWidth > 0f) constraints.maxWidth / totalPaddedWidth else 1f
                val totalScaledHeightPx = (rowHeights.sum() * finalScale).toInt()

                // Use constraints' max width and calculated scaled height
                val width = constraints.maxWidth
                // Ensure height respects constraints boundaries
                val height = totalScaledHeightPx.coerceIn(constraints.minHeight, constraints.maxHeight)

                Log.d(TAG, "Layout: Width=$width, Height=$height (ScaledTotal: $totalScaledHeightPx)")

                // Layout content within the calculated table size
                layout(width, height) {
                    placeables.forEach { placeable ->
                        placeable.placeRelative(0, 0)
                    }
                }
            }
        )
    }

    /**
     * Draws the grid lines for the table, including both horizontal and vertical lines.
     * Uses the already scaled dimensions.
     *
     * @param scaledColumnWidths List of scaled column widths (including padding).
     * @param scaledRowHeights List of scaled row heights (including padding).
     * @param borderColor The color for the table borders.
     */
    private fun DrawScope.drawTableGrid(
        scaledColumnWidths: List<Float>,
        scaledRowHeights: List<Float>,
        borderColor: Color
    ) {
        var yPos = 0f
        val totalGridHeight = scaledRowHeights.sum()
        // Use DrawScope's size.width which reflects the actual drawing area width
        val totalGridWidth = size.width // Use actual canvas width

        // Draw horizontal lines
        // Draw top border
        drawLine(color = borderColor, start = Offset(0f, 0f), end = Offset(totalGridWidth, 0f), strokeWidth = 1f)
        // Draw row separators and bottom border
        scaledRowHeights.forEach { rowHeight ->
            yPos += rowHeight
            // Coerce line position to ensure it's within the canvas bounds
            val finalYPos = yPos.coerceAtMost(totalGridHeight)
            drawLine(
                color = borderColor,
                start = Offset(0f, finalYPos),
                end = Offset(totalGridWidth, finalYPos),
                strokeWidth = 1f
            )
        }

        // Draw vertical lines
        var xPos = 0f
        // Draw left border
        drawLine(color = borderColor, start = Offset(0f, 0f), end = Offset(0f, totalGridHeight), strokeWidth = 1f)
        // Draw column separators and right border
        // Use indices to safely access widths and prevent issues if list is empty
        scaledColumnWidths.forEachIndexed { index, colWidth ->
            xPos += colWidth
            // Coerce line position, especially for the last line to match totalGridWidth
            val finalXPos = if (index == scaledColumnWidths.lastIndex) totalGridWidth else xPos.coerceAtMost(totalGridWidth)
            drawLine(
                color = borderColor,
                start = Offset(finalXPos, 0f),
                end = Offset(finalXPos, totalGridHeight),
                strokeWidth = 1f
            )
        }
    }
}