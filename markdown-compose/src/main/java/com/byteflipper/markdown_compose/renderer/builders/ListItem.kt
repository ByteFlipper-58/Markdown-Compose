package com.byteflipper.markdown_compose.renderer.builders

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.withStyle
import com.byteflipper.markdown_compose.model.ListItemNode
import com.byteflipper.markdown_compose.model.MarkdownStyleSheet
import com.byteflipper.markdown_compose.parser.BlockParser // For INPUT_SPACES_PER_LEVEL
import com.byteflipper.markdown_compose.renderer.MarkdownRenderer

object ListItem {

    /** Calculates the visual indentation string based on logical level and stylesheet padding. */
    private fun getVisualIndent(logicalIndentLevel: Int, styleSheet: MarkdownStyleSheet): String {
        // Basic heuristic: map indent Dp to number of spaces. Adjust spacesPerDp for visual tuning.
        val spacesPerDp = 0.5f // Example: 8.dp indent -> 4 spaces
        val totalIndentDp = styleSheet.listStyle.indentPadding * logicalIndentLevel
        val indentSpaceCount = (totalIndentDp.value * spacesPerDp).toInt()
        return " ".repeat(indentSpaceCount.coerceAtLeast(0))
    }

    /**
     * Renders a list item (ordered or unordered) into an AnnotatedString builder.
     * Ensures the footnote map is passed down to render nested references correctly.
     *
     * @param builder The AnnotatedString builder.
     * @param node The ListItemNode data.
     * @param styleSheet The stylesheet.
     * @param footnoteReferenceMap Map from footnote ID to display index.
     */
    fun render(
        builder: AnnotatedString.Builder,
        node: ListItemNode,
        styleSheet: MarkdownStyleSheet,
        footnoteReferenceMap: Map<String, Int>? // Add map parameter
    ) {
        with(builder) {
            // Calculate logical indentation level from raw input spaces
            val logicalIndentLevel = node.indentLevel / BlockParser.INPUT_SPACES_PER_LEVEL

            // Append calculated visual indentation
            append(getVisualIndent(logicalIndentLevel, styleSheet))

            // Apply the base text style for the list item
            withStyle(styleSheet.textStyle.toSpanStyle()) {
                val listStyle = styleSheet.listStyle

                // Append bullet or number prefix
                if (node.isOrdered) {
                    append(listStyle.numberPrefix(node.order ?: 0)) // Use configured number format
                } else {
                    // Cycle through bullet characters based on indent level
                    val bulletIndex = logicalIndentLevel % listStyle.bulletChars.size
                    val bullet = listStyle.bulletChars.getOrElse(bulletIndex) { listStyle.bulletChars.first() } // Fallback
                    append("$bullet ")
                }

                // Render the list item content, passing the footnote map down
                node.content.forEach { contentNode ->
                    MarkdownRenderer.renderNode(this, contentNode, styleSheet, footnoteReferenceMap)
                }
            }
        }
    }
}