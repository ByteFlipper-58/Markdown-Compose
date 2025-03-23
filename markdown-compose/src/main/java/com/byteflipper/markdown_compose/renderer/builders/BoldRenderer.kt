package com.byteflipper.markdown_compose.renderer.builders

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.byteflipper.markdown_compose.model.BoldTextNode

object BoldRenderer {
    fun render(builder: AnnotatedString.Builder, node: BoldTextNode, textColor: Color) {
        builder.withStyle(SpanStyle(
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )) {
            builder.append(node.text)
        }
    }
}