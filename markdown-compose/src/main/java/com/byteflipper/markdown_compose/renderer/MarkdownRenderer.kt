package com.byteflipper.markdown_compose.renderer

import android.util.Log
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import com.byteflipper.markdown_compose.model.*
import com.byteflipper.markdown_compose.renderer.builders.*

private const val TAG = "MarkdownRenderer"

/**
 * Object responsible for rendering a list of MarkdownNode objects into an AnnotatedString using
 * a provided MarkdownStyleSheet. This is primarily used for rendering inline content
 * within Text composables or for block elements that are essentially styled text (Headers, ListItems).
 * Images and ImageLinks are handled by dedicated Composables.
 */
object MarkdownRenderer {

    /**
     * Renders a list of MarkdownNode objects into an AnnotatedString according to the specified stylesheet.
     *
     * @param nodes A list of MarkdownNode objects to render.
     * @param styleSheet The stylesheet defining the appearance of elements.
     * @return An AnnotatedString containing the rendered Markdown.
     */
    fun render(nodes: List<MarkdownNode>, styleSheet: MarkdownStyleSheet): AnnotatedString {
        return buildAnnotatedString {
            nodes.forEach { node ->
                renderNode(this, node, styleSheet)
            }
        }
    }

    /**
     * Renders a single MarkdownNode into the provided AnnotatedString.Builder using the stylesheet.
     * Note: Nodes like TableNode, HorizontalRuleNode, TaskListItemNode, Block CodeNode, ImageNode,
     * and ImageLinkNode are handled by dedicated Composables in MarkdownText, not here.
     *
     * @param builder The AnnotatedString.Builder to append the rendered content.
     * @param node The MarkdownNode to render.
     * @param styleSheet The stylesheet defining the appearance.
     */
    fun renderNode(builder: AnnotatedString.Builder, node: MarkdownNode, styleSheet: MarkdownStyleSheet) {
        when (node) {
            // Block/Structural elements often rendered as styled text
            is HeaderNode -> Header.render(builder, node, styleSheet)
            is BlockQuoteNode -> BlockQuote.render(builder, node, styleSheet) // Applies style to BQ content
            is ListItemNode -> ListItem.render(builder, node, styleSheet)    // Renders bullet/number + content
            is LineBreakNode -> builder.append("\n")                         // Explicit line break

            // Inline Styles
            is BoldTextNode -> Bold.render(builder, node, styleSheet)
            is ItalicTextNode -> Italic.render(builder, node, styleSheet)
            is StrikethroughTextNode -> Strikethrough.render(builder, node, styleSheet)
            is LinkNode -> Link.render(builder, node, styleSheet)
            is CodeNode -> { // Only renders INLINE code spans here
                if (!node.isBlock) {
                    Code.render(builder, node, styleSheet)
                } else {
                    Log.w(TAG, "renderNode called for BLOCK code node - should be handled by CodeBlockComposable.")
                    // Optionally append placeholder: builder.append("[Block Code]")
                }
            }
            is TextNode -> Text.render(builder, node, styleSheet) // Plain text segment

            // NODES HANDLED BY DEDICATED COMPOSABLES in MarkdownText: Log warnings if they reach here.
            is TaskListItemNode -> {
                Log.w(TAG, "TaskListItemNode encountered in AnnotatedString render - should be handled by TaskListItemComposable.")
            }
            is HorizontalRuleNode -> {
                Log.w(TAG, "HorizontalRuleNode encountered - should be handled by HorizontalRule composable.")
            }
            is TableNode -> {
                Log.w(TAG, "TableNode encountered - should be handled by Table.RenderTable composable.")
            }
            is ImageNode -> {
                Log.w(TAG, "ImageNode encountered in AnnotatedString render - should be handled by ImageComposable.")
            }
            is ImageLinkNode -> {
                Log.w(TAG, "ImageLinkNode encountered in AnnotatedString render - should be handled by ImageLinkComposable.")
            }

            // Internal/Helper Nodes (Should generally not be in the final list)
            is TableCellNode, is TableRowNode -> {
                Log.e(TAG, "TableCellNode or TableRowNode encountered unexpectedly during renderNode.")
            }
        }
    }
}