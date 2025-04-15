package com.byteflipper.markdown_compose.model.ir

/**
 * Represents a block quote element (e.g., > Quote) in the Markdown IR.
 *
 * @property children The elements contained within the block quote.
 */
data class BlockQuoteElement(
    val children: List<MarkdownElement>
) : MarkdownElement
