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

    private val unorderedListRegex = Regex("""^(\s*)(?:[-*+•])\s+(.*)""")
    private val orderedListRegex = Regex("""^(\s*)(\d+)\.\s+(.*)""")
    internal const val INPUT_SPACES_PER_LEVEL = 2


    /**
     * Parses the input Markdown string into a list of MarkdownNode objects.
     * @param input The Markdown text to parse.
     * @return A list of parsed MarkdownNode elements.
     */
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun parseBlocks(input: String): List<MarkdownNode> {
        val nodes = mutableListOf<MarkdownNode>()
        val lines = input.lines()

        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            var consumedLines = 1

            if (i + 1 < lines.size) {
                val potentialTableLines = mutableListOf<String>()
                var lookaheadIndex = i
                while(lookaheadIndex < lines.size && lines[lookaheadIndex].trim().contains("|")) {
                    potentialTableLines.add(lines[lookaheadIndex])
                    lookaheadIndex++
                    if (lookaheadIndex > i + 20) break
                }

                if (potentialTableLines.size >= 2) {
                    val potentialTable = potentialTableLines.joinToString("\n")
                    if (tableParser.isTable(potentialTable)) {
                        Log.d(TAG, "Table detected, starting parsing from line $i")
                        var tableEndIndex = i
                        while (tableEndIndex < lines.size && lines[tableEndIndex].trim().contains("|")) {
                            tableEndIndex++
                        }
                        val tableContent = lines.subList(i, tableEndIndex).joinToString("\n")
                        try {
                            val tableNode = tableParser.parseTable(tableContent)
                            nodes.add(tableNode)
                            consumedLines = tableEndIndex - i
                            Log.d(TAG, "Added TableNode, consumed $consumedLines lines")
                            i += consumedLines
                            continue
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing detected table: ${e.message}. Content:\n$tableContent", e)
                            i += consumedLines
                            continue
                        }
                    }
                }
            }

            when {
                unorderedListRegex.matches(line) -> {
                    val match = unorderedListRegex.find(line)!!
                    val (indentation, contentStr) = match.destructured
                    val indentLevel = indentation.length
                    val inlineNodes = InlineParser.parseInline(contentStr.trim())
                    nodes.add(ListItemNode(inlineNodes, indentLevel, isOrdered = false))
                    Log.d(TAG, "Added Unordered ListItemNode (IndentSpaces: $indentLevel): $contentStr")
                }
                orderedListRegex.matches(line) -> {
                    val match = orderedListRegex.find(line)!!
                    val (indentation, orderStr, contentStr) = match.destructured
                    val indentLevel = indentation.length
                    val order = orderStr.toIntOrNull()
                    val inlineNodes = InlineParser.parseInline(contentStr.trim())
                    if (order != null) {
                        nodes.add(ListItemNode(inlineNodes, indentLevel, isOrdered = true, order = order))
                        Log.d(TAG, "Added Ordered ListItemNode (Order: $order, IndentSpaces: $indentLevel): $contentStr")
                    } else {
                        nodes.addAll(InlineParser.parseInline(line))
                        Log.w(TAG, "Failed to parse order number, treated as plain text: $line")
                    }
                }
                line.isBlank() -> {
                    if (nodes.isNotEmpty() && nodes.lastOrNull() !is LineBreakNode) {
                        nodes.add(LineBreakNode)
                        Log.d(TAG, "Added LineBreakNode")
                    }
                }
                line.startsWith("###### ") -> nodes.add(HeaderNode(InlineParser.parseInline(line.removePrefix("###### ").trim()), level = 6))
                line.startsWith("##### ") -> nodes.add(HeaderNode(InlineParser.parseInline(line.removePrefix("##### ").trim()), level = 5))
                line.startsWith("#### ") -> nodes.add(HeaderNode(InlineParser.parseInline(line.removePrefix("#### ").trim()), level = 4))
                line.startsWith("### ") -> nodes.add(HeaderNode(InlineParser.parseInline(line.removePrefix("### ").trim()), level = 3))
                line.startsWith("## ") -> nodes.add(HeaderNode(InlineParser.parseInline(line.removePrefix("## ").trim()), level = 2))
                line.startsWith("# ") -> nodes.add(HeaderNode(InlineParser.parseInline(line.removePrefix("# ").trim()), level = 1))
                line.startsWith("> ") -> {
                    val content = line.removePrefix("> ")
                    val inlineNodes = InlineParser.parseInline(content)
                    nodes.add(BlockQuoteNode(inlineNodes))
                    Log.d(TAG, "Added BlockQuoteNode: $content")
                }
                line.matches(Regex("""^\s*(---|===|\*\*\*|___)\s*$""")) -> {
                    if (nodes.lastOrNull() is LineBreakNode) {
                        nodes.removeLast()
                    }
                    nodes.add(HorizontalRuleNode)
                    Log.d(TAG, "Added HorizontalRuleNode from: $line")
                }
                line.startsWith("```") && line.endsWith("```") && line.length > 6 -> {
                    val code = line.substring(3, line.length - 3)
                    nodes.add(CodeNode(code))
                    Log.d(TAG, "Added single-line CodeNode: $code")
                }
                // TODO: Multi-line Code Block detection

                else -> {
                    val inlineNodes = InlineParser.parseInline(line)
                    if (inlineNodes.isNotEmpty()) {
                        nodes.addAll(inlineNodes)
                        Log.d(TAG, "Added plain text/inline elements: $line")
                    }
                }
            }

            i += consumedLines
        }

        if (nodes.lastOrNull() is LineBreakNode) {
            nodes.removeLast()
        }

        Log.d(TAG, "Finished parsing. Total nodes: ${nodes.size}")
        return nodes
    }
}