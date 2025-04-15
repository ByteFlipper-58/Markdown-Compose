package com.byteflipper.markdown_compose.model.ir

/**
 * Represents bold text (e.g., **bold**) in the Markdown IR.
 *
 * @property children The inline elements contained within the bold tags.
 *                    Typically contains MarkdownTextElement, but could contain others like ItalicElement.
 */
data class BoldElement(
    val children: List<MarkdownElement>
) : MarkdownElement
