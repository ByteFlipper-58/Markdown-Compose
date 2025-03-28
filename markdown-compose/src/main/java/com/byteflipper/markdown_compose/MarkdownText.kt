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
import com.byteflipper.markdown_compose.renderer.builders.TaskListItem

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
    val nodes = remember(markdown) { MarkdownParser.parse(markdown) }
    Log.d(TAG, "Parsed ${nodes.size} nodes for Markdown content.")

    val baseTextStyle = styleSheet.textStyle.merge(LocalTextStyle.current)

    Column(modifier = modifier) {
        var lastRenderedNode: MarkdownNode? = null
        val textNodeGrouper = mutableListOf<MarkdownNode>()

        /**
         * Renders the accumulated inline/text nodes in `textNodeGrouper` as a single `Text` composable.
         * Also handles adding spacing *before* this text block if necessary based on `lastRenderedNode`.
         */
        @Composable
        fun flushTextGroup() {
            if (textNodeGrouper.isNotEmpty()) {
                Log.d(TAG, "Flushing text group (${textNodeGrouper.size} nodes)")
                // Add spacing before the text block if the previous element was a block element.
                if (lastRenderedNode != null && lastRenderedNode !is LineBreakNode) {
                    Spacer(modifier = Modifier.height(styleSheet.blockSpacing))
                    Log.v(TAG, "Added block spacing before flushed text group.")
                }

                val annotatedString = MarkdownRenderer.render(textNodeGrouper, styleSheet)

                Text(
                    text = annotatedString,
                    style = baseTextStyle, // Apply base text style
                    modifier = Modifier.fillMaxWidth()
                )

                lastRenderedNode = textNodeGrouper.last()
                textNodeGrouper.clear()
            } else {
                Log.v(TAG, "flushTextGroup called, but buffer is empty.") // Optional: Reduce log noise
            }
        }

        // Iterate through all parsed nodes.
        nodes.forEachIndexed { index, node ->
            Log.v(TAG, "Processing node $index: ${node::class.simpleName}")
            val needsTopSpacing = lastRenderedNode != null && lastRenderedNode !is LineBreakNode
            Log.v(TAG, "Node $index needsTopSpacing: $needsTopSpacing (last was ${lastRenderedNode?.let { it::class.simpleName }})")

            // --- Spacing Calculation ---
            val spacingDp = when (node) {
                // Explicit LineBreak gets lineBreakSpacing (if space is needed)
                is LineBreakNode -> if (needsTopSpacing) styleSheet.lineBreakSpacing else 0.dp

                // Standard block elements get blockSpacing (if space is needed)
                is HeaderNode, is TableNode, is HorizontalRuleNode, is BlockQuoteNode ->
                    if (needsTopSpacing) styleSheet.blockSpacing else 0.dp
                is CodeNode -> if (node.isBlock && needsTopSpacing) styleSheet.blockSpacing else 0.dp

                // List items & Task list items: Add blockSpacing *only* before the *first* item of a list group.
                is ListItemNode -> if (needsTopSpacing && lastRenderedNode !is ListItemNode && lastRenderedNode !is TaskListItemNode) styleSheet.blockSpacing else 0.dp
                is TaskListItemNode -> if (needsTopSpacing && lastRenderedNode !is ListItemNode && lastRenderedNode !is TaskListItemNode) styleSheet.blockSpacing else 0.dp

                // Inline elements don't add spacing themselves; handled by flushTextGroup.
                else -> 0.dp
            }
            Log.v(TAG, "Node $index (${node::class.simpleName}) calculated spacing: ${spacingDp.value} dp")

            // --- Flush Condition: Determine if the current node is a block-level element ---
            val shouldFlush = when (node) {
                is HeaderNode, is TableNode, is HorizontalRuleNode, is BlockQuoteNode,
                is ListItemNode, is TaskListItemNode, // Task list items are block elements
                is LineBreakNode -> true
                is CodeNode -> node.isBlock // Block code flushes, inline code doesn't
                else -> false // Inline nodes do not trigger a flush
            }
            Log.v(TAG, "Node $index shouldFlush: $shouldFlush")

            // --- Flush Buffer if needed ---
            if (shouldFlush) {
                flushTextGroup()
            }

            // --- Add Vertical Spacing ---
            if (spacingDp > 0.dp) {
                Spacer(modifier = Modifier.height(spacingDp))
                Log.v(TAG,"Added Spacer height ${spacingDp.value} dp before node $index")
            }

            // --- Render the Current Node ---
            when (node) {
                // === Nodes Rendered by Dedicated Composables ===
                is TableNode -> {
                    Table.RenderTable(
                        tableNode = node,
                        styleSheet = styleSheet,
                        modifier = Modifier.fillMaxWidth()
                    )
                    lastRenderedNode = node
                    Log.d(TAG, "Rendered TableNode $index")
                }
                is HorizontalRuleNode -> {
                    HorizontalRule.Render(
                        color = styleSheet.horizontalRuleStyle.color,
                        thickness = styleSheet.horizontalRuleStyle.thickness
                    )
                    lastRenderedNode = node
                    Log.d(TAG, "Rendered HorizontalRuleNode $index")
                }
                is BlockQuoteNode -> {
                    BlockQuoteComposable(node = node, styleSheet = styleSheet, modifier = Modifier.fillMaxWidth())
                    lastRenderedNode = node
                    Log.d(TAG, "Rendered BlockQuoteNode $index")
                }
                is CodeNode -> {
                    if (node.isBlock) {
                        CodeBlockComposable(node = node, styleSheet = styleSheet, modifier = Modifier.fillMaxWidth())
                        lastRenderedNode = node
                        Log.d(TAG, "Rendered CodeBlockNode $index")
                    } else {
                        // Buffer INLINE code for flushTextGroup
                        Log.v(TAG, "Buffering Inline CodeNode $index")
                        textNodeGrouper.add(node)
                    }
                }
                is TaskListItemNode -> {
                    // Use the specific composable for Task List Items
                    TaskListItem(
                        node = node,
                        styleSheet = styleSheet,
                        modifier = Modifier.fillMaxWidth()
                    )
                    lastRenderedNode = node
                    Log.d(TAG, "Rendered TaskListItemNode $index via Composable")
                }

                // === Nodes Rendered primarily as Text (using AnnotatedString renderer) ===
                is HeaderNode -> {
                    Text(
                        text = MarkdownRenderer.render(listOf(node), styleSheet), // Renderer applies H1-H6 style
                        style = baseTextStyle, // Base style provided to Text
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = styleSheet.headerStyle.bottomPadding) // Add padding after
                    )
                    lastRenderedNode = node
                    Log.d(TAG, "Rendered HeaderNode $index")
                }
                is ListItemNode -> {
                    // Regular list items rendered via Text + AnnotatedString renderer
                    Text(
                        text = MarkdownRenderer.render(listOf(node), styleSheet), // Renderer handles bullet/number+content
                        style = baseTextStyle,
                        modifier = Modifier.fillMaxWidth()
                    )
                    lastRenderedNode = node
                    Log.d(TAG, "Rendered ListItemNode $index via Text")
                }
                is LineBreakNode -> {
                    // Explicit line break node (blank line in source). Spacing was handled above.
                    lastRenderedNode = node
                    Log.d(TAG, "Processed LineBreakNode $index")
                }

                // === Inline nodes buffered for flushTextGroup ===
                is TextNode, is BoldTextNode, is ItalicTextNode, is StrikethroughTextNode, is LinkNode -> {
                    Log.v(TAG, "Buffering ${node::class.simpleName} $index")
                    textNodeGrouper.add(node)
                }

                is TableCellNode, is TableRowNode -> {
                    Log.e(TAG, "Encountered unexpected ${node.javaClass.simpleName} $index during rendering.")
                }
            }
        }

        Log.d(TAG, "Flushing remaining text group after loop.")
        flushTextGroup()
        Log.d(TAG, "MarkdownText rendering complete.")
    }
}