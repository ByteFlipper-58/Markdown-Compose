package com.byteflipper.markdown_compose.parser

import android.util.Log
import com.byteflipper.markdown_compose.model.ir.* // Import IR elements

private const val TAG = "TableParser"

/**
 * Responsible for parsing Markdown tables into structured TableElement objects.
 */
class TableParser {

    private val codeBlockFenceRegex = Regex("""^\s*```(\w*)\s*$""") // Added to avoid parsing code fences as tables

    /**
      * Checks if the lines starting at `startIndex` look like a table and parses them if they do.
      *
      * @param lines The list of all lines in the input.
      * @param startIndex The index of the potential header line.
      * @return A Pair containing the parsed `TableElement` and the number of lines consumed,
      *         or null if it's not a valid table start.
      */
    fun tryParseTable(lines: List<String>, startIndex: Int): Pair<TableElement, Int>? { // Return TableElement
        if (startIndex + 1 >= lines.size) return null // Need at least header + separator

        val headerLine = lines[startIndex].trim()
        val separatorLine = lines[startIndex + 1].trim()

        // Basic validation - must contain pipe, separator needs pipes and dashes
        if (!headerLine.contains("|") || !separatorLine.contains("|") || !separatorLine.contains("-")) {
            return null
        }
        // Separator line must only contain valid chars
        if (!separatorLine.all { it == '|' || it == '-' || it == ':' || it.isWhitespace() }) {
            return null
        }
        // Avoid consuming code fences
        if (codeBlockFenceRegex.matches(headerLine) || codeBlockFenceRegex.matches(separatorLine)) {
            return null
        }

        // Determine the actual end of the table block
        var tableEndIndex = startIndex + 2 // Start looking after separator
        while (tableEndIndex < lines.size &&
            lines[tableEndIndex].trim().contains("|") && // Must contain a pipe
            !codeBlockFenceRegex.matches(lines[tableEndIndex].trim()) && // Not a code fence
            !isSeparatorLine(lines[tableEndIndex].trim()) // Stop if another separator line is found (potential new table)
        ) {
            tableEndIndex++
        }

        val tableLines = lines.subList(startIndex, tableEndIndex)
        if (tableLines.size < 2) return null // Should have at least header and separator

        // Perform full parsing attempt
        return try {
            val tableElement = parseTableInternal(tableLines) // Returns TableElement
            Pair(tableElement, tableLines.size)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing detected table block starting at line $startIndex: ${e.message}")
            null // Parsing failed
        }
    }

    /** Checks if a line looks like a separator line */
    private fun isSeparatorLine(line: String): Boolean {
        return line.contains('-') &&
                line.contains('|') &&
                line.all { it == '|' || it == '-' || it == ':' || it.isWhitespace() }
    }

    /**
     * Internal parsing logic, called when we are reasonably sure we have table lines.
     */
    private fun parseTableInternal(tableLines: List<String>): TableElement { // Return TableElement
        Log.d(TAG, "Starting internal table parsing for ${tableLines.size} lines.")

        val headerLine = tableLines[0].trim()
        val separatorLine = tableLines[1].trim()

        // Determine column alignments from the separator line
        val alignments = parseColumnAlignments(separatorLine)
        Log.d(TAG, "Column alignments determined: $alignments")
        val columnCount = alignments.size

        // Parse table rows
        val rows = mutableListOf<TableRowElement>() // Use TableRowElement

        // Add table header row
        rows.add(parseRow(headerLine, columnCount, isHeader = true)) // parseRow returns TableRowElement

        // Add remaining data rows (starting from index 2 of tableLines)
        for (i in 2 until tableLines.size) {
            rows.add(parseRow(tableLines[i].trim(), columnCount, isHeader = false)) // parseRow returns TableRowElement
        }

        Log.d(TAG, "Table successfully parsed with ${rows.size} rows and $columnCount columns")
        return TableElement(rows = rows.toList(), columnAlignments = alignments) // Create TableElement
    }


    /**
     * Parses a single table row into a TableRowElement.
     * @param line The table row text (trimmed).
     * @param expectedColumnCount The number of columns determined by the header/separator.
     * @param isHeader Boolean flag indicating if the row is a header.
     * @return Parsed TableRowElement.
     */
    private fun parseRow(line: String, expectedColumnCount: Int, isHeader: Boolean): TableRowElement { // Return TableRowElement
        // Split row by '|', removing leading/trailing empty strings if pipes are at ends
        val cellContents = line.split("|")
            .drop(if (line.startsWith("|")) 1 else 0)
            .dropLast(if (line.endsWith("|")) 1 else 0)
            .map { it.trim() }

        Log.d(TAG, "Parsing row content: $cellContents (Expected columns: $expectedColumnCount)")

        val cells = mutableListOf<TableCellElement>() // Use TableCellElement
        for (i in 0 until expectedColumnCount) {
            val contentString = cellContents.getOrElse(i) { "" } // Get content or empty if row has fewer cells
            val children = InlineParser.parseInline(contentString) // InlineParser returns List<MarkdownElement>
            cells.add(TableCellElement(children = children, isHeader = isHeader)) // Create TableCellElement, pass isHeader
        }

        // If row had more cells than expected (malformed markdown?), truncate for now
        if (cellContents.size > expectedColumnCount) {
            Log.w(TAG, "Row has more cells (${cellContents.size}) than expected ($expectedColumnCount). Truncating extra cells. Line: $line")
        }

        return TableRowElement(cells = cells.toList(), isHeader = isHeader) // Create TableRowElement (Corrected from TableRowNode)
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
            val endsWithColon = separator.endsWith(":") && separator.length > 1 // Ensure ':' isn't the only char

            when {
                startsWithColon && endsWithColon -> ColumnAlignment.CENTER
                endsWithColon -> ColumnAlignment.RIGHT
                // startsWithColon -> ColumnAlignment.LEFT // Considered LEFT by default now
                else -> ColumnAlignment.LEFT // Default alignment
            }
        }
    }
}
