package com.byteflipper.markdown_compose.renderer

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.byteflipper.markdown_compose.model.*

object MarkdownRenderer {
    fun render(nodes: List<MarkdownNode>, textColor: Color = Color.Unspecified): AnnotatedString {
        return buildAnnotatedString {
            for (node in nodes) {
                renderNode(node, textColor)
            }
        }
    }

    private fun AnnotatedString.Builder.renderNode(node: MarkdownNode, textColor: Color) {
        when (node) {
            is HeaderNode -> {
                withStyle(SpanStyle(
                    fontSize = when(node.level) {
                        1 -> 24.sp
                        2 -> 20.sp
                        3 -> 18.sp
                        4 -> 16.sp
                        else -> 14.sp
                    },
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )) {
                    // Render the header content
                    for (contentNode in node.content) {
                        renderNode(contentNode, textColor)
                    }
                    append("\n")
                }
            }

            is BlockQuoteNode -> {
                withStyle(SpanStyle(
                    color = textColor.copy(alpha = 0.7f),
                    fontStyle = FontStyle.Italic,
                    background = Color.LightGray.copy(alpha = 0.2f)
                )) {
                    append("│ ")
                    for (contentNode in node.content) {
                        renderNode(contentNode, textColor)
                    }
                    append("\n")
                }
            }

            is ListItemNode -> {
                withStyle(SpanStyle(color = textColor)) {
                    append("• ")
                    for (contentNode in node.content) {
                        renderNode(contentNode, textColor)
                    }
                    append("\n")
                }
            }

            is BoldTextNode -> {
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
                    )
                ) {
                    append(node.text)
                }
            }

            is ItalicTextNode -> {
                withStyle(
                    SpanStyle(
                        fontStyle = FontStyle.Italic,
                        fontFamily = FontFamily.Serif,
                        color = textColor
                    )
                ) {
                    append(node.text)
                }
            }

            is StrikethroughTextNode -> {
                withStyle(
                    SpanStyle(
                        textDecoration = TextDecoration.LineThrough,
                        color = textColor
                    )
                ) {
                    append(node.text)
                }
            }

            is TextNode -> {
                withStyle(SpanStyle(color = textColor)) {
                    append(node.text)
                }
            }

            is LineBreakNode -> {
                append("\n")
            }

            is LinkNode -> {
                pushStringAnnotation(tag = "URL", annotation = node.url)
                withStyle(SpanStyle(
                    color = Color.Blue,
                    textDecoration = TextDecoration.Underline
                )) {
                    append(node.text)
                }
                pop()
            }

            is CodeNode -> {
                withStyle(SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    background = Color.LightGray.copy(alpha = 0.3f)
                )) {
                    append(node.code)
                }
            }
        }
    }
}