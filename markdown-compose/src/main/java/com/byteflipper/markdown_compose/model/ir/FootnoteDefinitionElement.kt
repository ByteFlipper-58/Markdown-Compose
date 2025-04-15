package com.byteflipper.markdown_compose.model.ir

/**
 * Represents a footnote definition block (e.g., [^1]: Footnote text) in the Markdown IR.
 * These are typically collected by the parser and rendered separately at the end.
 *
 * @property identifier The unique identifier of the footnote (e.g., "1", "tag").
 * @property children The elements that make up the footnote's content.
 */
data class FootnoteDefinitionElement(
    val identifier: String,
    val children: List<MarkdownElement>
) : MarkdownElement // This is a definition, not directly rendered inline
