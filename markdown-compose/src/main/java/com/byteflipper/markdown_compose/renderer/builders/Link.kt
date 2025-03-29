package com.byteflipper.markdown_compose.renderer.builders

import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import com.byteflipper.markdown_compose.model.LinkNode
import com.byteflipper.markdown_compose.model.MarkdownStyleSheet

object Link {
    internal const val URL_TAG = "URL"

    fun render(builder: AnnotatedString.Builder, node: LinkNode, styleSheet: MarkdownStyleSheet) {
        val linkStyleModel = styleSheet.linkStyle
        // Use base text color if link color is unspecified
        val textColor = linkStyleModel.color.takeOrElse { styleSheet.textStyle.color }
        val spanStyle = SpanStyle(
            color = textColor,
            textDecoration = linkStyleModel.textDecoration
        )

        builder.pushStringAnnotation(tag = URL_TAG, annotation = node.url)
        builder.withStyle(spanStyle) {
            append(node.text)
        }
        builder.pop()
    }
}