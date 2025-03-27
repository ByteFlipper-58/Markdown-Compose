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
        Log.d(TAG, "--- Starting parseBlocks ---")
        val nodes = mutableListOf<MarkdownNode>()
        val lines = input.lines()

        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            var consumedLines = 1
            Log.d(TAG, "Processing line $i: \"$line\"")

            if (line.isBlank()) {
                if (nodes.isNotEmpty() && nodes.lastOrNull() !is LineBreakNode) {
                    nodes.add(LineBreakNode)
                    Log.d(TAG, "Added LineBreakNode for blank line $i.")
                } else {
                    Log.v(TAG, "Skipping redundant blank line $i.")
                }
                i++
                continue
            }

            if (line.trim().contains("|")) {
                val potentialTableLines = mutableListOf<String>()
                var lookaheadIndex = i
                while (lookaheadIndex < lines.size &&
                    lines[lookaheadIndex].trim().contains("|") &&
                    !codeBlockFenceRegex.matches(lines[lookaheadIndex].trim()) &&
                    (lookaheadIndex - i < 20)) {
                    potentialTableLines.add(lines[lookaheadIndex])
                    lookaheadIndex++
                }

                if (potentialTableLines.size >= 2) {
                    val tableCandidate = potentialTableLines.joinToString("\n")
                    if (tableParser.isTable(tableCandidate)) {
                        var tableEndIndex = i
                        while (tableEndIndex < lines.size && lines[tableEndIndex].trim().contains("|") && !codeBlockFenceRegex.matches(lines[tableEndIndex].trim())) {
                            tableEndIndex++
                        }
                        val tableContent = lines.subList(i, tableEndIndex).joinToString("\n")
                        try {
                            val tableNode = tableParser.parseTable(tableContent)
                            nodes.add(tableNode)
                            consumedLines = tableEndIndex - i
                            i += consumedLines
                            continue
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing detected table block: ${e.message}. Treating line $i as text.", e)
                        }
                    } else {
                        Log.d(TAG, "Line $i contains '|' but lookahead did not confirm table structure.")
                    }
                }
            }
            val codeBlockMatch = codeBlockFenceRegex.matchEntire(line.trim())
            if (codeBlockMatch != null) {
                val language = codeBlockMatch.groupValues[1].takeIf { it.isNotEmpty() }

                val codeContentBuilder = StringBuilder()
                var codeEndIndex = i + 1
                var foundEndFence = false
                while (codeEndIndex < lines.size) {
                    val currentCodeLine = lines[codeEndIndex]
                    if (codeBlockFenceRegex.matches(currentCodeLine.trim())) {
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
                    i += consumedLines
                    continue
                } else {
                    Log.w(TAG, "Code block starting at line $i did not find a closing fence. Treating as text.")
                }
            }
            when {
                line.startsWith("###### ") -> {
                    Log.d(TAG, "Detected H6")
                    val headerContent = line.removePrefix("###### ").trim()
                    Log.i(TAG, ">>> Calling InlineParser for H6 content: \"$headerContent\"")
                    nodes.add(HeaderNode(InlineParser.parseInline(headerContent), level = 6))
                }
                line.startsWith("##### ") -> {
                    Log.d(TAG, "Detected H5")
                    val headerContent = line.removePrefix("##### ").trim()
                    Log.i(TAG, ">>> Calling InlineParser for H5 content: \"$headerContent\"")
                    nodes.add(HeaderNode(InlineParser.parseInline(headerContent), level = 5))
                }
                line.startsWith("#### ") -> {
                    val headerContent = line.removePrefix("#### ").trim()
                    nodes.add(HeaderNode(InlineParser.parseInline(headerContent), level = 4))
                }
                line.startsWith("### ") -> {
                    val headerContent = line.removePrefix("### ").trim()
                    nodes.add(HeaderNode(InlineParser.parseInline(headerContent), level = 3))
                }
                line.startsWith("## ") -> {
                    val headerContent = line.removePrefix("## ").trim()
                    nodes.add(HeaderNode(InlineParser.parseInline(headerContent), level = 2))
                }
                line.startsWith("# ") -> {
                    val headerContent = line.removePrefix("# ").trim()
                    nodes.add(HeaderNode(InlineParser.parseInline(headerContent), level = 1))
                }

                unorderedListRegex.matches(line) -> {
                    val match = unorderedListRegex.find(line)!!
                    val (indentation, contentStr) = match.destructured
                    val indentLevel = indentation.length
                    val inlineNodes = InlineParser.parseInline(contentStr.trim())
                    nodes.add(ListItemNode(inlineNodes, indentLevel, isOrdered = false))
                }
                orderedListRegex.matches(line) -> {
                    val match = orderedListRegex.find(line)!!
                    val (indentation, orderStr, contentStr) = match.destructured
                    val indentLevel = indentation.length
                    val order = orderStr.toIntOrNull()
                    Log.d(TAG, "Detected Ordered List Item (order: $order, indentSpaces: $indentLevel)")
                    if (order != null) {
                        Log.i(TAG, ">>> Calling InlineParser for OL content: \"${contentStr.trim()}\"")
                        val inlineNodes = InlineParser.parseInline(contentStr.trim())
                        nodes.add(ListItemNode(inlineNodes, indentLevel, isOrdered = true, order = order))
                    } else {
                        Log.w(TAG, "Failed to parse order number for potential OL item. Treating line as plain text.")
                        Log.i(TAG, ">>> Calling InlineParser for plain text (failed OL): \"$line\"")
                        nodes.addAll(InlineParser.parseInline(line))
                    }
                }

                line.startsWith("> ") -> {
                    Log.d(TAG, "Detected Block Quote")
                    val content = line.removePrefix("> ")
                    Log.i(TAG, ">>> Calling InlineParser for BQ content: \"$content\"")
                    val inlineNodes = InlineParser.parseInline(content)
                    nodes.add(BlockQuoteNode(inlineNodes))
                }
                line.matches(Regex("""^\s*([-*_])\s*\1\s*\1+\s*$""")) -> {
                    Log.d(TAG, "Detected Horizontal Rule from: $line")
                    // Remove preceding LineBreakNode if it exists, as HR implies a break
                    if (nodes.lastOrNull() is LineBreakNode) {
                        Log.d(TAG, "Removing preceding LineBreakNode before HR.")
                        nodes.removeLast()
                    }
                    nodes.add(HorizontalRuleNode)
                }

                else -> {
                    Log.d(TAG, "Treating line $i as paragraph/inline content.")
                    Log.i(TAG, ">>> Calling InlineParser for paragraph line: \"$line\"")
                    val inlineNodes = InlineParser.parseInline(line)
                    if (inlineNodes.isNotEmpty()) {
                        nodes.addAll(inlineNodes)
                    } else if (line.isNotEmpty()) {
                        nodes.add(TextNode(line))
                    }
                }
            }

            i += consumedLines
        }

        if (nodes.lastOrNull() is LineBreakNode) {
            Log.d(TAG,"Removing trailing LineBreakNode.")
            nodes.removeLast()
        }

        Log.d(TAG, "--- Finished parseBlocks. Total nodes created: ${nodes.size} ---")
        return nodes
    }
}