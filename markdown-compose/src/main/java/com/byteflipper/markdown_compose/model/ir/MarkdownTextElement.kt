package com.byteflipper.markdown_compose.model.ir

/**
 * Represents a plain text element in the Markdown Intermediate Representation (IR).
 *
 * @property text The actual text content.
 */
data class MarkdownTextElement(
    val text: String
) : MarkdownElement
