package com.byteflipper.markdown_compose.renderer.builders

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.withStyle
import com.byteflipper.markdown_compose.model.MarkdownStyleSheet
import com.byteflipper.markdown_compose.model.TextNode

object Text {
    fun render(builder: AnnotatedString.Builder, node: TextNode, styleSheet: MarkdownStyleSheet) {
        builder.withStyle(styleSheet.textStyle.toSpanStyle()) {
            append(node.text)
        }
    }
}