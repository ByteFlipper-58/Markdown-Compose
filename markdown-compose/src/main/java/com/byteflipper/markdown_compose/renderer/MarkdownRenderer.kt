package com.byteflipper.markdown_compose.renderer

import android.util.Log
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import com.byteflipper.markdown_compose.model.*
import com.byteflipper.markdown_compose.renderer.builders.*

private const val TAG = "MarkdownRenderer"

/**
 * Object responsible for rendering a list of MarkdownNode objects into an AnnotatedString using
 * a provided MarkdownStyleSheet. Supports various Markdown elements. Primarily for inline/text elements.
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
            for (node in nodes) {
                renderNode(this, node, styleSheet)
            }
        }
    }

    /**
     * Renders a single MarkdownNode into the provided AnnotatedString.Builder using the stylesheet.
     *
     * @param builder The AnnotatedString.Builder to append the rendered content.
     * @param node The MarkdownNode to render.
     * @param styleSheet The stylesheet defining the appearance.
     */
    fun renderNode(builder: AnnotatedString.Builder, node: MarkdownNode, styleSheet: MarkdownStyleSheet) {
        when (node) {
            // Text and Inline Styles
            is HeaderNode -> Header.render(builder, node, styleSheet)
            is BlockQuoteNode -> BlockQuote.render(builder, node, styleSheet)
            is ListItemNode -> ListItem.render(builder, node, styleSheet)
            is BoldTextNode -> Bold.render(builder, node, styleSheet)
            is ItalicTextNode -> Italic.render(builder, node, styleSheet)
            is StrikethroughTextNode -> Strikethrough.render(builder, node, styleSheet)
            is LinkNode -> Link.render(builder, node, styleSheet)
            is CodeNode -> Code.render(builder, node, styleSheet)
            is LineBreakNode -> builder.append("\n")
            is TextNode -> Text.render(builder, node, styleSheet)

            is HorizontalRuleNode -> {
                // Intentionally left blank - Handled by HorizontalRule.Render composable
            }
            is TableNode -> {
                builder.append("[Table Placeholder: ${(node.rows.size)} rows x ${node.columnAlignments.size} cols]")
                Log.d(TAG, "TableNode encountered in AnnotatedString render - should be handled by Table.RenderTable.")
            }
            is TableCellNode, is TableRowNode -> {
                Log.w(TAG, "TableCellNode or TableRowNode encountered unexpectedly in AnnotatedString render.")
            }
        }
    }
}