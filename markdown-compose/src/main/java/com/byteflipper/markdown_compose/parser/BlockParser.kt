package com.byteflipper.markdown_compose.parser

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.byteflipper.markdown_compose.model.*

private const val TAG = "BlockParser"

/**
 * Object responsible for parsing Markdown blocks into structured MarkdownNode objects.
 */
object BlockParser {
    private val tableParser = TableParser()

    private val unorderedListRegex = Regex("""^(\s*)(?:[-*+•])\s+(.*)""")
    private val orderedListRegex = Regex("""^(\s*)(\d+)\.\s+(.*)""")
    internal const val INPUT_SPACES_PER_LEVEL = 2
    private val codeBlockFenceRegex = Regex("""^\s*```(\w*)\s*$""")

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
            if (line.trim().contains("|")) {
                var potentialTable = true
                if (potentialTable) {
                    val potentialTableLines = mutableListOf<String>()
                    var lookaheadIndex = i
                    // Look ahead a reasonable number of lines for a potential table structure
                    while (lookaheadIndex < lines.size &&
                        lines[lookaheadIndex].trim().contains("|") && // Line must contain pipe
                        !codeBlockFenceRegex.matches(lines[lookaheadIndex].trim()) && // Stop if we hit a code fence
                        (lookaheadIndex - i < 20)) { // Limit lookahead depth
                        potentialTableLines.add(lines[lookaheadIndex])
                        lookaheadIndex++
                    }

                    if (potentialTableLines.size >= 2) {
                        val tableCandidate = potentialTableLines.joinToString("\n")
                        if (tableParser.isTable(tableCandidate)) {
                            Log.d(TAG, "Potential table detected starting at line $i")
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
                                Log.e(TAG, "Error parsing detected table: ${e.message}. Treating as text.", e)
                                consumedLines = 1
                            }
                        }
                    }
                }
            }

            // 2. Check for Code Blocks (```) - Highest Priority
            val codeBlockMatch = codeBlockFenceRegex.matchEntire(line.trim())
            if (codeBlockMatch != null) {
                val language = codeBlockMatch.groupValues[1].takeIf { it.isNotEmpty() }
                Log.d(TAG, "Code block start fence detected at line $i with language: $language")

                val codeContentBuilder = StringBuilder()
                var codeEndIndex = i + 1
                var foundEndFence = false
                while (codeEndIndex < lines.size) {
                    val currentCodeLine = lines[codeEndIndex]
                    if (codeBlockFenceRegex.matches(currentCodeLine.trim())) {
                        Log.d(TAG, "Code block end fence detected at line $codeEndIndex")
                        consumedLines = (codeEndIndex + 1) - i
                        foundEndFence = true
                        break
                    }
                    codeContentBuilder.append(currentCodeLine).append("\n")
                    codeEndIndex++
                }

                if (foundEndFence) {
                    val codeString = codeContentBuilder.toString().dropLastWhile { it == '\n' }
                    nodes.add(CodeNode(codeString, language = language, isBlock = true))
                    Log.d(TAG, "Added CodeNode (Block), consumed $consumedLines lines")
                    i += consumedLines
                    continue
                } else {
                    Log.w(TAG, "Code block starting at line $i did not find a closing fence. Treating as text.")
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

                // 7. Block Quote
                line.startsWith("> ") -> {
                    val content = line.removePrefix("> ")
                    val inlineNodes = InlineParser.parseInline(content)
                    nodes.add(BlockQuoteNode(inlineNodes))
                    Log.d(TAG, "Added BlockQuoteNode: $content")
                }
                line.matches(Regex("""^\s*([-*_])\s*\1\s*\1+\s*$""")) -> {
                    if (nodes.lastOrNull() is LineBreakNode) {
                        nodes.removeLast()
                        Log.d(TAG, "Removed preceding LineBreakNode before HR")
                    }
                    nodes.add(HorizontalRuleNode)
                    Log.d(TAG, "Added HorizontalRuleNode from: $line")
                }
                else -> {
                    val inlineNodes = InlineParser.parseInline(line)
                    if (inlineNodes.isNotEmpty()) {
                        nodes.addAll(inlineNodes)
                        Log.d(TAG, "Added plain text/inline elements from line: $line")
                    } else if (line.isNotEmpty()) {
                        nodes.add(TextNode(line))
                        Log.d(TAG, "Added non-empty line as raw TextNode: $line")
                    }
                }
            }
            i += consumedLines
        }

        if (nodes.lastOrNull() is LineBreakNode) {
            nodes.removeLast()
            Log.d(TAG,"Removed trailing LineBreakNode")
        }

        Log.d(TAG, "Finished parsing. Total nodes: ${nodes.size}")
        return nodes
    }
}