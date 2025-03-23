package com.byteflipper.markdown_compose.renderer.builders

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import com.byteflipper.markdown_compose.model.CodeNode

object CodeRenderer {
    fun render(builder: AnnotatedString.Builder, node: CodeNode) {
        builder.withStyle(
            SpanStyle(
                fontFamily = FontFamily.Monospace,
                background = Color.LightGray.copy(alpha = 0.3f)
            )
        ) {
            append(node.code)
            append("\n")
        }
    }
}