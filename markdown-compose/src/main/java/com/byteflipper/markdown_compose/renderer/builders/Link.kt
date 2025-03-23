package com.byteflipper.markdown_compose.renderer.builders

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.byteflipper.markdown_compose.model.LinkNode

object Link {
    fun render(builder: AnnotatedString.Builder, node: LinkNode) {
        builder.pushStringAnnotation(tag = "URL", annotation = node.url)
        builder.withStyle(
            SpanStyle(
                color = Color.Blue,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append(node.text)
        }
        builder.pop()
    }
}