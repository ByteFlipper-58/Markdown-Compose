package com.byteflipper.markdown_compose.model.ir

/**
 * Represents a list block (ordered or unordered) in the Markdown IR.
 *
 * @property items The list of [ListItemElement]s within this list.
 * @property isOrdered True if the list is ordered (e.g., 1., 2.), false if unordered (e.g., *, -).
 */
data class ListElement(
    val items: List<ListItemElement>,
    val isOrdered: Boolean
) : MarkdownElement
