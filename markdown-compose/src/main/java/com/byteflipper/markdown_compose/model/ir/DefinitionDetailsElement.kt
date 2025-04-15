package com.byteflipper.markdown_compose.model.ir

/**
 * Represents the details/description part of a definition list item in the Markdown IR.
 * Usually starts with a colon ':'.
 *
 * @property children The elements that make up the definition's details.
 */
data class DefinitionDetailsElement(
    val children: List<MarkdownElement>
) : MarkdownElement
