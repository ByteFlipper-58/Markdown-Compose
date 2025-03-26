// File: markdown-compose/src/main/java/com/byteflipper/markdown_compose/parser/BlockParser.kt
package com.byteflipper.markdown_compose.parser

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.byteflipper.markdown_compose.model.*

private const val TAG = "UpdatedBlockParser"

/**
 * Object responsible for parsing Markdown blocks into structured MarkdownNode objects.
 */
object BlockParser {
    private val tableParser = TableParser()

    // Regex for list items
    private val unorderedListRegex = Regex("""^(\s*)(?:[-*+•])\s+(.*)""")
    private val orderedListRegex = Regex("""^(\s*)(\d+)\.\s+(.*)""")
    // Assuming 2 spaces per indent level for calculation, adjust if standard is different (e.g., 4)
    private const val SPACES_PER_INDENT_LEVEL = 2


    /**
     * Parses the input Markdown string into a list of MarkdownNode objects.
     * @param input The Markdown text to parse.
     * @return A list of parsed MarkdownNode elements.
     */
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM) // Note: This might not be needed depending on API usage
    fun parseBlocks(input: String): List<MarkdownNode> {
        val nodes = mutableListOf<MarkdownNode>()
        val lines = input.lines()

        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            var consumedLines = 1 // Default: consume one line

            // 1. Check for Table (potentially consumes multiple lines)
            // Ensure we don't re-check if the previous block was already identified as part of a table
            if (i + 1 < lines.size) { // Need at least header + separator
                val potentialTableLines = mutableListOf<String>()
                var lookaheadIndex = i
                // Look ahead for potential table rows (containing '|')
                while(lookaheadIndex < lines.size && lines[lookaheadIndex].trim().contains("|")) {
                    potentialTableLines.add(lines[lookaheadIndex])
                    lookaheadIndex++
                    // Limit lookahead to avoid performance issues on large non-table blocks
                    if (lookaheadIndex > i + 20) break
                }

                if (potentialTableLines.size >= 2) { // Need at least header and separator
                    val potentialTable = potentialTableLines.joinToString("\n")
                    if (tableParser.isTable(potentialTable)) {
                        Log.d(TAG, "Table detected, starting parsing from line $i")

                        // Find the actual end of the table
                        var tableEndIndex = i
                        while (tableEndIndex < lines.size && lines[tableEndIndex].trim().contains("|")) {
                            tableEndIndex++
                        }

                        val tableContent = lines.subList(i, tableEndIndex).joinToString("\n")
                        try {
                            val tableNode = tableParser.parseTable(tableContent)
                            nodes.add(tableNode)
                            consumedLines = tableEndIndex - i // Update consumed lines count
                            Log.d(TAG, "Added TableNode, consumed $consumedLines lines")
                            i += consumedLines // Move index past the table
                            continue // Restart loop for the next block after the table
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing detected table: ${e.message}. Content:\n$tableContent", e)
                            // Fallback: treat lines as plain text? Or just skip the table block?
                            // For now, let's just continue parsing after the detected table block
                            i += consumedLines
                            continue
                        }
                    }
                }
            }

            // 2. Check for other block types
            when {
                // Unordered List Item
                unorderedListRegex.matches(line) -> {
                    val match = unorderedListRegex.find(line)!!
                    val (indentation, contentStr) = match.destructured
                    // Calculate indent level based on space count
                    val indentLevel = indentation.length // Using raw space count
                    val inlineNodes = InlineParser.parseInline(contentStr.trim()) // Trim content before inline parsing
                    nodes.add(ListItemNode(inlineNodes, indentLevel, isOrdered = false))
                    Log.d(TAG, "Added Unordered ListItemNode (IndentSpaces: $indentLevel): $contentStr")
                }
                // Ordered List Item
                orderedListRegex.matches(line) -> {
                    val match = orderedListRegex.find(line)!!
                    val (indentation, orderStr, contentStr) = match.destructured
                    val indentLevel = indentation.length // Raw space count
                    val order = orderStr.toIntOrNull()
                    val inlineNodes = InlineParser.parseInline(contentStr.trim()) // Trim content
                    if (order != null) {
                        nodes.add(ListItemNode(inlineNodes, indentLevel, isOrdered = true, order = order))
                        Log.d(TAG, "Added Ordered ListItemNode (Order: $order, IndentSpaces: $indentLevel): $contentStr")
                    } else {
                        // Treat as plain text if order number parsing fails
                        nodes.addAll(InlineParser.parseInline(line))
                        Log.w(TAG, "Failed to parse order number, treated as plain text: $line")
                    }
                }
                // Blank Line (potential separator)
                line.isBlank() -> {
                    // Avoid adding multiple consecutive LineBreakNodes
                    if (nodes.isNotEmpty() && nodes.lastOrNull() !is LineBreakNode) {
                        nodes.add(LineBreakNode)
                        Log.d(TAG, "Added LineBreakNode")
                    }
                    // else: Ignore consecutive blank lines
                }
                // Headers
                line.startsWith("#### ") -> {
                    val content = line.removePrefix("#### ").trim()
                    nodes.add(HeaderNode(InlineParser.parseInline(content), level = 4))
                    Log.d(TAG, "Added HeaderNode(4): $content")
                }
                line.startsWith("### ") -> {
                    val content = line.removePrefix("### ").trim()
                    nodes.add(HeaderNode(InlineParser.parseInline(content), level = 3))
                    Log.d(TAG, "Added HeaderNode(3): $content")
                }
                line.startsWith("## ") -> {
                    val content = line.removePrefix("## ").trim()
                    nodes.add(HeaderNode(InlineParser.parseInline(content), level = 2))
                    Log.d(TAG, "Added HeaderNode(2): $content")
                }
                line.startsWith("# ") -> {
                    val content = line.removePrefix("# ").trim()
                    nodes.add(HeaderNode(InlineParser.parseInline(content), level = 1))
                    Log.d(TAG, "Added HeaderNode(1): $content")
                }
                // Block Quote
                line.startsWith("> ") -> {
                    val content = line.removePrefix("> ")
                    val inlineNodes = InlineParser.parseInline(content)
                    nodes.add(BlockQuoteNode(inlineNodes))
                    Log.d(TAG, "Added BlockQuoteNode: $content")
                }
                line.matches(Regex("""^\s*(---|===|\*\*\*|___)\s*$""")) -> { // Added === for completeness
                    // Remove preceding blank line if any, as HR acts as separator
                    if (nodes.lastOrNull() is LineBreakNode) {
                        nodes.removeLast()
                    }
                    nodes.add(HorizontalRuleNode)
                    Log.d(TAG, "Added HorizontalRuleNode from: $line")
                }
                // Single-line Code Block (```code```)
                line.startsWith("```") && line.endsWith("```") && line.length > 6 -> {
                    val code = line.substring(3, line.length - 3)
                    nodes.add(CodeNode(code))
                    Log.d(TAG, "Added single-line CodeNode: $code")
                }
                // TODO: Multi-line Code Block (```\n code \n```) detection needed here

                // Default: Treat as plain text line (part of a paragraph)
                else -> {
                    // If the previous node was not a text-like node or linebreak, this might start a new paragraph.
                    // InlineParser handles splitting into TextNode, Bold, etc.
                    val inlineNodes = InlineParser.parseInline(line.trim()) // Trim leading/trailing whitespace from paragraph lines
                    if (inlineNodes.isNotEmpty()) {
                        // Check if the last node was also suitable for merging (e.g. another TextNode from previous line)
                        // Simple approach: Add directly. MarkdownText's flushTextGroup will handle grouping.
                        nodes.addAll(inlineNodes)
                        Log.d(TAG, "Added plain text/inline elements: $line")
                    }
                }
            }

            // Move to the next line
            i += consumedLines
        }

        // Post-processing: Remove trailing LineBreakNode if present
        if (nodes.lastOrNull() is LineBreakNode) {
            nodes.removeLast()
        }

        Log.d(TAG, "Finished parsing. Total nodes: ${nodes.size}")
        return nodes
    }
}