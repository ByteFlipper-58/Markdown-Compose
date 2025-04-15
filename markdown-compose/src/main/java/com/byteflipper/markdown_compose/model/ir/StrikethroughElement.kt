package com.byteflipper.markdown_compose.model.ir

/**
 * Represents strikethrough text (e.g., ~~strikethrough~~) in the Markdown IR.
 *
 * @property children The inline elements contained within the strikethrough tags.
 */
data class StrikethroughElement(
    val children: List<MarkdownElement>
) : MarkdownElement
