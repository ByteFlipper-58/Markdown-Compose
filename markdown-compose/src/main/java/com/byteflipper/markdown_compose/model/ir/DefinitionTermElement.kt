package com.byteflipper.markdown_compose.model.ir

/**
 * Represents the term part of a definition list item in the Markdown IR.
 *
 * @property children The inline elements that make up the term's text.
 */
data class DefinitionTermElement(
    val children: List<MarkdownElement>
) : MarkdownElement
