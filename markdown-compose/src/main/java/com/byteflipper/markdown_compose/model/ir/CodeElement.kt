package com.byteflipper.markdown_compose.model.ir

/**
 * Represents a code element (inline `code` or block ```code```) in the Markdown IR.
 *
 * @property content The raw code content.
 * @property language The language identifier for syntax highlighting (usually for block code). Null if not specified.
 * @property isBlock True if this represents a code block, false for inline code.
 */
data class CodeElement(
    val content: String,
    val language: String? = null,
    val isBlock: Boolean
) : MarkdownElement
