package com.byteflipper.markdown_compose.parser

import android.util.Log
import com.byteflipper.markdown_compose.model.*

private const val TAG = "TableParser"

/**
 * Responsible for parsing Markdown tables into structured TableNode objects.
 */
class TableParser {

    /**
     * Checks if the given text potentially represents a Markdown table header and separator.
     * @param text The input text (expected to be at least 2 lines).
     * @return True if the text matches the basic table structure, false otherwise.
     */
    fun isTable(text: String): Boolean {
        val lines = text.lines().filter { it.isNotBlank() }
        if (lines.size < 2) return false

        val firstLine = lines[0].trim()
        val secondLine = lines[1].trim()

        // Header must contain at least one '|'
        // Separator must contain '|', '-', and potentially ':'
        // Separator needs same or more pipes than header potentially
        val headerPipes = firstLine.count { it == '|' }
        val separatorPipes = secondLine.count { it == '|' }
        val separatorContentValid = secondLine.all { it == '|' || it == '-' || it == ':' || it.isWhitespace() }

        return headerPipes >= 1 &&
                separatorPipes >= headerPipes && // Separator should have at least as many dividers
                secondLine.contains("-") &&
                separatorContentValid &&
                // Basic check for leading/trailing pipes, though trimming helps
                (firstLine.startsWith("|") || firstLine.endsWith("|")) &&
                (secondLine.startsWith("|") || secondLine.endsWith("|"))

    }


    /**
     * Parses a Markdown table string and returns a TableNode.
     * Assumes `isTable` has already confirmed the input is likely a table.
     * @param text The Markdown table content.
     * @return Parsed TableNode.
     * @throws IllegalArgumentException if the table format is invalid (e.g., missing separator).
     */
    fun parseTable(text: String): TableNode {
        Log.d(TAG, "Starting table parsing for:\n$text")
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() && it.contains("|") }

        if (lines.size < 2) {
            throw IllegalArgumentException("Table text must have at least a header and a separator line. Found: ${lines.size}")
        }

        // Extract header and separator lines
        val headerLine = lines[0]
        val separatorLine = lines[1]

        // Validate separator line more strictly
        if (!separatorLine.contains("-") || !separatorLine.contains("|")) {
            throw IllegalArgumentException("Invalid table separator line: $separatorLine")
        }

        // Determine column alignments from the separator line
        val alignments = parseColumnAlignments(separatorLine)
        Log.d(TAG, "Column alignments determined: $alignments")
        val columnCount = alignments.size

        // Parse table rows
        val rows = mutableListOf<TableRowNode>()

        // Add table header row
        rows.add(parseRow(headerLine, columnCount, isHeader = true))

        // Add remaining data rows
        for (i in 2 until lines.size) {
            // Allow rows with fewer cells than header - they will be rendered with empty trailing cells
            rows.add(parseRow(lines[i], columnCount, isHeader = false))
        }

        Log.d(TAG, "Table successfully parsed with ${rows.size} rows and $columnCount columns")
        return TableNode(rows, alignments)
    }


    /**
     * Parses a single table row into a TableRowNode.
     * @param line The table row text (trimmed).
     * @param expectedColumnCount The number of columns determined by the header/separator.
     * @param isHeader Boolean flag indicating if the row is a header.
     * @return Parsed TableRowNode.
     */
    private fun parseRow(line: String, expectedColumnCount: Int, isHeader: Boolean): TableRowNode {
        // Split row by '|', removing leading/trailing empty strings if pipes are at ends
        val cellContents = line.split("|")
            .drop(if (line.startsWith("|")) 1 else 0)
            .dropLast(if (line.endsWith("|")) 1 else 0)
            .map { it.trim() }

        Log.d(TAG, "Parsing row content: $cellContents (Expected columns: $expectedColumnCount)")

        val cells = mutableListOf<TableCellNode>()
        for (i in 0 until expectedColumnCount) {
            val contentString = cellContents.getOrElse(i) { "" } // Get content or empty if row has fewer cells
            val contentNodes = InlineParser.parseInline(contentString)
            cells.add(TableCellNode(contentNodes, i)) // Use index i as columnIndex
        }

        // If row had more cells than expected (malformed markdown?), truncate for now
        // Or could throw an error, but being lenient might be better
        if (cellContents.size > expectedColumnCount) {
            Log.w(TAG, "Row has more cells (${cellContents.size}) than expected ($expectedColumnCount). Truncating extra cells. Line: $line")
        }


        return TableRowNode(cells.toList(), isHeader) // Ensure immutable list
    }


    /**
     * Determines column alignment based on the separator line format.
     * @param separatorLine The separator row of the table (trimmed).
     * @return List of ColumnAlignment corresponding to each column.
     */
    private fun parseColumnAlignments(separatorLine: String): List<ColumnAlignment> {
        // Split separator by '|', removing leading/trailing empty strings
        val separators = separatorLine.split("|")
            .drop(if (separatorLine.startsWith("|")) 1 else 0)
            .dropLast(if (separatorLine.endsWith("|")) 1 else 0)
            .map { it.trim() }

        Log.d(TAG, "Parsing separators for alignment: $separators")

        return separators.map { separator ->
            val startsWithColon = separator.startsWith(":")
            val endsWithColon = separator.endsWith(":")

            when {
                startsWithColon && endsWithColon -> ColumnAlignment.CENTER
                endsWithColon -> ColumnAlignment.RIGHT
                // startsWithColon -> ColumnAlignment.LEFT (default)
                else -> ColumnAlignment.LEFT // Default alignment
            }
        }
    }
}