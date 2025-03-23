package com.byteflipper.markdown_compose.renderer

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import com.byteflipper.markdown_compose.model.*
import com.byteflipper.markdown_compose.renderer.builders.*

private const val TAG = "MarkdownRenderer"

object MarkdownRenderer {
    fun render(nodes: List<MarkdownNode>, textColor: Color = Color.Unspecified): AnnotatedString {
        return buildAnnotatedString {
            for (node in nodes) {
                Log.d(TAG, "Рендеринг узла: $node") // 🔥 Логируем перед рендерингом
                renderNode(this, node, textColor)
            }
        }
    }

    fun renderNode(builder: AnnotatedString.Builder, node: MarkdownNode, textColor: Color) {
        when (node) {
            is HeaderNode -> HeaderRenderer.render(builder, node, textColor)
            is BlockQuoteNode -> BlockQuoteRenderer.render(builder, node, textColor)
            is ListItemNode -> ListItemRenderer.render(builder, node, textColor)
            is BoldTextNode -> BoldRenderer.render(builder, node, textColor)
            is ItalicTextNode -> ItalicRenderer.render(builder, node, textColor)
            is StrikethroughTextNode -> StrikethroughRenderer.render(builder, node, textColor)
            is LinkNode -> LinkRenderer.render(builder, node)
            is CodeNode -> CodeRenderer.render(builder, node)
            is LineBreakNode -> builder.append("\n")
            is TextNode -> TextRenderer.render(builder, node, textColor)
        }
    }
}