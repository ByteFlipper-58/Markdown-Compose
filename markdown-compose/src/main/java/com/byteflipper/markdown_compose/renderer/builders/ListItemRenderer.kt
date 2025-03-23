package com.byteflipper.markdown_compose.renderer.builders

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import com.byteflipper.markdown_compose.model.ListItemNode
import com.byteflipper.markdown_compose.renderer.MarkdownRenderer

object ListItemRenderer {
    fun render(builder: AnnotatedString.Builder, node: ListItemNode, textColor: Color) {
        with(builder) {
            append("• ") // Добавляем маркер списка
            node.content.forEach { MarkdownRenderer.renderNode(this, it, textColor) }
            append("\n") // Перенос строки после списка
        }
    }
}