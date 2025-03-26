package com.byteflipper.markdown_compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.byteflipper.markdown_compose.model.*
import com.byteflipper.markdown_compose.parser.MarkdownParser
import com.byteflipper.markdown_compose.renderer.MarkdownRenderer
import com.byteflipper.markdown_compose.renderer.builders.BlockQuoteComposable
import com.byteflipper.markdown_compose.renderer.builders.Table
import com.byteflipper.markdown_compose.renderer.canvas.HorizontalRule

/**
 * Composable function to render Markdown content as Jetpack Compose UI components.
 * Handles block layout, spacing, and rendering of different Markdown elements using customizable styles.
 *
 * @param markdown The Markdown string to be rendered.
 * @param modifier Modifier for layout adjustments.
 * @param styleSheet The stylesheet defining the visual appearance of Markdown elements.
 *                   Defaults to `defaultMarkdownStyleSheet()` based on MaterialTheme.
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    styleSheet: MarkdownStyleSheet = defaultMarkdownStyleSheet()
) {
    val nodes = remember(markdown) { MarkdownParser.parse(markdown) }
    val baseTextStyle = styleSheet.textStyle.merge(LocalTextStyle.current)
    val resolvedTextColor = baseTextStyle.color

    Column(modifier = modifier) {
        var lastRenderedNode: MarkdownNode? = null
        val textNodeGrouper = mutableListOf<MarkdownNode>()

        /**
         * Renders grouped inline text elements in a single `Text` composable.
         */
        @Composable
        fun flushTextGroup() {
            if (textNodeGrouper.isNotEmpty()) {
                // Add spacing before a text block if needed
                if (lastRenderedNode !is LineBreakNode && lastRenderedNode != null) {
                    Spacer(modifier = Modifier.height(styleSheet.blockSpacing))
                }

                Text(
                    text = MarkdownRenderer.render(textNodeGrouper, styleSheet),
                    style = baseTextStyle,
                    modifier = Modifier.fillMaxWidth()
                )
                lastRenderedNode = textNodeGrouper.last()
                textNodeGrouper.clear()
            }
        }

        nodes.forEachIndexed { index, node ->
            val needsTopSpacing = index > 0 && lastRenderedNode != null && lastRenderedNode !is LineBreakNode

            val spacingDp = when (node) {
                is HeaderNode -> if (needsTopSpacing) styleSheet.blockSpacing else 0.dp
                is TableNode, is HorizontalRuleNode, is BlockQuoteNode, is CodeNode ->
                    if (needsTopSpacing) styleSheet.blockSpacing else 0.dp
                // Custom spacing for list items based on context
                is ListItemNode -> when {
                    needsTopSpacing && lastRenderedNode is ListItemNode -> styleSheet.listStyle.itemSpacing // Space between consecutive items
                    needsTopSpacing && lastRenderedNode !is ListItemNode -> styleSheet.listStyle.itemSpacing // Space before first item if needed
                    else -> 0.dp
                }
                is LineBreakNode -> if (lastRenderedNode !is LineBreakNode && lastRenderedNode != null) styleSheet.lineBreakSpacing else 0.dp
                else -> 0.dp
            }

            when (node) {
                is TableNode, is HorizontalRuleNode, is HeaderNode, is ListItemNode, is BlockQuoteNode, is CodeNode -> {
                    flushTextGroup()

                    if (spacingDp > 0.dp) {
                        Spacer(modifier = Modifier.height(spacingDp))
                    }

                    when (node) {
                        is TableNode -> {
                            Table.RenderTable(
                                tableNode = node,
                                styleSheet = styleSheet,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        is HorizontalRuleNode -> {
                            HorizontalRule.Render(
                                color = styleSheet.horizontalRuleStyle.color,
                                thickness = styleSheet.horizontalRuleStyle.thickness
                            )
                        }
                        is HeaderNode -> {
                            Text(
                                text = MarkdownRenderer.render(listOf(node), styleSheet),
                                style = baseTextStyle,
                                modifier = Modifier.fillMaxWidth().padding(bottom = styleSheet.headerStyle.bottomPadding)
                            )
                        }
                        is ListItemNode -> {
                            Text(
                                text = MarkdownRenderer.render(listOf(node), styleSheet),
                                style = baseTextStyle,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        is BlockQuoteNode -> {
                            BlockQuoteComposable(
                                node = node,
                                styleSheet = styleSheet,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        is CodeNode -> {
                            Text(
                                text = MarkdownRenderer.render(listOf(node), styleSheet),
                                style = baseTextStyle,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        else -> {}
                    }
                    lastRenderedNode = node
                }

                is LineBreakNode -> {
                    flushTextGroup()
                    if (spacingDp > 0.dp) {
                        Spacer(modifier = Modifier.height(spacingDp))
                    }
                    lastRenderedNode = node
                }

                else -> {
                    textNodeGrouper.add(node)
                }
            }
        }
        flushTextGroup()
    }
}