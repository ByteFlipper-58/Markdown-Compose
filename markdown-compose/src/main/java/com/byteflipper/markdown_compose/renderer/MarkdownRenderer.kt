package com.byteflipper.markdown_compose.renderer

import android.util.Log
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.byteflipper.markdown_compose.model.*
import com.byteflipper.markdown_compose.renderer.builders.*

private const val TAG = "MarkdownRenderer"

/** Tag used in AnnotatedString for footnote reference annotations. */
internal const val FOOTNOTE_REF_TAG = "FOOTNOTE_REFERENCE"

/**
 * Object responsible for rendering a list of MarkdownNode objects into an AnnotatedString.
 * This is primarily used for inline content and text-based blocks like Headers and List Items.
 * It handles the recursive application of styles and ensures context (like footnote maps)
 * is passed down correctly.
 */
object MarkdownRenderer {

    /**
     * Renders a list of MarkdownNode objects into an AnnotatedString.
     *
     * @param nodes A list of MarkdownNode objects to render.
     * @param styleSheet The stylesheet defining the appearance of elements.
     * @param footnoteReferenceMap Optional map from footnote identifier to its display index ([1], [2], ...).
     *                             Required for rendering FootnoteReferenceNode correctly.
     * @return An AnnotatedString containing the rendered Markdown.
     */
    fun render(
        nodes: List<MarkdownNode>,
        styleSheet: MarkdownStyleSheet,
        footnoteReferenceMap: Map<String, Int>? = null
    ): AnnotatedString {
        return buildAnnotatedString {
            nodes.forEach { node ->
                renderNode(this, node, styleSheet, footnoteReferenceMap) // Pass map down
            }
        }
    }

    /**
     * Renders a single MarkdownNode into the provided AnnotatedString.Builder,
     * passing the footnote map down for recursive calls.
     *
     * Note: Nodes requiring dedicated Composables (Table, HR, TaskList, Images, BlockCode)
     * are logged as warnings if encountered here, as they should be handled by `MarkdownText`.
     *
     * @param builder The AnnotatedString.Builder to append to.
     * @param node The MarkdownNode to render.
     * @param styleSheet The stylesheet defining the appearance.
     * @param footnoteReferenceMap Map from footnote identifier to its display index.
     */
    fun renderNode(
        builder: AnnotatedString.Builder,
        node: MarkdownNode,
        styleSheet: MarkdownStyleSheet,
        footnoteReferenceMap: Map<String, Int>? = null // Receive map
    ) {
        when (node) {
            // Nodes that render as styled text and might contain children
            is HeaderNode -> Header.render(builder, node, styleSheet, footnoteReferenceMap) // Pass map
            is BlockQuoteNode -> BlockQuote.render(builder, node, styleSheet, footnoteReferenceMap) // Pass map
            is ListItemNode -> ListItem.render(builder, node, styleSheet, footnoteReferenceMap) // Pass map
            is LineBreakNode -> builder.append("\n")

            // Inline style nodes (generally don't need the map themselves)
            is BoldTextNode -> Bold.render(builder, node, styleSheet)
            is ItalicTextNode -> Italic.render(builder, node, styleSheet)
            is StrikethroughTextNode -> Strikethrough.render(builder, node, styleSheet)
            is LinkNode -> Link.render(builder, node, styleSheet) // Link uses its own annotation tag

            // Footnote reference - uses the map directly
            is FootnoteReferenceNode -> renderFootnoteReference(builder, node, styleSheet, footnoteReferenceMap)

            // Code nodes - only render inline here
            is CodeNode -> {
                if (!node.isBlock) {
                    Code.render(builder, node, styleSheet)
                } else {
                    Log.w(TAG, "Block code node encountered in inline rendering path.")
                }
            }
            // Plain text node
            is TextNode -> Text.render(builder, node, styleSheet)

            // Nodes handled by dedicated Composables or logic elsewhere
            is FootnoteDefinitionsBlockNode -> Log.v(TAG, "FootnoteDefinitionsBlockNode skipped during AnnotatedString render.")
            is TaskListItemNode -> Log.w(TAG, "TaskListItemNode should be handled by TaskListItem Composable.")
            is HorizontalRuleNode -> Log.w(TAG, "HorizontalRuleNode should be handled by HorizontalRule Composable.")
            is TableNode -> Log.w(TAG, "TableNode should be handled by Table.RenderTable Composable.")
            is ImageNode -> Log.w(TAG, "ImageNode should be handled by ImageComposable.")
            is ImageLinkNode -> Log.w(TAG, "ImageLinkNode should be handled by ImageLinkComposable.")

            // Internal/Helper Nodes - should not appear in the final render list
            is TableCellNode, is TableRowNode -> Log.e(TAG, "${node::class.simpleName} encountered unexpectedly during renderNode.")
            is FootnoteDefinitionNode -> Log.e(TAG, "FootnoteDefinitionNode encountered unexpectedly during renderNode.")
        }
    }

    /** Renders an inline footnote reference [^id] as a styled, annotated link [index]. */
    private fun renderFootnoteReference(
        builder: AnnotatedString.Builder,
        node: FootnoteReferenceNode,
        styleSheet: MarkdownStyleSheet,
        footnoteReferenceMap: Map<String, Int>?
    ) {
        val displayIndex = footnoteReferenceMap?.get(node.identifier)
        if (displayIndex == null) {
            // Fallback: Render as plain text if no index is available (e.g., missing definition)
            Log.w(TAG, "No index found for footnote reference [^${node.identifier}]. Rendering as plain text.")
            builder.withStyle(styleSheet.textStyle.toSpanStyle()) { append("[^${node.identifier}]") }
            return
        }

        val referenceText = "[$displayIndex]" // Display text like [1], [2]
        val style = styleSheet.footnoteReferenceStyle // Get style from stylesheet

        // Add annotation for click handling and apply the specific style
        builder.pushStringAnnotation(tag = FOOTNOTE_REF_TAG, annotation = node.identifier)
        builder.withStyle(style) {
            append(referenceText)
        }
        builder.pop() // Remove annotation scope
    }
}