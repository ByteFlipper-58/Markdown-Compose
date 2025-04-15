package com.byteflipper.markdown_compose.model.ir

/**
 * Represents a hyperlink element (e.g., [link text](url)) in the Markdown IR.
 *
 * @property url The destination URL of the link.
 * @property children The inline elements that make up the link's text (e.g., MarkdownTextElement).
 */
data class LinkElement(
    val url: String,
    val children: List<MarkdownElement>
) : MarkdownElement
