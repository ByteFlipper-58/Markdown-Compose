package com.byteflipper.markdown_compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.byteflipper.markdown_compose.model.*
import com.byteflipper.markdown_compose.parser.MarkdownParser
import com.byteflipper.markdown_compose.renderer.MarkdownRenderer
import com.byteflipper.markdown_compose.renderer.builders.Table
import com.byteflipper.markdown_compose.renderer.canvas.HorizontalRule

// Default spacing values for block elements
private val DefaultBlockSpacing = 8.dp
private val HeaderBottomSpacing = 4.dp

/**
 * Composable function to render Markdown content as Jetpack Compose UI components.
 *
 * @param markdown The Markdown string to be rendered.
 * @param modifier Modifier for layout adjustments.
 * @param textColor Default text color, can be overridden.
 * @param style Text style applied to Markdown elements.
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    val nodes = remember(markdown) { MarkdownParser.parse(markdown) }

    Column(modifier = modifier) {
        var previousNode: MarkdownNode? = null
        val textNodeGrouper = mutableListOf<MarkdownNode>()

        /**
         * Renders grouped inline text elements in a single `Text` composable.
         */
        @Composable
        fun flushTextGroup() {
            if (textNodeGrouper.isNotEmpty()) {
                if (previousNode != null) {
                    Spacer(modifier = Modifier.height(DefaultBlockSpacing))
                }
                Text(
                    text = MarkdownRenderer.render(textNodeGrouper, textColor),
                    style = style,
                    color = textColor,
                    modifier = Modifier.fillMaxWidth()
                )
                previousNode = textNodeGrouper.last()
                textNodeGrouper.clear()
            }
        }

        nodes.forEachIndexed { index, node ->
            val addSpacing = index > 0 && when (node) {
                is ListItemNode -> previousNode !is ListItemNode
                else -> true
            }

            when (node) {
                is TableNode -> {
                    flushTextGroup()
                    if (addSpacing) Spacer(modifier = Modifier.height(DefaultBlockSpacing))
                    Table.RenderTable(
                        tableNode = node,
                        textColor = textColor,
                        modifier = Modifier.fillMaxWidth()
                    )
                    previousNode = node
                }
                is HorizontalRuleNode -> {
                    flushTextGroup()
                    if (addSpacing) Spacer(modifier = Modifier.height(DefaultBlockSpacing))
                    HorizontalRule.Render(color = textColor.copy(alpha = 0.5f))
                    previousNode = node
                }
                is HeaderNode -> {
                    flushTextGroup()
                    if (addSpacing) Spacer(modifier = Modifier.height(DefaultBlockSpacing))
                    Text(
                        text = MarkdownRenderer.render(listOf(node), textColor),
                        style = style,
                        color = textColor,
                        modifier = Modifier.fillMaxWidth().padding(bottom = HeaderBottomSpacing)
                    )
                    previousNode = node
                }
                is ListItemNode -> {
                    flushTextGroup()
                    if (addSpacing) Spacer(modifier = Modifier.height(DefaultBlockSpacing / 2))
                    Text(
                        text = MarkdownRenderer.render(listOf(node), textColor),
                        style = style,
                        color = textColor,
                        modifier = Modifier.fillMaxWidth()
                    )
                    previousNode = node
                }
                is BlockQuoteNode, is CodeNode -> {
                    flushTextGroup()
                    if (addSpacing) Spacer(modifier = Modifier.height(DefaultBlockSpacing))
                    Text(
                        text = MarkdownRenderer.render(listOf(node), textColor),
                        style = style,
                        color = textColor,
                        modifier = Modifier.fillMaxWidth()
                    )
                    previousNode = node
                }
                is LineBreakNode -> {
                    flushTextGroup()
                    if (previousNode !is LineBreakNode) {
                        Spacer(modifier = Modifier.height(DefaultBlockSpacing))
                    }
                    previousNode = node
                }
                else -> {
                    textNodeGrouper.add(node)
                }
            }
        }
        flushTextGroup()
    }
}
