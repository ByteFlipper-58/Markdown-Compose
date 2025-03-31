package com.byteflipper.markdown_compose.renderer.builders

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.withStyle
import com.byteflipper.markdown_compose.model.HeaderNode
import com.byteflipper.markdown_compose.model.MarkdownStyleSheet
import com.byteflipper.markdown_compose.renderer.MarkdownRenderer

object Header {
    /**
     * Renders a Markdown header node into an [AnnotatedString.Builder].
     * Ensures the footnote map is passed down to render nested references correctly.
     *
     * @param builder The [AnnotatedString.Builder] to append to.
     * @param node The [HeaderNode] containing the header content and level.
     * @param styleSheet The [MarkdownStyleSheet] defining visual styles.
     * @param footnoteReferenceMap Map from footnote ID to display index.
     */
    fun render(
        builder: AnnotatedString.Builder,
        node: HeaderNode,
        styleSheet: MarkdownStyleSheet,
        footnoteReferenceMap: Map<String, Int>?
    ) {
        val headerStyle = styleSheet.headerStyle

        // Determine the specific text style based on the header level
        val textStyle = when (node.level) {
            1 -> headerStyle.h1
            2 -> headerStyle.h2
            3 -> headerStyle.h3
            4 -> headerStyle.h4
            5 -> headerStyle.h5
            6 -> headerStyle.h6
            else -> styleSheet.textStyle // Fallback to base style if level is invalid
        }

        // Use the determined header style as the base for its content rendering
        val contentStyleSheet = styleSheet.copy(textStyle = textStyle)

        // Apply the header style and render the content
        builder.withStyle(textStyle.toSpanStyle()) {
            // Pass the map down when rendering child nodes within the header
            node.content.forEach { contentNode ->
                MarkdownRenderer.renderNode(this, contentNode, contentStyleSheet, footnoteReferenceMap)
            }
        }
    }
}