package com.byteflipper.markdown_compose.parser

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.byteflipper.markdown_compose.model.*

private const val TAG = "BlockParser"

/** Contains the results of parsing blocks, including the main nodes and footnote definitions. */
data class BlockParseResult(
    val nodes: List<MarkdownNode>,
    val definitions: Map<String, FootnoteDefinitionNode>
)

/**
 * Parses Markdown text into block-level nodes, coordinating with specialized parsers.
 */
object BlockParser {

    // Delegate parsers
    private val tableParser = TableParser()
    private val codeBlockParser = CodeBlockParser
    private val listParser = ListParser

    // Constants
    internal const val INPUT_SPACES_PER_LEVEL = 2 // Used by ListParser

    // Simple patterns managed here
    private val horizontalRuleRegex = Regex("""^\s*([-*_])\s*\1\s*\1+\s*$""")
    private val headerPrefixRegex = Regex("""^#{1,6}\s+.*""") // Basic check for # prefix + space
    private val footnoteDefinitionRegex = Regex("""^\s*\[\^([^\]\s]+)]:\s?(.*)""") // [^id]: text (single line)

    /**
     * Parses the input Markdown string into block-level nodes and footnote definitions.
     * @param input The Markdown text to parse.
     * @return A BlockParseResult containing the list of main nodes and a map of footnote definitions.
     */
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun parseBlocks(input: String): BlockParseResult { // Возвращаем BlockParseResult
        Log.d(TAG, "--- Starting parseBlocks ---")
        val nodes = mutableListOf<MarkdownNode>()
        val definitions = mutableMapOf<String, FootnoteDefinitionNode>() // Карта для сбора определений
        val lines = input.lines()

        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            var consumedLines = 1
            Log.d(TAG, "Processing line $i: \"$line\"")

            if (line.isBlank()) {
                // Handle blank lines (only add LineBreakNode if needed)
                if (nodes.isNotEmpty() && nodes.lastOrNull() !is LineBreakNode) {
                    nodes.add(LineBreakNode)
                    Log.d(TAG, "Added LineBreakNode for blank line $i.")
                } else {
                    Log.v(TAG, "Skipping redundant blank line $i.")
                }
                i++
                continue
            }

            // --- Coordination Logic ---
            // Check block types in a specific order (e.g., multi-line before single-line)

            // 1. Table Check (Potential multi-line)
            // Use TableParser's ability to check and parse
            val tableParseResult = tableParser.tryParseTable(lines, i)
            if (tableParseResult != null) {
                nodes.add(tableParseResult.first)
                consumedLines = tableParseResult.second
                Log.d(TAG, "Parsed TableNode ending at line ${i + consumedLines - 1}")
                i += consumedLines
                continue
            }

            // 2. Code Block Check (Potential multi-line)
            if (codeBlockParser.isStartOfCodeBlock(line)) {
                val codeBlockParseResult = codeBlockParser.parse(lines, i)
                if (codeBlockParseResult != null) {
                    nodes.add(codeBlockParseResult.first)
                    consumedLines = codeBlockParseResult.second
                    Log.d(TAG, "Parsed CodeBlockNode ending at line ${i + consumedLines - 1}")
                    i += consumedLines
                    continue
                } else {
                    // Failed to parse (e.g., no closing fence), treat the first line as text below
                    Log.w(TAG, "Code block detected but failed to parse. Treating line $i as text.")
                }
            }

            // 3. Footnote Definition Check (НОВОЕ - перед списком/цитатами)
            // TODO: Add support for multi-line footnote definitions.
            val footnoteDefNode = parseFootnoteDefinition(line)
            if (footnoteDefNode != null) {
                if (!definitions.containsKey(footnoteDefNode.identifier)) { // First definition wins
                    definitions[footnoteDefNode.identifier] = footnoteDefNode
                    Log.d(TAG, "Parsed FootnoteDefinition for [^${footnoteDefNode.identifier}] at line $i")
                } else {
                    Log.w(TAG, "Duplicate footnote definition ignored for [^${footnoteDefNode.identifier}] at line $i")
                }
                i++ // Consume one line for definition
                continue
            }

            // 4. List Item Check (Single line per item, parsed by ListParser)
            if (listParser.isStartOfListItem(line)) {
                val listItemNode = listParser.parseListItem(line)
                if (listItemNode != null) {
                    nodes.add(listItemNode)
                    Log.d(TAG, "Parsed ${listItemNode::class.simpleName} at line $i")
                    i++ // Consumes only one line
                    continue
                } else {
                    Log.w(TAG, "Line started like a list item but failed parsing. Treating as text. Line: $line")
                    // Fallthrough to treat as text
                }
            }

            // 5. Header Check (Single line)
            val headerNode = parseHeader(line)
            if (headerNode != null) {
                nodes.add(headerNode)
                Log.d(TAG, "Parsed HeaderNode (H${headerNode.level}) at line $i")
                i++
                continue
            }

            // 6. Block Quote Check (Single line) - More complex quotes might need refinement later
            val blockQuoteNode = parseBlockQuote(line)
            if (blockQuoteNode != null) {
                nodes.add(blockQuoteNode)
                Log.d(TAG, "Parsed BlockQuoteNode at line $i")
                i++
                continue
            }

            // 7. Horizontal Rule Check (Single line)
            val horizontalRuleNode = parseHorizontalRule(line)
            if (horizontalRuleNode != null) {
                if (nodes.lastOrNull() is LineBreakNode) {
                    Log.d(TAG, "Removing preceding LineBreakNode before HR.")
                    nodes.removeLast()
                }
                nodes.add(horizontalRuleNode)
                Log.d(TAG, "Parsed HorizontalRuleNode at line $i")
                i++
                continue
            }

            // 8. Default: Treat as Paragraph (use InlineParser)
            Log.d(TAG, "Treating line $i as paragraph/inline content.")
            val inlineNodes = InlineParser.parseInline(line)
            if (inlineNodes.isNotEmpty()) {
                // Combine consecutive inline nodes into larger TextNode blocks if possible? Not currently done.
                nodes.addAll(inlineNodes)
            } else if (line.isNotEmpty()) {
                // Handle cases where InlineParser might return empty for non-empty input (e.g., only whitespace?)
                Log.w(TAG, "InlineParser returned empty list for non-empty line: \"$line\". Adding as TextNode.")
                nodes.add(TextNode(line))
            }
            i++ // Consume one line
        } // End while loop

