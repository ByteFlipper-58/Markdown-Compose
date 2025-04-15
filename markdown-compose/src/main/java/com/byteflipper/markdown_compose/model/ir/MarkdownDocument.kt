package com.byteflipper.markdown_compose.model.ir

/**
 * Represents the root of a Markdown document in the Intermediate Representation (IR).
 * Contains a list of top-level Markdown elements.
 *
 * @property children The list of top-level elements within the document.
 */
data class MarkdownDocument(
    val children: List<MarkdownElement>
) : MarkdownElement
