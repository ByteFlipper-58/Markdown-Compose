package com.byteflipper.markdown_compose.model.ir

/**
 * Represents a single cell within a table row (header or data) in the Markdown IR.
 *
 * @property children The inline elements contained within the cell.
 * @property isHeader Indicates if this cell is part of a header row.
 */
data class TableCellElement(
    val children: List<MarkdownElement>,
    val isHeader: Boolean = false // Useful for styling header cells differently
) : MarkdownElement
