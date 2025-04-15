package com.byteflipper.markdown_compose.model.ir

/**
 * Represents italic text (e.g., *italic* or _italic_) in the Markdown IR.
 *
 * @property children The inline elements contained within the italic tags.
 */
data class ItalicElement(
    val children: List<MarkdownElement>
) : MarkdownElement
