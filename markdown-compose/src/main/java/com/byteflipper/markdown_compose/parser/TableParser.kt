package com.byteflipper.markdown_compose.parser

import android.util.Log
import com.byteflipper.markdown_compose.model.*

private const val TAG = "TableParser"

/**
 * Responsible for parsing Markdown tables into structured TableNode objects.
 */
class TableParser {

    /**
     * Checks if the given text represents a Markdown table.
     * @param text The input text.
     * @return True if the text is a valid table, false otherwise.
     */
    fun isTable(text: String): Boolean {
        val lines = text.lines()
        if (lines.size < 2) return false

        // Check if the lines start with '|' symbol
        val firstLine = lines[0]
        val secondLine = lines[1]

        // Basic table format validation
        return firstLine.trim().startsWith("|") &&
                secondLine.trim().startsWith("|") &&
                secondLine.contains("-") &&
                secondLine.contains("|")
    }

    /**
     * Parses a Markdown table and returns a TableNode.
     * @param text The Markdown table content.
     * @return Parsed TableNode.
     */
    fun parseTable(text: String): TableNode {
        Log.d(TAG, "Starting table parsing")
        val lines = text.lines().filter { it.isNotEmpty() }

        // Extract header and separator lines
        val headerLine = lines[0]
        val separatorLine = lines[1]

        // Determine column alignments
        val alignments = parseColumnAlignments(separatorLine)
        Log.d(TAG, "Column alignments determined: $alignments")

        // Parse table rows
        val rows = mutableListOf<TableRowNode>()

        // Add table header
        rows.add(parseRow(headerLine, alignments, true))

        // Add remaining table rows
        for (i in 2 until lines.size) {
            rows.add(parseRow(lines[i], alignments, false))
        }

        Log.d(TAG, "Table successfully parsed with ${rows.size} rows")
        return TableNode(rows, alignments)
    }

    /**
     * Parses a table row into a TableRowNode.
     * @param line The table row text.
     * @param alignments The list of column alignments.
     * @param isHeader Boolean flag indicating if the row is a header.
     * @return Parsed TableRowNode.
     */
    private fun parseRow(line: String, alignments: List<ColumnAlignment>, isHeader: Boolean): TableRowNode {
        // Split the row into individual cells based on '|'
        val cellContents = line.trim().split("|")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        Log.d(TAG, "Parsing row: $cellContents")

        // Create table cells
        val cells = mutableListOf<TableCellNode>()
        for (i in cellContents.indices) {
            val columnIndex = if (i < alignments.size) i else alignments.size - 1
            val content = InlineParser.parseInline(cellContents[i])
            cells.add(TableCellNode(content, columnIndex))
        }

        return TableRowNode(cells, isHeader)
    }

    /**
     * Determines column alignment based on the separator line.
     * @param separatorLine The separator row of the table.
     * @return List of column alignments.
     */
    private fun parseColumnAlignments(separatorLine: String): List<ColumnAlignment> {
        val separators = separatorLine.trim().split("|")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        return separators.map { separator ->
            when {
                separator.startsWith(":") && separator.endsWith(":") -> ColumnAlignment.CENTER
                separator.endsWith(":") -> ColumnAlignment.RIGHT
                else -> ColumnAlignment.LEFT
            }
        }
    }
}