package com.byteflipper.markdown_compose.model.ir

/**
 * Represents a single item within a list (ordered or unordered) in the Markdown IR.
 *
 * @property children The elements contained within the list item.
 * @property order The numerical order for ordered lists (e.g., 1, 2), null for unordered lists.
 */
data class ListItemElement(
    val children: List<MarkdownElement>,
    val order: Int? = null // Null for unordered list items
) : MarkdownElement
