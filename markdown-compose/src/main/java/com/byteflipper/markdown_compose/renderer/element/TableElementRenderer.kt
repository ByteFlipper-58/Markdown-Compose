package com.byteflipper.markdown_compose.renderer.element

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
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
import com.byteflipper.markdown_compose.model.MarkdownStyleSheet
import com.byteflipper.markdown_compose.model.ir.*
import com.byteflipper.markdown_compose.renderer.ComposeMarkdownRenderer
import com.byteflipper.markdown_compose.renderer.util.AnnotatedStringRenderUtil // Import the util

/**
 * Renders a table element using Canvas.
 */
internal object TableElementRenderer {

    // Re-define constants or import if needed
    private const val URL_TAG = "URL"
    private const val FOOTNOTE_REF_TAG = "FOOTNOTE_REF" // Define a consistent tag

    private data class CellRenderData(
        val annotatedString: AnnotatedString,
        val textLayoutResult: TextLayoutResult,
        val isHeader: Boolean
    )

    private data class CellLayoutInfo(
        val bounds: Rect,
        val textLayoutResult: TextLayoutResult,
        val textTopLeft: Offset
    )

    // Removed @Composable annotation
    fun render(
        renderer: ComposeMarkdownRenderer,
        element: TableElement
    ): @Composable () -> Unit = {
        RenderTableInternal(
            renderer = renderer,
            tableNode = element,
            styleSheet = renderer.styleSheet,
            modifier = Modifier, // Apply modifier from renderer instance?
            footnoteReferenceMap = renderer.footnoteDefinitions.mapValues { renderer.footnoteDefinitions.keys.indexOf(it.key) + 1 },
            linkHandler = { url -> renderer.onLinkClick?.invoke(url) },
            onFootnoteReferenceClick = { id -> renderer.onFootnoteReferenceClick?.invoke(id) }
        )
    }

