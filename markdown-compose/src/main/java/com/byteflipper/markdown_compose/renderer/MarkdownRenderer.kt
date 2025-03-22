package com.byteflipper.markdown_compose.renderer

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
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
                        else -> 16.sp
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
                // Explicitly use a bold font weight
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = textColor)) {
                    append(node.text)
                }
            }

            is ItalicTextNode -> {
                // Explicitly use italic font style
                withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = textColor)) {
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
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    background = Color.LightGray.copy(alpha = 0.3f)
                )) {
                    append(node.code)
                }
            }
        }
    }
}