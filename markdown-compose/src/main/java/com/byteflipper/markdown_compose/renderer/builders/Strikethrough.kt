package com.byteflipper.markdown_compose.renderer.builders

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.byteflipper.markdown_compose.model.StrikethroughTextNode

object Strikethrough {
    fun render(builder: AnnotatedString.Builder, node: StrikethroughTextNode, textColor: Color) {
        builder.withStyle(
            SpanStyle(
                textDecoration = TextDecoration.LineThrough,
                color = textColor
            )
        ) {
            append(node.text)
        }
    }
}