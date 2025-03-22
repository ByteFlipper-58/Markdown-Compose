package com.byteflipper.markdown_compose

import android.util.Log
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import com.byteflipper.markdown_compose.model.*
import com.byteflipper.markdown_compose.parser.MarkdownParser
import com.byteflipper.markdown_compose.renderer.MarkdownRenderer

private const val TAG = "MarkdownText"

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle(),
    textColor: Color = Color.Unspecified,
    onLinkClick: ((String) -> Unit)? = null
) {
    val uriHandler = LocalUriHandler.current
    val ast = MarkdownParser.parse(text)

    Log.d(TAG, "Parsed ${ast.size} nodes from text: $text")
    ast.forEachIndexed { index, node ->
        when (node) {
            is BoldTextNode -> Log.d(TAG, "Node $index: Bold text: ${node.text}")
            is ItalicTextNode -> Log.d(TAG, "Node $index: Italic text: ${node.text}")
            is TextNode -> Log.d(TAG, "Node $index: Regular text: ${node.text}")
            else -> Log.d(TAG, "Node $index: ${node::class.java.simpleName}")
        }
    }

    val renderedText = MarkdownRenderer.render(ast, textColor)

    ClickableText(
        text = renderedText,
        modifier = modifier,
        style = style,
        onClick = { offset ->
            renderedText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    onLinkClick?.invoke(annotation.item) ?: uriHandler.openUri(annotation.item)
                }
        }
    )
}