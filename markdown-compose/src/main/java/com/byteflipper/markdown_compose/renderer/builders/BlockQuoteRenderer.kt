package com.byteflipper.markdown_compose.renderer.builders

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import com.byteflipper.markdown_compose.model.BlockQuoteNode
import com.byteflipper.markdown_compose.renderer.MarkdownRenderer

object BlockQuoteRenderer {
    fun render(builder: AnnotatedString.Builder, node: BlockQuoteNode, textColor: Color) {
        with(builder) {
            withStyle(
                SpanStyle(
                    color = textColor.copy(alpha = 0.8f),
                    fontStyle = FontStyle.Italic,
                    background = Color.LightGray.copy(alpha = 0.3f)
                )
            ) {
                append("│ ")
                node.content.forEach { MarkdownRenderer.renderNode(this, it, textColor) }
                append("\n")
            }
        }
    }
}