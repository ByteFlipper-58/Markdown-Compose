package com.byteflipper.markdown_compose.renderer.builders

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import com.byteflipper.markdown_compose.model.ListItemNode
import com.byteflipper.markdown_compose.renderer.MarkdownRenderer

object ListItem {
    fun render(builder: AnnotatedString.Builder, node: ListItemNode, textColor: Color) {
        with(builder) {
            withStyle(SpanStyle(color = textColor)) {
                append("• ")
            }
            node.content.forEach { MarkdownRenderer.renderNode(this, it, textColor) }
        }
    }
}