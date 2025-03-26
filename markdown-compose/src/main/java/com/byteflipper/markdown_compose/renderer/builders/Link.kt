package com.byteflipper.markdown_compose.renderer.builders

import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import com.byteflipper.markdown_compose.model.LinkNode
import com.byteflipper.markdown_compose.model.MarkdownStyleSheet

object Link {
    private const val URL_TAG = "URL"

    fun render(builder: AnnotatedString.Builder, node: LinkNode, styleSheet: MarkdownStyleSheet) {
        val linkStyleModel = styleSheet.linkStyle
        val spanStyle = SpanStyle(
            color = linkStyleModel.color.takeOrElse { styleSheet.textStyle.color },
            textDecoration = linkStyleModel.textDecoration

        )

        builder.pushStringAnnotation(tag = URL_TAG, annotation = node.url)
        builder.withStyle(spanStyle) {
            append(node.text)
        }
        builder.pop()
    }
}