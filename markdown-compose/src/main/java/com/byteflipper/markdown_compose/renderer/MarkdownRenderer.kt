package com.byteflipper.markdown_compose.renderer

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import com.byteflipper.markdown_compose.model.*
import com.byteflipper.markdown_compose.renderer.builders.*

private const val TAG = "MarkdownRenderer"

/**
 * Object responsible for rendering a list of MarkdownNode objects into an AnnotatedString that can be
 * used in Jetpack Compose UI. It supports rendering various Markdown elements like headers, blockquotes,
 * lists, text formatting (bold, italic, strikethrough), links, and code.
 */
object MarkdownRenderer {

    /**
     * Renders a list of MarkdownNode objects into an AnnotatedString, which can be displayed in Jetpack
     * Compose UI. Each node is rendered according to its type.
     *
     * @param nodes A list of MarkdownNode objects to render.
     * @param textColor The color to apply to the text. Default is `Color.Unspecified` which means no color is applied.
     * @return An AnnotatedString containing the rendered Markdown.
     */
    fun render(nodes: List<MarkdownNode>, textColor: Color = Color.Unspecified): AnnotatedString {
        return buildAnnotatedString {
            // Loop through each node and render it
            for (node in nodes) {
                Log.d(TAG, "Rendering node: $node") // 🔥 Log before rendering each node
                renderNode(this, node, textColor)
            }
        }
    }

    /**
     * Renders a single MarkdownNode into the provided AnnotatedString.Builder.
     *
     * @param builder The AnnotatedString.Builder to append the rendered content.
     * @param node The MarkdownNode to render.
     * @param textColor The color to apply to the text.
     */
    fun renderNode(builder: AnnotatedString.Builder, node: MarkdownNode, textColor: Color) {
        when (node) {
            is HeaderNode -> Header.render(builder, node, textColor)
            is BlockQuoteNode -> BlockQuote.render(builder, node, textColor)
            is ListItemNode -> ListItem.render(builder, node, textColor)
            is BoldTextNode -> Bold.render(builder, node, textColor)
            is ItalicTextNode -> Italic.render(builder, node, textColor)
            is StrikethroughTextNode -> Strikethrough.render(builder, node, textColor)
            is LinkNode -> Link.render(builder, node)
            is CodeNode -> Code.render(builder, node)
            is LineBreakNode -> builder.append("\n")
            is TextNode -> Text.render(builder, node, textColor)

            // Tables are not directly rendered through AnnotatedString
            // They are handled by a separate component.
            is TableNode -> {
                builder.append("[Table: ${(node.rows.size)} rows x ${node.columnAlignments.size} columns]")
            }
            is TableCellNode, is TableRowNode -> {
                Log.d(TAG, "Attempt to render table component directly: $node")
            }
        }
    }
}