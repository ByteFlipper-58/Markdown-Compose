package com.byteflipper.markdown_compose

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.byteflipper.markdown_compose.model.*
import com.byteflipper.markdown_compose.parser.MarkdownParser
import com.byteflipper.markdown_compose.renderer.MarkdownRenderer
import com.byteflipper.markdown_compose.renderer.builders.*
import com.byteflipper.markdown_compose.renderer.canvas.HorizontalRule

private const val TAG = "MarkdownText"

/**
 * Composable function to render Markdown content as Jetpack Compose UI components.
 * Handles block layout, spacing, rendering of different Markdown elements, and clickable links.
 *
 * @param markdown The Markdown string to be rendered.
 * @param modifier Modifier for layout adjustments.
 * @param styleSheet The stylesheet defining the visual appearance of Markdown elements.
 *                   Defaults to `defaultMarkdownStyleSheet()` based on MaterialTheme.
 * @param onLinkClick Optional lambda to handle clicks on links. If null, links will be opened
 *                    in the default browser using an ACTION_VIEW Intent. If provided, this lambda
 *                    will be called with the clicked URL string.
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    styleSheet: MarkdownStyleSheet = defaultMarkdownStyleSheet(),
    onLinkClick: ((url: String) -> Unit)? = null // Link click handler parameter
) {
    val nodes = remember(markdown) { MarkdownParser.parse(markdown) }
    Log.d(TAG, "Parsed ${nodes.size} nodes for Markdown content.")

    val baseTextStyle = styleSheet.textStyle.merge(LocalTextStyle.current)
    val context = LocalContext.current // Get context for default link handling

    /** Default link handler using ACTION_VIEW Intent */
    val defaultLinkHandler: (String) -> Unit = remember(context) {
        { url ->
            try {
                Log.i(TAG, "Attempting to open link (default handler): $url")
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Log.e(TAG, "Activity not found to handle link click for URL: $url", e)
                // Optionally show a Toast to the user
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open link for URL: $url", e)
                // Optionally show a Toast
            }
        }
    }

    /** Effective link handler: user's lambda or the default one */
    val currentLinkHandler = onLinkClick ?: defaultLinkHandler

    Column(modifier = modifier) {
        var lastRenderedNode: MarkdownNode? = null
        val textNodeGrouper = mutableListOf<MarkdownNode>() // Buffer for consecutive text/inline nodes

        /** Flushes the buffered inline nodes into a single ClickableText composable. */
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

                // Use ClickableText for paragraphs and mixed inline content
                ClickableText(
                    text = annotatedString,
                    style = baseTextStyle,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { offset ->
                        Log.d(TAG, "Click detected at offset: $offset in flushed text group")
                        annotatedString
                            .getStringAnnotations(tag = Link.URL_TAG, start = offset, end = offset)
                            .firstOrNull()?.let { annotation ->
                                Log.i(TAG, "Link clicked in paragraph/inline: ${annotation.item}")
                                currentLinkHandler(annotation.item) // Use effective handler
                            }
                    }
                )

                lastRenderedNode = textNodeGrouper.last() // Update last node *after* rendering group
                textNodeGrouper.clear()
            } else {
                Log.v(TAG, "flushTextGroup called, but buffer is empty.")
            }
        }

        // Iterate through all parsed nodes.
        nodes.forEachIndexed { index, node ->
            Log.v(TAG, "Processing node $index: ${node::class.simpleName}")
            val needsTopSpacing = lastRenderedNode != null && lastRenderedNode !is LineBreakNode
            Log.v(TAG, "Node $index needsTopSpacing: $needsTopSpacing (last was ${lastRenderedNode?.let { it::class.simpleName }})")

            // --- Flush Condition: Determine if the current node is a block-level element ---
            // Check if the current node necessitates flushing the text buffer
            val shouldFlush = when (node) {
                // These nodes break the paragraph flow and require their own layout
                is HeaderNode, is TableNode, is HorizontalRuleNode, is BlockQuoteNode,
                is ListItemNode, is TaskListItemNode,
                is LineBreakNode // Explicit line break also flushes
                    -> true
                // Code blocks flush
                is CodeNode -> node.isBlock
                // Inline elements do not trigger a flush on their own
                else -> false
            }
            Log.v(TAG, "Node $index shouldFlush: $shouldFlush")


            if (shouldFlush) {
                flushTextGroup()
            }

            val spacingDp = when (node) {
                is LineBreakNode -> if (needsTopSpacing) styleSheet.lineBreakSpacing else 0.dp
                is HeaderNode, is TableNode, is HorizontalRuleNode, is BlockQuoteNode ->
                    if (needsTopSpacing) styleSheet.blockSpacing else 0.dp
                is CodeNode -> if (node.isBlock && needsTopSpacing) styleSheet.blockSpacing else 0.dp
                is ListItemNode -> if (needsTopSpacing && lastRenderedNode !is ListItemNode && lastRenderedNode !is TaskListItemNode) styleSheet.blockSpacing else 0.dp
                is TaskListItemNode -> if (needsTopSpacing && lastRenderedNode !is ListItemNode && lastRenderedNode !is TaskListItemNode) styleSheet.blockSpacing else 0.dp
                else -> 0.dp
            }
            Log.v(TAG, "Node $index (${node::class.simpleName}) calculated spacing: ${spacingDp.value} dp")

            if (spacingDp > 0.dp) {
                Spacer(modifier = Modifier.height(spacingDp))
                Log.v(TAG,"Added Spacer height ${spacingDp.value} dp before node $index")
            }


            // --- Render the Current Node ---
            when (node) {
                // === Nodes Rendered by Dedicated Composables (Non-Text or Complex) ===
                is TableNode -> {
                    Table.RenderTable(
                        tableNode = node,
                        styleSheet = styleSheet,
                        modifier = Modifier.fillMaxWidth(),
                        linkHandler = currentLinkHandler
                    )
                    lastRenderedNode = node
                }
                is HorizontalRuleNode -> {
                    HorizontalRule.Render(
                        color = styleSheet.horizontalRuleStyle.color,
                        thickness = styleSheet.horizontalRuleStyle.thickness
                    )
                    lastRenderedNode = node
                }
                is BlockQuoteNode -> {
                    BlockQuoteComposable(
                        node = node,
                        styleSheet = styleSheet,
                        modifier = Modifier.fillMaxWidth(),
                        linkHandler = currentLinkHandler
                    )
                    lastRenderedNode = node
                }
                is CodeNode -> {
                    if (node.isBlock) {
                        CodeBlockComposable(node = node, styleSheet = styleSheet, modifier = Modifier.fillMaxWidth())
                        lastRenderedNode = node
                    } else {
                        // Buffer INLINE code for flushTextGroup
                        Log.v(TAG, "Buffering Inline CodeNode $index")
                        textNodeGrouper.add(node)
                    }
                }
                is TaskListItemNode -> {
                    TaskListItem(
                        node = node,
                        styleSheet = styleSheet,
                        modifier = Modifier.fillMaxWidth(),
                        linkHandler = currentLinkHandler
                    )
                    lastRenderedNode = node
                }

                // === Nodes Rendered primarily as Text (using ClickableText) ===
                is HeaderNode -> {
                    Text(
                        text = MarkdownRenderer.render(listOf(node), styleSheet), // Renderer applies H1-H6 style
                        style = baseTextStyle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = styleSheet.headerStyle.bottomPadding) // Add padding after
                    )
                    lastRenderedNode = node
                }
                is ListItemNode -> {
                    val listString = MarkdownRenderer.render(listOf(node), styleSheet)
                    ClickableText(
                        text = listString,
                        style = baseTextStyle,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { offset ->
                            listString
                                .getStringAnnotations(Link.URL_TAG, offset, offset)
                                .firstOrNull()?.let { annotation ->
                                    Log.i(TAG, "Link clicked in ListItem: ${annotation.item}")
                                    currentLinkHandler(annotation.item)
                                }
                        }
                    )
                    lastRenderedNode = node // Update last node
                }
                is LineBreakNode -> {
                    // Explicit line break node (blank line in source). Spacing was handled above.
                    lastRenderedNode = node // Update last node (represents the blank line itself)
                    Log.d(TAG, "Processed LineBreakNode $index")
                }

                // === Inline nodes buffered for flushTextGroup ===
                is TextNode, is BoldTextNode, is ItalicTextNode, is StrikethroughTextNode, is LinkNode -> {
                    Log.v(TAG, "Buffering ${node::class.simpleName} $index")
                    textNodeGrouper.add(node)
                }

                // Internal/Helper Nodes (Should not appear here)
                is TableCellNode, is TableRowNode -> {
                    Log.e(TAG, "Encountered unexpected ${node.javaClass.simpleName} $index during rendering.")
                }
            }
        }

        // Flush any remaining text in the buffer after the loop finishes
        Log.d(TAG, "Flushing remaining text group after loop.")
        flushTextGroup()
        Log.d(TAG, "MarkdownText rendering complete.")
    }
}