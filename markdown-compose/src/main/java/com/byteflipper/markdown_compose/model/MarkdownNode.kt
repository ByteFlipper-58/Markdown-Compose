package com.byteflipper.markdown_compose.model

sealed interface MarkdownNode

data class HeaderNode(val content: List<MarkdownNode>, val level: Int) : MarkdownNode
data class ListItemNode(val content: List<MarkdownNode>) : MarkdownNode
data class BlockQuoteNode(val content: List<MarkdownNode>) : MarkdownNode
object LineBreakNode : MarkdownNode

data class BoldTextNode(val text: String) : MarkdownNode
data class ItalicTextNode(val text: String) : MarkdownNode
data class StrikethroughTextNode(val text: String) : MarkdownNode
data class TextNode(val text: String) : MarkdownNode
data class LinkNode(val text: String, val url: String) : MarkdownNode
data class CodeNode(val code: String) : MarkdownNode