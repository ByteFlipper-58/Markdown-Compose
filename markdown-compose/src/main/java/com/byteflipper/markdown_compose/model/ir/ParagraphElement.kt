package com.byteflipper.markdown_compose.model.ir

/**
 * Represents a paragraph block in the Markdown Intermediate Representation.
 * Paragraphs contain inline content.
 *
 * @param children The list of inline Markdown elements within this paragraph.
 */
data class ParagraphElement(
    val children: List<MarkdownElement> // Remove 'override'
) : MarkdownElement // Inherits from MarkdownElement to be part of the IR tree
