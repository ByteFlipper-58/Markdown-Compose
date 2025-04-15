package com.byteflipper.markdown_compose.model.ir

/**
 * Represents a table block in the Markdown IR.
 *
 * @property rows The list of [TableRowElement]s in the table (including the header row if present).
 * @property columnAlignments The alignment specification for each column.
 */
data class TableElement(
    val rows: List<TableRowElement>,
    val columnAlignments: List<ColumnAlignment>
) : MarkdownElement
