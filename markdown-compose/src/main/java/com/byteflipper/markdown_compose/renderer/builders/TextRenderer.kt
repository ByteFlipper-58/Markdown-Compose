package com.byteflipper.markdown_compose.renderer.builders

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import com.byteflipper.markdown_compose.model.TextNode

object TextRenderer {
    fun render(builder: AnnotatedString.Builder, node: TextNode, textColor: Color) {
        builder.withStyle(
            SpanStyle(color = textColor)
        ) {
            append(node.text)
        }
    }
}