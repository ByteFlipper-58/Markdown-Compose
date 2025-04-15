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
    // @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM) // Removed - Not needed by underlying parsers
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

            // 8. Definition List Check (NEW - before paragraph fallback)
            val definitionListResult = parseDefinitionList(lines, i)
            if (definitionListResult != null) {
                nodes.add(definitionListResult.first)
                consumedLines = definitionListResult.second
                Log.d(TAG, "Parsed DefinitionListNode ending at line ${i + consumedLines - 1}")
                i += consumedLines
                continue
            }

            // 9. Default: Treat as Paragraph (use InlineParser)
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

    /**
     * Attempts to parse a definition list starting at the given index.
     * Syntax:
     * Term 1
     * : Definition 1a
     * : Definition 1b
     * Term 2
     * : Definition 2
     *
     * @param lines List of all lines.
     * @param startIndex The index to start checking from.
     * @return A Pair containing the DefinitionListNode and the number of lines consumed, or null if not a definition list.
     */
    private fun parseDefinitionList(lines: List<String>, startIndex: Int): Pair<DefinitionListNode, Int>? {
        Log.v(TAG, "Checking for Definition List at line $startIndex")
        val items = mutableListOf<DefinitionItemNode>()
        var currentIndex = startIndex

        while (currentIndex < lines.size) {
            val termLine = lines[currentIndex]

            // --- Check for Term Line ---
            // Must be non-blank and not start with common block markers (could be refined)
            if (termLine.isBlank() || termLine.startsWith(listOf(">", "#", "-", "*", "+", "1.", "```", "|", "[^", "    ", "\t"))) {
                Log.v(TAG, "Line $currentIndex ('$termLine') is not a valid term start. Ending definition list check.")
                break // Not a term or end of list
            }

            // --- Check for Details Line(s) ---
            var detailsIndex = currentIndex + 1
            if (detailsIndex >= lines.size || !lines[detailsIndex].trimStart().startsWith(':')) {
                Log.v(TAG, "Line ${currentIndex + 1} does not start with ':'. Not a definition list or list ended.")
                break // Next line must start with ':'
            }

            // --- Parse Term ---
            val termContent = InlineParser.parseInline(termLine.trim())
            val currentTerm = DefinitionTermNode(termContent)
            Log.d(TAG, "Potential Term found at line $currentIndex: '$termLine'")

            // --- Parse Details ---
            val currentDetails = mutableListOf<DefinitionDetailsNode>()
            while (detailsIndex < lines.size) {
                val detailsLine = lines[detailsIndex]
                val trimmedDetailsLine = detailsLine.trimStart()
                if (trimmedDetailsLine.startsWith(':')) {
                    // Remove leading ':' and optional space, then parse inline content
                    val detailsContentString = trimmedDetailsLine.substring(1).trimStart()
                    val detailsContent = InlineParser.parseInline(detailsContentString)
                    currentDetails.add(DefinitionDetailsNode(detailsContent))
                    Log.d(TAG, "Added Details line $detailsIndex: '$detailsContentString'")
                    detailsIndex++
                } else {
                    // Line doesn't start with ':', so details for this term end
                    break
                }
            }

            // --- Add Item and Update Index ---
            if (currentDetails.isNotEmpty()) {
                items.add(DefinitionItemNode(currentTerm, currentDetails.toList()))
                Log.d(TAG,"Added DefinitionItemNode. Term: '${termLine}', Details lines: ${currentDetails.size}")
                currentIndex = detailsIndex // Move main index past consumed term and details
            } else {
                // Term found but no valid details lines followed. This isn't a definition list item.
                Log.v(TAG, "Term found at line ${currentIndex}, but no valid details lines followed. Stopping list parse.")
                break
            }
        } // End while loop

        return if (items.isNotEmpty()) {
            val consumed = currentIndex - startIndex
            Log.d(TAG, "Successfully parsed DefinitionListNode with ${items.size} items, consuming $consumed lines.")
            Pair(DefinitionListNode(items), consumed)
        } else {
            Log.v(TAG, "No definition list items found starting at line $startIndex.")
            null
        }
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

    // Helper to check if a line starts with common block markers
    private fun String.startsWith(prefixes: List<String>): Boolean {
        return prefixes.any { this.trimStart().startsWith(it) }
    }
}
