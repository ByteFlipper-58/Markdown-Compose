package com.byteflipper.markdown_compose.renderer.builders

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.withStyle
import com.byteflipper.markdown_compose.model.MarkdownStyleSheet
import com.byteflipper.markdown_compose.model.StrikethroughTextNode

object Strikethrough {
    fun render(builder: AnnotatedString.Builder, node: StrikethroughTextNode, styleSheet: MarkdownStyleSheet) {
        builder.withStyle(styleSheet.strikethroughTextStyle.toSpanStyle()) {
            append(node.text)
        }
    }
}