package com.byteflipper.markdown_compose.renderer.builders

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.byteflipper.markdown_compose.model.ListItemNode
import com.byteflipper.markdown_compose.model.MarkdownStyleSheet
import com.byteflipper.markdown_compose.parser.BlockParser // For INPUT_SPACES_PER_LEVEL
import com.byteflipper.markdown_compose.renderer.MarkdownRenderer

object ListItem {

    // Determine visual indentation based on stylesheet's padding per level
    private fun getVisualIndent(logicalIndentLevel: Int, styleSheet: MarkdownStyleSheet): String {
        // Convert Dp padding to an approximate number of space characters
        // This is imprecise but necessary for AnnotatedString indenting.
        // A value like 4 spaces per 8.dp indent might be a starting point.
        val spacesPerDp = 0.5f // Heuristic: Adjust this value as needed visually
        val totalIndentDp = styleSheet.listStyle.indentPadding * logicalIndentLevel
        val indentSpaceCount = (totalIndentDp.value * spacesPerDp).toInt()
        return " ".repeat(indentSpaceCount.coerceAtLeast(0))
    }

    fun render(builder: AnnotatedString.Builder, node: ListItemNode, styleSheet: MarkdownStyleSheet) {
        with(builder) {
            // Calculate logical indentation level based on raw input spaces
            val logicalIndentLevel = node.indentLevel / BlockParser.INPUT_SPACES_PER_LEVEL

            append(getVisualIndent(logicalIndentLevel, styleSheet))

            withStyle(styleSheet.textStyle.toSpanStyle()) {
                val listStyle = styleSheet.listStyle

                if (node.isOrdered) {
                    append(listStyle.numberPrefix(node.order ?: 0)) // Use configured number format
                } else {
                    val bulletIndex = logicalIndentLevel % listStyle.bulletChars.size
                    val bullet = listStyle.bulletChars.getOrElse(bulletIndex) { listStyle.bulletChars.first() } // Fallback
                    append("$bullet ")
                }

                node.content.forEach { contentNode ->
                    MarkdownRenderer.renderNode(this, contentNode, styleSheet)
                }
            }
        }
    }
}