    @Composable
    private fun RenderTableInternal(
        renderer: ComposeMarkdownRenderer, // Pass the main renderer instance
        tableNode: TableElement,
        styleSheet: MarkdownStyleSheet,
        modifier: Modifier = Modifier,
        footnoteReferenceMap: Map<String, Int>?,
        linkHandler: (url: String) -> Unit,
        onFootnoteReferenceClick: ((identifier: String) -> Unit)?
    ) {
        Log.d("TableElementRenderer", "Start rendering table with style: ${styleSheet.tableStyle}")

        val textMeasurer: TextMeasurer = rememberTextMeasurer()
        val density = LocalDensity.current
        val tableStyle = styleSheet.tableStyle

        val cellPaddingPx = with(density) { tableStyle.cellPadding.toPx() }
        val borderThicknessPx = with(density) { tableStyle.borderThickness.toPx().coerceAtLeast(0.1f) }

        val (rowHeightsPx, columnWidthsPx, cellRenderData) = remember(tableNode, styleSheet, textMeasurer, cellPaddingPx, density, footnoteReferenceMap) {
            measureTableContent(renderer, tableNode, styleSheet, textMeasurer, cellPaddingPx, footnoteReferenceMap)
        }

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
                        .pointerInput(tableNode, cellLayoutInfosState.value, linkHandler, onFootnoteReferenceClick) {
                            detectTapGestures(
                                onTap = { tapOffset ->
                                    handleTap(
                                        tapOffset = tapOffset,
                                        cellLayoutInfos = cellLayoutInfosState.value,
                                        linkHandler = linkHandler,
                                        onFootnoteReferenceClick = onFootnoteReferenceClick
                                    )
                                }
                            )
                        }
                ) {
                    val layoutWidth = size.width
                    val layoutHeight = size.height
                    val scale = if (totalUnscaledPaddedWidth > 0f) layoutWidth / totalUnscaledPaddedWidth else 1f

                    val scaledPaddedColumnWidths = paddedColumnWidthsPx.map { it * scale }
                    val scaledPaddedRowHeights = paddedRowHeightsPx.map { it * scale }
                    val scaledBorderThickness = borderThicknessPx
                    val scaledCellPadding = cellPaddingPx * scale

                    val totalGridWidth = scaledPaddedColumnWidths.sum()
                    val totalGridHeight = scaledPaddedRowHeights.sum().coerceAtMost(layoutHeight)

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

                    val outlineShape = tableStyle.outerBorderShape
                    val outlinePath = if (outlineShape != null) {
                        Path().apply { addOutline(outlineShape.createOutline(Size(totalGridWidth, totalGridHeight), layoutDirection, density)) }
                    } else null

                    if (outlinePath != null) {
                        clipPath(outlinePath, clipOp = ClipOp.Intersect) { drawAndCapture() }
                        if (scaledBorderThickness > 0) drawPath(path = outlinePath, color = tableStyle.borderColor, style = Stroke(width = scaledBorderThickness))
                    } else {
                        drawAndCapture()
                        if (scaledBorderThickness > 0) drawRect(color = tableStyle.borderColor, topLeft = Offset.Zero, size = Size(totalGridWidth, totalGridHeight), style = Stroke(width = scaledBorderThickness))
                    }

                    if (cellLayoutInfosState.value != currentCellLayoutInfos) {
                        cellLayoutInfosState.value = currentCellLayoutInfos.toList()
                        Log.d("TableElementRenderer", "Updated CellLayoutInfo state with ${currentCellLayoutInfos.size} cells")
                    }
                }
            },
            modifier = modifier,
            measurePolicy = { measurables, constraints ->
                val requiredWidth = totalUnscaledPaddedWidth.coerceAtLeast(0f)
                val finalWidth = requiredWidth.coerceIn(constraints.minWidth.toFloat(), constraints.maxWidth.toFloat())
                val scale = if (totalUnscaledPaddedWidth > 0f) finalWidth / totalUnscaledPaddedWidth else 1f
                val requiredHeight = (totalUnscaledPaddedHeight * scale).coerceAtLeast(0f)
                val finalHeight = requiredHeight.coerceIn(constraints.minHeight.toFloat(), constraints.maxHeight.toFloat())

                val placeable = measurables.first().measure(Constraints.fixed(finalWidth.toInt(), finalHeight.toInt()))
                Log.d("TableElementRenderer", "Layout - Constraints: $constraints, Required W/H: $requiredWidth/$requiredHeight, Final W/H: ${finalWidth.toInt()}/${finalHeight.toInt()}")
                layout(finalWidth.toInt(), finalHeight.toInt()) { placeable.placeRelative(0, 0) }
            }
        )
    }

    private fun measureTableContent(
        renderer: ComposeMarkdownRenderer, // Pass renderer
        tableNode: TableElement,
        styleSheet: MarkdownStyleSheet,
        textMeasurer: TextMeasurer,
        cellPaddingPx: Float,
        footnoteReferenceMap: Map<String, Int>?
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

            for (colIndex in 0 until actualColumnCount) {
                val cellElement = row.cells.getOrNull(colIndex)
                val cellKey = Pair(rowIndex, colIndex)

                val annotatedString = buildAnnotatedString {
                    if (cellElement != null) {
                        // Use the utility function
                        AnnotatedStringRenderUtil.renderChildren(renderer, this, cellElement.children)
                    }
                }

                val textLayoutResult = textMeasurer.measure(annotatedString, cellTextStyle)
                cellDataMap[cellKey] = CellRenderData(annotatedString, textLayoutResult, isHeaderRow)

                val textHeight = textLayoutResult.size.height.toFloat()
                val textWidth = textLayoutResult.size.width.toFloat()
                maxRowHeight = maxOf(maxRowHeight, textHeight)
                columnWidthsPx[colIndex] = maxOf(columnWidthsPx[colIndex], textWidth)
            }
            rowHeightsPx.add(maxRowHeight)
        }
        return Triple(rowHeightsPx.toList(), columnWidthsPx.toList(), cellDataMap.toMap())
    }

    private fun DrawScope.drawGridAndContent(
        tableNode: TableElement,
        cellRenderData: Map<Pair<Int, Int>, CellRenderData>,
        scaledPaddedColumnWidths: List<Float>,
        scaledPaddedRowHeights: List<Float>,
        scaledCellPadding: Float,
        borderThickness: Float,
        borderColor: Color,
        columnAlignments: List<ColumnAlignment>,
        captureLayoutInfoList: MutableList<CellLayoutInfo>
    ) {
        captureLayoutInfoList.clear()
        val totalGridHeight = scaledPaddedRowHeights.sum().coerceAtMost(size.height)
        val totalGridWidth = scaledPaddedColumnWidths.sum().coerceAtMost(size.width)

        var yPos = 0f
        if (borderThickness > 0) {
            scaledPaddedRowHeights.dropLast(1).forEach { rowHeight ->
                yPos += rowHeight
                val currentY = yPos.coerceAtMost(totalGridHeight)
                drawLine(color = borderColor, start = Offset(0f, currentY), end = Offset(totalGridWidth, currentY), strokeWidth = borderThickness)
            }
            var xPos = 0f
            scaledPaddedColumnWidths.dropLast(1).forEach { colWidth ->
                xPos += colWidth
                val currentX = xPos.coerceAtMost(totalGridWidth)
                drawLine(color = borderColor, start = Offset(currentX, 0f), end = Offset(currentX, totalGridHeight), strokeWidth = borderThickness)
            }
        }

        var currentY = 0f
        tableNode.rows.forEachIndexed { rowIndex, row ->
            var currentX = 0f
            val scaledCurrentRowHeight = scaledPaddedRowHeights.getOrElse(rowIndex) { 0f }
            for (columnIndex in 0 until scaledPaddedColumnWidths.size) {
                val cellKey = Pair(rowIndex, columnIndex)
                val data = cellRenderData[cellKey]
                val scaledCellWidth = scaledPaddedColumnWidths.getOrElse(columnIndex) { 0f }
                if (data != null) {
                    val textLayoutResult = data.textLayoutResult
                    val alignment = columnAlignments.getOrElse(columnIndex) { ColumnAlignment.LEFT }
                    val textWidth = textLayoutResult.size.width.toFloat()
                    val textHeight = textLayoutResult.size.height.toFloat()
                    val textOffsetX = when (alignment) {
                        ColumnAlignment.LEFT -> currentX + scaledCellPadding
                        ColumnAlignment.RIGHT -> currentX + scaledCellWidth - scaledCellPadding - textWidth
                        ColumnAlignment.CENTER -> currentX + (scaledCellWidth - textWidth) / 2f
                    }.coerceIn(currentX, currentX + scaledCellWidth - textWidth)
                    val verticalCenterOffset = (scaledCurrentRowHeight - textHeight) / 2f
                    val textOffsetY = (currentY + verticalCenterOffset).coerceIn(currentY, currentY + scaledCurrentRowHeight - textHeight.coerceAtLeast(0f))
                    val textDrawOffset = Offset(textOffsetX, textOffsetY)
                    drawText(textLayoutResult = textLayoutResult, topLeft = textDrawOffset)
                    val cellBounds = Rect(left = currentX, top = currentY, right = currentX + scaledCellWidth, bottom = currentY + scaledCurrentRowHeight)
                    captureLayoutInfoList.add(CellLayoutInfo(bounds = cellBounds, textLayoutResult = textLayoutResult, textTopLeft = textDrawOffset))
                } else {
                    Log.w("TableElementRenderer", "No render data found for cell ($rowIndex, $columnIndex)")
                }
                currentX += scaledCellWidth
            }
            currentY += scaledCurrentRowHeight
        }
        Log.v("TableElementRenderer", "Captured layout info for ${captureLayoutInfoList.size} cells")
    }

    private fun handleTap(
        tapOffset: Offset,
        cellLayoutInfos: List<CellLayoutInfo>,
        linkHandler: (url: String) -> Unit,
        onFootnoteReferenceClick: ((identifier: String) -> Unit)?
    ) {
        Log.d("TableElementRenderer", "Tap detected at: $tapOffset. Checking ${cellLayoutInfos.size} cells.")
        val tappedCellInfo = cellLayoutInfos.find { tapOffset in it.bounds }
        if (tappedCellInfo != null) {
            Log.d("TableElementRenderer", "Tap inside cell bounds: ${tappedCellInfo.bounds}")
            val textRelativeOffset = tapOffset - tappedCellInfo.textTopLeft
            if (textRelativeOffset.x >= 0 && textRelativeOffset.y >= 0 &&
                textRelativeOffset.x <= tappedCellInfo.textLayoutResult.size.width &&
                textRelativeOffset.y <= tappedCellInfo.textLayoutResult.size.height) {
                val characterOffset = try { tappedCellInfo.textLayoutResult.getOffsetForPosition(textRelativeOffset) } catch (e: Exception) { -1 }
                if (characterOffset >= 0) {
                    Log.d("TableElementRenderer", "Tap maps to character offset: $characterOffset")
                    val text = tappedCellInfo.textLayoutResult.layoutInput.text
                    text.getStringAnnotations(tag = URL_TAG, start = characterOffset, end = characterOffset).firstOrNull()?.let {
                        Log.i("TableElementRenderer", "Link clicked in Table (Canvas): ${it.item}")
                        linkHandler(it.item)
                        return
                    }
                    text.getStringAnnotations(tag = FOOTNOTE_REF_TAG, start = characterOffset, end = characterOffset).firstOrNull()?.let {
                        Log.i("TableElementRenderer", "Footnote ref [^${it.item}] clicked in Table (Canvas)")
                        onFootnoteReferenceClick?.invoke(it.item)
                        return
                    }
                    Log.d("TableElementRenderer", "No link or footnote annotation found at offset $characterOffset")
                } else {
                    Log.w("TableElementRenderer", "Tap was within text layout, but getOffsetForPosition returned invalid offset for relative: $textRelativeOffset")
                }
            } else {
                Log.d("TableElementRenderer", "Tap inside cell bounds, but outside text layout area. Relative offset: $textRelativeOffset")
            }
        } else {
            Log.d("TableElementRenderer", "Tap outside any cell bounds.")
        }
    }

    // Helper extension (duplicate - consider extracting)
    private operator fun Rect.contains(offset: Offset): Boolean = offset.x >= left && offset.x < right && offset.y >= top && offset.y < bottom

    // renderChildrenToAnnotatedString was already removed correctly here.
}
