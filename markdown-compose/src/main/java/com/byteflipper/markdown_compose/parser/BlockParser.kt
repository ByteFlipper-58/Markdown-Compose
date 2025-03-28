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

    // Regex patterns should be private unless strictly needed elsewhere
    private val taskListRegex = Regex("""^(\s*)- \[( |x|X)]\s+(.*)""") // Match '- [ ] ' or '- [x] '
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

            // Table Check (before general markdown elements)
            if (line.trim().contains("|")) {
                // (Keep existing table parsing logic as is)
                val potentialTableLines = mutableListOf<String>()
                var lookaheadIndex = i
                // Limit lookahead to prevent excessive checking
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
                            Log.d(TAG, "Successfully parsed TableNode ending at line ${tableEndIndex - 1}")
                            i += consumedLines
                            continue
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing detected table block: ${e.message}. Treating line $i as text.", e)
                            // Fallback: If table parsing fails, treat the first line as normal text below
                        }
                    } else {
                        Log.d(TAG, "Line $i contains '|' but lookahead did not confirm table structure.")
                    }
                } else {
                    Log.d(TAG, "Line $i contains '|' but not enough lines for a potential table.")
                }
            }

            // Code Block Check
            val codeBlockMatch = codeBlockFenceRegex.matchEntire(line.trim())
            if (codeBlockMatch != null) {
                // (Keep existing code block logic as is)
                val language = codeBlockMatch.groupValues[1].takeIf { it.isNotEmpty() }
                Log.d(TAG, "Detected Code Block start at line $i with language: $language")

                val codeContentBuilder = StringBuilder()
                var codeEndIndex = i + 1
                var foundEndFence = false
                while (codeEndIndex < lines.size) {
                    val currentCodeLine = lines[codeEndIndex]
                    if (codeBlockFenceRegex.matches(currentCodeLine.trim())) {
                        consumedLines = (codeEndIndex + 1) - i
                        foundEndFence = true
                        Log.d(TAG, "Found Code Block end at line $codeEndIndex")
                        break
                    }
                    codeContentBuilder.append(currentCodeLine).append("\n")
                    codeEndIndex++
                }

                if (foundEndFence) {
                    val codeString = codeContentBuilder.toString().dropLastWhile { it == '\n' }
                    nodes.add(CodeNode(codeString, language = language, isBlock = true))
                    i += consumedLines
                    continue // Move to next element after the code block
                } else {
                    Log.w(TAG, "Code block starting at line $i did not find a closing fence '```'. Treating first line as text.")
                    // Fallback: If no end fence, treat the first ``` line as regular text below
                }
            } // End Code Block Check

            // Parse other block elements
            when {
                // Header check (using startsWith for efficiency)
                line.startsWith("#") -> {
                    when {
                        line.startsWith("###### ") -> {
                            Log.d(TAG, "Detected H6")
                            nodes.add(HeaderNode(InlineParser.parseInline(line.removePrefix("###### ").trim()), level = 6))
                        }
                        line.startsWith("##### ") -> {
                            Log.d(TAG, "Detected H5")
                            nodes.add(HeaderNode(InlineParser.parseInline(line.removePrefix("##### ").trim()), level = 5))
                        }
                        line.startsWith("#### ") -> {
                            Log.d(TAG, "Detected H4")
                            nodes.add(HeaderNode(InlineParser.parseInline(line.removePrefix("#### ").trim()), level = 4))
                        }
                        line.startsWith("### ") -> {
                            Log.d(TAG, "Detected H3")
                            nodes.add(HeaderNode(InlineParser.parseInline(line.removePrefix("### ").trim()), level = 3))
                        }
                        line.startsWith("## ") -> {
                            Log.d(TAG, "Detected H2")
                            nodes.add(HeaderNode(InlineParser.parseInline(line.removePrefix("## ").trim()), level = 2))
                        }
                        line.startsWith("# ") -> {
                            Log.d(TAG, "Detected H1")
                            nodes.add(HeaderNode(InlineParser.parseInline(line.removePrefix("# ").trim()), level = 1))
                        }
                        // Handle cases like #foo (not a header)
                        else -> {
                            Log.d(TAG, "Line starts with # but not a valid header, treating as paragraph.")
                            nodes.addAll(InlineParser.parseInline(line))
                        }
                    }
                }

                // Task List Check (Must be BEFORE unordered list check)
                taskListRegex.matches(line) -> {
                    val match = taskListRegex.find(line)!!
                    val (indentation, checkedChar, contentStr) = match.destructured
                    val indentLevel = indentation.length
                    val isChecked = checkedChar.equals("x", ignoreCase = true)
                    Log.d(TAG, "Detected Task List Item (checked: $isChecked, indentSpaces: $indentLevel)")
                    val inlineNodes = InlineParser.parseInline(contentStr.trim())
                    nodes.add(TaskListItemNode(inlineNodes, indentLevel, isChecked))
                }

                // Unordered List Check
                unorderedListRegex.matches(line) -> {
                    val match = unorderedListRegex.find(line)!!
                    val (indentation, contentStr) = match.destructured
                    val indentLevel = indentation.length
                    Log.d(TAG, "Detected Unordered List Item (indentSpaces: $indentLevel)")
                    val inlineNodes = InlineParser.parseInline(contentStr.trim())
                    nodes.add(ListItemNode(inlineNodes, indentLevel, isOrdered = false))
                }

                // Ordered List Check
                orderedListRegex.matches(line) -> {
                    val match = orderedListRegex.find(line)!!
                    val (indentation, orderStr, contentStr) = match.destructured
                    val indentLevel = indentation.length
                    val order = orderStr.toIntOrNull()
                    Log.d(TAG, "Detected Ordered List Item (order: $order, indentSpaces: $indentLevel)")
                    if (order != null) {
                        val inlineNodes = InlineParser.parseInline(contentStr.trim())
                        nodes.add(ListItemNode(inlineNodes, indentLevel, isOrdered = true, order = order))
                    } else {
                        Log.w(TAG, "Failed to parse order number for potential OL item. Treating line as plain text.")
                        nodes.addAll(InlineParser.parseInline(line))
                    }
                }

                // Block Quote Check
                line.startsWith("> ") -> {
                    Log.d(TAG, "Detected Block Quote")
                    val content = line.removePrefix("> ")
                    val inlineNodes = InlineParser.parseInline(content) // Parse content for inline elements
                    nodes.add(BlockQuoteNode(inlineNodes))
                }

                // Horizontal Rule Check
                line.matches(Regex("""^\s*([-*_])\s*\1\s*\1+\s*$""")) -> {
                    Log.d(TAG, "Detected Horizontal Rule from: $line")
                    if (nodes.lastOrNull() is LineBreakNode) {
                        Log.d(TAG, "Removing preceding LineBreakNode before HR.")
                        nodes.removeLast()
                    }
                    nodes.add(HorizontalRuleNode)
                }

                else -> {
                    Log.d(TAG, "Treating line $i as paragraph/inline content.")
                    val inlineNodes = InlineParser.parseInline(line)
                    if (inlineNodes.isNotEmpty()) {
                        nodes.addAll(inlineNodes)
                    } else if (line.isNotEmpty()) {
                        Log.w(TAG, "InlineParser returned empty list for non-empty line: \"$line\". Adding as TextNode.")
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