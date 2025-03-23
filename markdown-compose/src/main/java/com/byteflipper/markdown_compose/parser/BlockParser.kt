package com.byteflipper.markdown_compose.parser

import android.util.Log
import com.byteflipper.markdown_compose.model.*

private const val TAG = "UpdatedBlockParser"

/**
 * Object responsible for parsing Markdown blocks into structured MarkdownNode objects.
 */
object BlockParser {
    private val tableParser = TableParser()

    /**
     * Parses the input Markdown string into a list of MarkdownNode objects.
     * @param input The Markdown text to parse.
     * @return A list of parsed MarkdownNode elements.
     */
    fun parseBlocks(input: String): List<MarkdownNode> {
        val nodes = mutableListOf<MarkdownNode>()
        val lines = input.lines()

        var i = 0
        while (i < lines.size) {
            val line = lines[i]

            // Check for a table block
            if (i + 2 < lines.size) {
                val potentialTable = lines.subList(i, minOf(i + 10, lines.size)).joinToString("\n")
                if (tableParser.isTable(potentialTable)) {
                    Log.d(TAG, "Table detected, starting parsing")

                    // Find the end of the table
                    var tableEndIndex = i
                    while (tableEndIndex < lines.size && lines[tableEndIndex].trim().contains("|")) {
                        tableEndIndex++
                    }

                    // Parse the table
                    val tableContent = lines.subList(i, tableEndIndex).joinToString("\n")
                    val tableNode = tableParser.parseTable(tableContent)
                    nodes.add(tableNode)

                    // Move the index past the table
                    i = tableEndIndex
                    continue
                }
            }

            // Parse other Markdown blocks
            when {
                line.isBlank() -> {
                    nodes.add(LineBreakNode)
                    Log.d(TAG, "Added LineBreakNode")
                }
                line.startsWith("#### ") -> {
                    val content = line.removePrefix("#### ")
                    val inlineNodes = InlineParser.parseInline(content)
                    nodes.add(HeaderNode(inlineNodes, level = 4))
                    Log.d(TAG, "Added HeaderNode(4): $content")
                }
                line.startsWith("### ") -> {
                    val content = line.removePrefix("### ")
                    val inlineNodes = InlineParser.parseInline(content)
                    nodes.add(HeaderNode(inlineNodes, level = 3))
                    Log.d(TAG, "Added HeaderNode(3): $content")
                }
                line.startsWith("## ") -> {
                    val content = line.removePrefix("## ")
                    val inlineNodes = InlineParser.parseInline(content)
                    nodes.add(HeaderNode(inlineNodes, level = 2))
                    Log.d(TAG, "Added HeaderNode(2): $content")
                }
                line.startsWith("# ") -> {
                    val content = line.removePrefix("# ")
                    val inlineNodes = InlineParser.parseInline(content)
                    nodes.add(HeaderNode(inlineNodes, level = 1))
                    Log.d(TAG, "Added HeaderNode(1): $content")
                }
                line.startsWith("> ") -> {
                    val content = line.removePrefix("> ")
                    val inlineNodes = InlineParser.parseInline(content)
                    nodes.add(BlockQuoteNode(inlineNodes))
                    Log.d(TAG, "Added BlockQuoteNode: $content")
                }
                line.startsWith("- ") -> {
                    val content = line.removePrefix("- ")
                    val inlineNodes = InlineParser.parseInline(content)
                    nodes.add(ListItemNode(inlineNodes))
                    Log.d(TAG, "Added ListItemNode: $content")
                }
                line.startsWith("• ") -> {
                    val content = line.removePrefix("• ")
                    val inlineNodes = InlineParser.parseInline(content)
                    nodes.add(ListItemNode(inlineNodes))
                    Log.d(TAG, "Added ListItemNode with bullet: $content")
                }
                else -> {
                    if (line.startsWith("```") && line.endsWith("```") && line.length > 6) {
                        val code = line.substring(3, line.length - 3)
                        nodes.add(CodeNode(code))
                        Log.d(TAG, "Added CodeNode from string: $code")
                    } else {
                        nodes.addAll(InlineParser.parseInline(line))
                        Log.d(TAG, "Added plain text: $line")
                    }
                }
            }

            i++
        }

        return nodes
    }
}