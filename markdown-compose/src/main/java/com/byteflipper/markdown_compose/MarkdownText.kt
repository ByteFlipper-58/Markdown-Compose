// File: markdown-compose/src/main/java/com/byteflipper/markdown_compose/MarkdownText.kt
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
private val DefaultBlockSpacing = 16.dp
private val HeaderBottomSpacing = 4.dp
private val ListSpacing = 4.dp
private val LineBreakSpacing = 16.dp


/**
 * Composable function to render Markdown content as Jetpack Compose UI components.
 * Handles block layout, spacing, and rendering of different Markdown elements.
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
    // Parse Markdown once and remember the result
    val nodes = remember(markdown) { MarkdownParser.parse(markdown) }
    val resolvedTextColor = if (textColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else textColor


    Column(modifier = modifier) {
        var lastRenderedNode: MarkdownNode? = null // Track the last *block* node rendered for spacing logic
        val textNodeGrouper = mutableListOf<MarkdownNode>() // Group consecutive inline/text nodes

        /**
         * Renders grouped inline text elements (TextNode, Bold, Italic, etc.)
         * in a single `Text` composable for better performance and paragraph handling.
         */
        @Composable
        fun flushTextGroup() {
            if (textNodeGrouper.isNotEmpty()) {
                // Add spacing before a text block if needed (e.g., after HR, Header)
                if (lastRenderedNode !is LineBreakNode && lastRenderedNode != null) {
                    Spacer(modifier = Modifier.height(DefaultBlockSpacing))
                }

                Text(
                    text = MarkdownRenderer.render(textNodeGrouper, resolvedTextColor),
                    style = style,
                    color = resolvedTextColor, // Apply resolved color
                    modifier = Modifier.fillMaxWidth()
                )
                lastRenderedNode = textNodeGrouper.last() // Mark the end of the text block
                textNodeGrouper.clear()
            }
        }

        nodes.forEachIndexed { index, node ->
            // Determine if top spacing is needed before rendering the current node
            val needsTopSpacing = index > 0 && lastRenderedNode != null && lastRenderedNode !is LineBreakNode

            // Calculate spacing based on current node type and context
            val spacingDp = when (node) {
                is HeaderNode -> if (needsTopSpacing) DefaultBlockSpacing else 0.dp
                is TableNode, is HorizontalRuleNode, is BlockQuoteNode, is CodeNode ->
                    if (needsTopSpacing) DefaultBlockSpacing else 0.dp
                is ListItemNode -> if (needsTopSpacing && lastRenderedNode !is ListItemNode) ListSpacing else 0.dp // Space before list starts
                is LineBreakNode -> if (lastRenderedNode !is LineBreakNode && lastRenderedNode != null) LineBreakSpacing else 0.dp // Space for blank line
                else -> 0.dp // Text nodes are handled by flushTextGroup
            }


            // Render based on node type
            when (node) {
                // Block elements that require flushing previous text group first
                is TableNode, is HorizontalRuleNode, is HeaderNode, is ListItemNode, is BlockQuoteNode, is CodeNode -> {
                    flushTextGroup() // Render any pending text first

                    // Add calculated spacing
                    if (spacingDp > 0.dp) {
                        Spacer(modifier = Modifier.height(spacingDp))
                    }

                    // Render the specific block element
                    when (node) {
                        is TableNode -> {
                            Table.RenderTable(
                                tableNode = node,
                                textColor = resolvedTextColor,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        is HorizontalRuleNode -> {
                            HorizontalRule.Render(color = resolvedTextColor.copy(alpha = 0.5f))
                        }
                        is HeaderNode -> {
                            Text(
                                text = MarkdownRenderer.render(listOf(node), resolvedTextColor),
                                style = style, // Base style, specific styling done in renderer
                                color = resolvedTextColor,
                                modifier = Modifier.fillMaxWidth().padding(bottom = HeaderBottomSpacing) // Add bottom padding specific to headers
                            )
                        }
                        is ListItemNode -> {
                            // Padding/indentation is handled by the renderer using spaces
                            Text(
                                text = MarkdownRenderer.render(listOf(node), resolvedTextColor),
                                style = style,
                                color = resolvedTextColor,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        is BlockQuoteNode -> {
                            Text(
                                text = MarkdownRenderer.render(listOf(node), resolvedTextColor),
                                style = style, // Base style
                                color = resolvedTextColor, // Renderer might override color/style
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        is CodeNode -> {
                            Text(
                                text = MarkdownRenderer.render(listOf(node), resolvedTextColor),
                                style = style, // Base style
                                color = resolvedTextColor, // Renderer might override color/style
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        // Exhaustive check - should not happen due to outer when
                        else -> {}
                    }
                    lastRenderedNode = node // Update last rendered block node
                }

                // Line break node - flush text and add specific spacing
                is LineBreakNode -> {
                    flushTextGroup()
                    if (spacingDp > 0.dp) {
                        Spacer(modifier = Modifier.height(spacingDp))
                    }
                    lastRenderedNode = node // Mark line break as rendered
                }

                // Inline/Text elements - add to grouper
                else -> { // Includes TextNode, BoldTextNode, ItalicTextNode, etc.
                    textNodeGrouper.add(node)
                    // Don't update lastRenderedNode here; wait for flushTextGroup
                }
            }
        }
        // Flush any remaining text at the end
        flushTextGroup()
    }
}