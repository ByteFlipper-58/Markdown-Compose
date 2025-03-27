package com.byteflipper.markdown_compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.byteflipper.markdown_compose.model.*
import com.byteflipper.markdown_compose.parser.MarkdownParser
import com.byteflipper.markdown_compose.renderer.MarkdownRenderer
import com.byteflipper.markdown_compose.renderer.builders.BlockQuoteComposable
import com.byteflipper.markdown_compose.renderer.builders.CodeBlockComposable
import com.byteflipper.markdown_compose.renderer.builders.Table
import com.byteflipper.markdown_compose.renderer.canvas.HorizontalRule
import android.util.Log

private const val TAG = "MarkdownText"

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
    // Parse the Markdown input string into a list of nodes. Remember based on the input string.
    val nodes = remember(markdown) { MarkdownParser.parse(markdown) }

    // Get the base text style from the stylesheet and merge it with the local ambient style.
    val baseTextStyle = styleSheet.textStyle.merge(LocalTextStyle.current)

    // Use a Column to lay out the parsed Markdown blocks vertically.
    Column(modifier = modifier) {
        var lastRenderedNode: MarkdownNode? = null
        val textNodeGrouper = mutableListOf<MarkdownNode>()

        /**
         * Renders the accumulated inline/text nodes in `textNodeGrouper` as a single `Text` composable.
         * Also handles adding spacing *before* this text block if necessary.
         */
        @Composable
        fun flushTextGroup() {
            // Proceed only if there are nodes in the buffer
            if (textNodeGrouper.isNotEmpty()) {
                // Determine if spacing is needed before this text block.
                // Add spacing if the last rendered element was a block element (not null and not a LineBreak).
                if (lastRenderedNode != null && lastRenderedNode !is LineBreakNode) {
                    Spacer(modifier = Modifier.height(styleSheet.blockSpacing))
                }

                // Render the grouped nodes into an AnnotatedString using the renderer.
                val annotatedString = MarkdownRenderer.render(textNodeGrouper, styleSheet)

                // Display the AnnotatedString using a Text composable.
                Text(
                    text = annotatedString,
                    style = baseTextStyle,
                    modifier = Modifier.fillMaxWidth()
                )

                // Update the last rendered node type to the last node in the flushed group.
                lastRenderedNode = textNodeGrouper.last()
                // Clear the buffer for the next group.
                textNodeGrouper.clear()
            }
        }

        // Iterate through all parsed nodes.
        nodes.forEachIndexed { index, node ->
            // Determine if the current node requires spacing *before* it.
            // Needs spacing if a block element was rendered previously (not null and not a LineBreak).
            val needsTopSpacing = lastRenderedNode != null && lastRenderedNode !is LineBreakNode

            // Calculate the appropriate spacing amount based on the current node type.
            val spacingDp = when (node) {
                // Explicit LineBreak nodes get lineBreakSpacing (if needed).
                is LineBreakNode -> if (needsTopSpacing) styleSheet.lineBreakSpacing else 0.dp

                // Specific block types get blockSpacing (if needed).
                is HeaderNode,
                is TableNode,
                is HorizontalRuleNode,
                is BlockQuoteNode -> if (needsTopSpacing) styleSheet.blockSpacing else 0.dp

                is CodeNode -> {
                    // Check isBlock *after* confirming the type
                    if (node.isBlock && needsTopSpacing) {
                        styleSheet.blockSpacing
                    } else {
                        0.dp
                    }
                }

                // List items: Add blockSpacing *only* before the first item of a list block.
                is ListItemNode -> if (needsTopSpacing && lastRenderedNode !is ListItemNode) {
                    styleSheet.blockSpacing
                } else {
                    // No spacing between consecutive list items (rely on line height or add padding below).
                    // You could add styleSheet.listStyle.itemSpacing here if needed.
                    0.dp
                }

                // Inline elements (Text, Bold, Italic, InlineCode, etc.) do not add spacing themselves.
                // Spacing is handled by `flushTextGroup` before the group is rendered.
                else -> 0.dp
            }

            val isNodeBlockCode = if (node is CodeNode) node.isBlock else false


            // Determine if the node is a type that should trigger flushing the text group buffer.
            // Check isBlock *after* the type check.
            val shouldFlush = when (node) {
                is HeaderNode,
                is TableNode,
                is HorizontalRuleNode,
                is BlockQuoteNode,
                is ListItemNode,
                is LineBreakNode -> true
                is CodeNode -> node.isBlock // Flush only if it's a BLOCK code node
                else -> false // Inline nodes do not trigger a flush
            }

            // Before processing a block element or an explicit line break,
            // ensure any buffered inline/text content is rendered first.
            if (shouldFlush) {
                flushTextGroup()
            }

            // Add the calculated vertical spacing *before* rendering the current node.
            if (spacingDp > 0.dp) {
                Spacer(modifier = Modifier.height(spacingDp))
            }

            // Render the current node based on its type.
            when (node) {
                // Render Headers using Text with specific styling.
                is HeaderNode -> {
                    Text(
                        text = MarkdownRenderer.render(listOf(node), styleSheet),
                        style = baseTextStyle, // Renderer applies H1-H6 style internally
                        modifier = Modifier.fillMaxWidth().padding(bottom = styleSheet.headerStyle.bottomPadding)
                    )
                    lastRenderedNode = node
                }
                // Render Tables using the dedicated Table composable.
                is TableNode -> {
                    Table.RenderTable(
                        tableNode = node,
                        styleSheet = styleSheet,
                        modifier = Modifier.fillMaxWidth()
                    )
                    lastRenderedNode = node
                }
                // Render Horizontal Rules using the dedicated HR composable.
                is HorizontalRuleNode -> {
                    HorizontalRule.Render(
                        color = styleSheet.horizontalRuleStyle.color,
                        thickness = styleSheet.horizontalRuleStyle.thickness,
                    )
                    lastRenderedNode = node // Update last rendered type
                }
                // Render Block Quotes using the dedicated BQ composable.
                is BlockQuoteNode -> {
                    BlockQuoteComposable(
                        node = node,
                        styleSheet = styleSheet,
                        modifier = Modifier.fillMaxWidth()
                    )
                    lastRenderedNode = node
                }
                // Handle Code nodes (block vs inline).
                is CodeNode -> {
                    if (node.isBlock) {
                        // Render Code Blocks using the dedicated composable.
                        CodeBlockComposable(
                            node = node,
                            styleSheet = styleSheet,
                            modifier = Modifier.fillMaxWidth()
                        )
                        lastRenderedNode = node
                    } else {
                        textNodeGrouper.add(node)
                    }
                }
                // Render List Items using Text.
                is ListItemNode -> {
                    Text(
                        text = MarkdownRenderer.render(listOf(node), styleSheet),
                        style = baseTextStyle,
                        modifier = Modifier.fillMaxWidth()
                    )
                    lastRenderedNode = node
                }
                // Handle explicit Line Breaks (spacing was already added).
                is LineBreakNode -> {
                    lastRenderedNode = node
                }
                // Buffer all other inline node types (Text, Bold, Italic, Link, etc.).
                is TextNode, is BoldTextNode, is ItalicTextNode, is StrikethroughTextNode, is LinkNode -> {
                    textNodeGrouper.add(node)
                }
                is TableCellNode, is TableRowNode -> {
                    Log.w(TAG, "Encountered unexpected ${node.javaClass.simpleName} during rendering loop.")
                }
            }
        }
        flushTextGroup()

    }
}