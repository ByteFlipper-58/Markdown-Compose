package com.byteflipper.markdown_compose.renderer.builders

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.byteflipper.markdown_compose.model.HeaderNode
import com.byteflipper.markdown_compose.renderer.MarkdownRenderer

/**
 * Object responsible for rendering Markdown header nodes into styled text.
 */
object Header {
    /**
     * Renders a Markdown header node into an [AnnotatedString.Builder] with the appropriate styling.
     *
     * @param builder The [AnnotatedString.Builder] where the header text will be appended.
     * @param node The [HeaderNode] containing the header content and level.
     * @param textColor The color of the header text.
     */
    fun render(builder: AnnotatedString.Builder, node: HeaderNode, textColor: Color) {
        builder.withStyle(
            SpanStyle(
                fontSize = when (node.level) {
                    1 -> 28.sp
                    2 -> 24.sp
                    3 -> 20.sp
                    4 -> 18.sp
                    else -> 16.sp
                },
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        ) {
            node.content.forEach { MarkdownRenderer.renderNode(this, it, textColor) }
        }
    }
}