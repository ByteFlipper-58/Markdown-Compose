package com.byteflipper.markdown_compose.model.ir

/**
 * Represents a header element (e.g., # Header 1) in the Markdown IR.
 *
 * @property level The header level (1-6).
 * @property children The inline elements that make up the header's content.
 */
data class HeaderElement(
    val level: Int,
    val children: List<MarkdownElement>
) : MarkdownElement
