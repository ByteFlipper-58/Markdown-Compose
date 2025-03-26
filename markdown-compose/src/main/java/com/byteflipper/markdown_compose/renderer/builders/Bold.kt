package com.byteflipper.markdown_compose.renderer.builders

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.withStyle
import com.byteflipper.markdown_compose.model.BoldTextNode
import com.byteflipper.markdown_compose.model.MarkdownStyleSheet

object Bold {
    fun render(builder: AnnotatedString.Builder, node: BoldTextNode, styleSheet: MarkdownStyleSheet) {
        builder.withStyle(styleSheet.boldTextStyle.toSpanStyle()) {
            builder.append(node.text)
        }
    }
}