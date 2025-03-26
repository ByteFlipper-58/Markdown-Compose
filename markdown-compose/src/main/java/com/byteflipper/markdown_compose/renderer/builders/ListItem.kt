// File: markdown-compose/src/main/java/com/byteflipper/markdown_compose/renderer/builders/ListItem.kt
package com.byteflipper.markdown_compose.renderer.builders

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import com.byteflipper.markdown_compose.model.ListItemNode
import com.byteflipper.markdown_compose.renderer.MarkdownRenderer
// Import dp if needed for padding calculation, although we use spaces here
// import androidx.compose.ui.unit.dp

object ListItem {
    // Define how many spaces constitute one level of visual indentation in the output string
    private const val VISUAL_INDENT_SPACES_PER_LEVEL = 4 // Adjust as needed (e.g., 2 or 4)
    // Define how many raw spaces in the input correspond to one logical level
    private const val INPUT_SPACES_PER_LEVEL = 2 // Based on BlockParser logic (adjust if changed)

    fun render(builder: AnnotatedString.Builder, node: ListItemNode, textColor: Color) {
        with(builder) {
            // Calculate visual indentation level
            val logicalIndentLevel = node.indentLevel / INPUT_SPACES_PER_LEVEL
            val indentSpaces = " ".repeat(logicalIndentLevel * VISUAL_INDENT_SPACES_PER_LEVEL)
            append(indentSpaces)

            // Append bullet or number
            withStyle(SpanStyle(color = textColor)) {
                if (node.isOrdered) {
                    append("${node.order ?: '?'}. ") // Use order number
                } else {
                    // Use different bullets for different nesting levels (based on logical level)
                    val bullet = when (logicalIndentLevel % 3) { // Cycle through 3 bullet types
                        0 -> "•" // Level 0, 3, 6...
                        1 -> "◦" // Level 1, 4, 7...
                        else -> "▪" // Level 2, 5, 8...
                    }
                    append("$bullet ")
                }
            }

            // Render the actual content of the list item
            node.content.forEach { contentNode ->
                MarkdownRenderer.renderNode(this, contentNode, textColor)
            }
            // Do not add newline here; MarkdownText composable handles spacing between elements
        }
    }
}