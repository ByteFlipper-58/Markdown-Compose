package com.byteflipper.markdown_compose.model.ir

/**
 * Represents an inline footnote reference (e.g., [^1] or [^tag]) in the Markdown IR.
 *
 * @property identifier The unique identifier of the footnote (e.g., "1", "tag").
 * @property displayIndex The sequential number assigned during rendering (e.g., 1 for [^1]). Optional, might be added later.
 */
data class FootnoteReferenceElement(
    val identifier: String,
    var displayIndex: Int? = null // Can be populated during a pre-rendering pass or by the renderer
) : MarkdownElement
