package com.byteflipper.markdown_compose.renderer.builders

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.withStyle
import com.byteflipper.markdown_compose.model.HeaderNode
import com.byteflipper.markdown_compose.model.MarkdownStyleSheet
import com.byteflipper.markdown_compose.renderer.MarkdownRenderer

/**
 * Object responsible for rendering Markdown header nodes using styles from MarkdownStyleSheet.
 */
object Header {
    /**
     * Renders a Markdown header node into an [AnnotatedString.Builder] with the appropriate styling
     * based on the header level and the provided [MarkdownStyleSheet].
     *
     * @param builder The [AnnotatedString.Builder] where the header text will be appended.
     * @param node The [HeaderNode] containing the header content and level.
     * @param styleSheet The [MarkdownStyleSheet] defining the visual styles.
     */
    fun render(builder: AnnotatedString.Builder, node: HeaderNode, styleSheet: MarkdownStyleSheet) {
        val headerStyle = styleSheet.headerStyle

        val textStyle = when (node.level) {
            1 -> headerStyle.h1
            2 -> headerStyle.h2
            3 -> headerStyle.h3
            4 -> headerStyle.h4
            5 -> headerStyle.h5
            6 -> headerStyle.h6
            else -> styleSheet.textStyle
        }

        builder.withStyle(
            textStyle.toSpanStyle()
        ) {
            val contentStyleSheet = styleSheet.copy(textStyle = textStyle)
            node.content.forEach { contentNode ->
                MarkdownRenderer.renderNode(this, contentNode, contentStyleSheet)
            }
        }
    }
}