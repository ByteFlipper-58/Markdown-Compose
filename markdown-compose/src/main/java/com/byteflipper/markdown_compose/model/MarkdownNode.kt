package com.byteflipper.markdown_compose.model

sealed interface MarkdownNode

data class HeaderNode(val content: List<MarkdownNode>, val level: Int) : MarkdownNode

data class ListItemNode(
    val content: List<MarkdownNode>,
    val indentLevel: Int,
    val isOrdered: Boolean,
    val order: Int? = null
) : MarkdownNode

data class TaskListItemNode(
    val content: List<MarkdownNode>,
    val indentLevel: Int,
    val isChecked: Boolean
) : MarkdownNode

data class BlockQuoteNode(val content: List<MarkdownNode>) : MarkdownNode
object LineBreakNode : MarkdownNode
object HorizontalRuleNode : MarkdownNode

data class BoldTextNode(val text: String) : MarkdownNode
data class ItalicTextNode(val text: String) : MarkdownNode
data class StrikethroughTextNode(val text: String) : MarkdownNode
data class TextNode(val text: String) : MarkdownNode
data class LinkNode(val text: String, val url: String) : MarkdownNode

data class ImageNode(val altText: String, val url: String) : MarkdownNode

data class ImageLinkNode(
    val altText: String,
    val imageUrl: String,
    val linkUrl: String
) : MarkdownNode

data class CodeNode(
    val code: String,
    val language: String? = null,
    val isBlock: Boolean = false
) : MarkdownNode

// --- Footnote Nodes ---

/** Represents an inline footnote reference like [^1] or [^tag]. */
data class FootnoteReferenceNode(
    val identifier: String, // The raw identifier (e.g., "1", "tag")
    var displayIndex: Int? = null // The sequential number [1], [2], etc., determined during rendering
) : MarkdownNode

/** Represents a footnote definition block like [^1]: Some text. */
data class FootnoteDefinitionNode(
    val identifier: String,
    val content: List<MarkdownNode> // The parsed content of the footnote
) : MarkdownNode // Technically a block-level element definition

/** A special node added by the parser to hold all found definitions. Rendered at the end. */
data class FootnoteDefinitionsBlockNode(
    val definitions: Map<String, FootnoteDefinitionNode> // Map: identifier -> DefinitionNode
) : MarkdownNode

// --- Table Nodes ---

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

// --- Definition List Nodes ---

/** Represents a term in a definition list. */
data class DefinitionTermNode(val content: List<MarkdownNode>) : MarkdownNode

/** Represents the details/description part of a definition item. */
data class DefinitionDetailsNode(val content: List<MarkdownNode>) : MarkdownNode

/** Represents a single item (term + details) in a definition list. */
data class DefinitionItemNode(
    val term: DefinitionTermNode,
    val details: List<DefinitionDetailsNode> // Can have multiple detail lines
) : MarkdownNode

/** Represents the entire definition list block. */
data class DefinitionListNode(val items: List<DefinitionItemNode>) : MarkdownNode
