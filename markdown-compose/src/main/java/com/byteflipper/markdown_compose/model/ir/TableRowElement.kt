package com.byteflipper.markdown_compose.model.ir

/**
 * Represents a single row within a table (either header or data row) in the Markdown IR.
 *
 * @property cells The list of [TableCellElement]s in this row.
 * @property isHeader Indicates if this is a header row.
 */
data class TableRowElement(
    val cells: List<TableCellElement>,
    val isHeader: Boolean = false
) : MarkdownElement
