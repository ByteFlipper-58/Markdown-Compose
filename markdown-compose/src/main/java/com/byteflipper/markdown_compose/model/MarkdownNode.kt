// File: markdown-compose/src/main/java/com/byteflipper/markdown_compose/model/MarkdownNode.kt
package com.byteflipper.markdown_compose.model

sealed interface MarkdownNode

data class HeaderNode(val content: List<MarkdownNode>, val level: Int) : MarkdownNode

data class ListItemNode(
    val content: List<MarkdownNode>,
    val indentLevel: Int,
    val isOrdered: Boolean,
    val order: Int? = null
) : MarkdownNode

data class BlockQuoteNode(val content: List<MarkdownNode>) : MarkdownNode
object LineBreakNode : MarkdownNode
object HorizontalRuleNode : MarkdownNode

data class BoldTextNode(val text: String) : MarkdownNode
data class ItalicTextNode(val text: String) : MarkdownNode
data class StrikethroughTextNode(val text: String) : MarkdownNode
data class TextNode(val text: String) : MarkdownNode
data class LinkNode(val text: String, val url: String) : MarkdownNode
data class CodeNode(val code: String) : MarkdownNode

enum class ColumnAlignment {
    LEFT,
    RIGHT,
    CENTER
}

data class TableCellNode(
    val content: List<MarkdownNode>,
    val columnIndex: Int
) : MarkdownNode

data class TableRowNode(
    val cells: List<TableCellNode>,
    val isHeader: Boolean = false
) : MarkdownNode

data class TableNode(
    val rows: List<TableRowNode>,
    val columnAlignments: List<ColumnAlignment>,
    val columnWidths: List<Float> = emptyList()
) : MarkdownNode