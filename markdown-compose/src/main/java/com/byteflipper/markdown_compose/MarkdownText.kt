package com.byteflipper.markdown_compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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

/**
 * A Composable function to display Markdown content as a rendered text block in Jetpack Compose.
 * It handles parsing the Markdown, rendering text nodes, and rendering tables separately.
 *
 * @param markdown The Markdown string to be rendered.
 * @param modifier The Modifier to be applied to the Column that holds the content.
 * @param textColor The color of the text. Default is `Color.Unspecified`.
 * @param style The text style to be applied to the rendered text. Default is `MaterialTheme.typography.bodyMedium`.
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    // Parse the Markdown string into a list of Markdown nodes (elements)
    val nodes = remember(markdown) { MarkdownParser.parse(markdown) }

    // Column that will hold the Markdown content
    Column(modifier = modifier) {
        // A list to accumulate non-table text nodes
        val currentTextNodes = mutableListOf<MarkdownNode>()

        // Composable function to render the accumulated text nodes
        @Composable
        fun renderTextNodes() {
            if (currentTextNodes.isNotEmpty()) {
                // Render the accumulated text nodes as a Text composable
                Text(
                    text = MarkdownRenderer.render(currentTextNodes, textColor),
                    style = style,
                    color = textColor,
                    modifier = Modifier.fillMaxWidth()
                )
                // Clear the list after rendering
                currentTextNodes.clear()
            }
        }

        // Iterate through each parsed Markdown node
        nodes.forEach { node ->
            when (node) {
                is TableNode -> {
                    // Before rendering a table, render any accumulated text nodes
                    renderTextNodes()

                    // Add spacing before the table
                    Spacer(modifier = Modifier.height(8.dp))

                    // Render the table using a dedicated Table.RenderTable function
                    Table.RenderTable(
                        tableNode = node,
                        textColor = textColor,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Add spacing after the table
                    Spacer(modifier = Modifier.height(8.dp))
                }
                is HorizontalRuleNode -> {
                    // Before rendering a horizontal rule, render any accumulated text nodes
                    renderTextNodes()

                    // Add spacing before the rule
                    Spacer(modifier = Modifier.height(8.dp))

                    // Render the horizontal rule
                    HorizontalRule.Render(color = textColor)

                    // Add spacing after the rule
                    Spacer(modifier = Modifier.height(8.dp))
                }
                else -> {
                    // If it's not a table node, accumulate the text node
                    currentTextNodes.add(node)
                }
            }
        }

        // Render any remaining text nodes after processing all nodes
        renderTextNodes()
    }
}