        // Remove trailing blank line node if present
        if (nodes.lastOrNull() is LineBreakNode) {
            Log.d(TAG,"Removing trailing LineBreakNode.")
            nodes.removeLast()
        }

        Log.d(TAG, "--- Finished parseBlocks. Nodes: ${nodes.size}, Definitions: ${definitions.size} ---")
        return BlockParseResult(nodes.toList(), definitions.toMap()) // Возвращаем результат
    }

    // --- Private Helper Functions for Simple Blocks ---

    /** Parses H1-H6 headers. Returns HeaderNode or null. */
    private fun parseHeader(line: String): HeaderNode? {
        if (!line.startsWith("#")) return null // Quick exit

        var level = 0
        while (level < line.length && line[level] == '#') {
            level++
        }

        if (level > 6 || level == 0 || line.length <= level || line[level] != ' ') {
            // Ensure level <= 6, has '#', and is followed by a space
            return null // Not a valid header
        }

        val content = line.substring(level + 1).trim() // +1 to skip the space
        Log.v(TAG, "Parsing H$level content: \"$content\"")
        return HeaderNode(InlineParser.parseInline(content), level)
    }

    /** Parses simple single-line block quotes. Returns BlockQuoteNode or null. */
    private fun parseBlockQuote(line: String): BlockQuoteNode? {
        if (!line.startsWith("> ")) return null
        val content = line.removePrefix("> ")
        Log.v(TAG, "Parsing BlockQuote content: \"$content\"")
        return BlockQuoteNode(InlineParser.parseInline(content))
    }

    /** Parses horizontal rules. Returns HorizontalRuleNode or null. */
    private fun parseHorizontalRule(line: String): HorizontalRuleNode? {
        return if (horizontalRuleRegex.matches(line)) HorizontalRuleNode else null
    }

    /** Parses a single-line footnote definition. Returns FootnoteDefinitionNode or null. */
    private fun parseFootnoteDefinition(line: String): FootnoteDefinitionNode? {
        footnoteDefinitionRegex.matchEntire(line)?.let { match ->
            val identifier = match.groupValues[1]
            val contentString = match.groupValues[2] // Content is the rest of the line
            // Parse the content string using the inline parser
            val contentNodes = InlineParser.parseInline(contentString)
            return FootnoteDefinitionNode(identifier, contentNodes)
        }
        return null
    }
}