package com.byteflipper.markdown_compose.renderer.builders

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.withStyle
import com.byteflipper.markdown_compose.model.ItalicTextNode
import com.byteflipper.markdown_compose.model.MarkdownStyleSheet

object Italic {
    fun render(builder: AnnotatedString.Builder, node: ItalicTextNode, styleSheet: MarkdownStyleSheet) {
        builder.withStyle(styleSheet.italicTextStyle.toSpanStyle()) {
            append(node.text)
        }
    }
}