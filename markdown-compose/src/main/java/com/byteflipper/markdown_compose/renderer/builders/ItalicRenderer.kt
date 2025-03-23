package com.byteflipper.markdown_compose.renderer.builders

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import com.byteflipper.markdown_compose.model.ItalicTextNode

object ItalicRenderer {
    fun render(builder: AnnotatedString.Builder, node: ItalicTextNode, textColor: Color) {
        builder.withStyle(
            SpanStyle(
                fontStyle = FontStyle.Italic,
                fontFamily = FontFamily.Serif,
                color = textColor
            )
        ) {
            append(node.text)
        }
    }
}