package com.byteflipper.markdown_compose.parser

import android.util.Log
import com.byteflipper.markdown_compose.model.ir.* // Import new IR elements

private const val TAG = "BlockParser"

/** Contains the results of parsing blocks, including the main elements and footnote definitions. */
data class BlockParseResult(
    val elements: List<MarkdownElement>, // Changed from nodes
    val definitions: Map<String, FootnoteDefinitionElement> // Changed from FootnoteDefinitionNode
)

/**
 * Parses Markdown text into block-level nodes, coordinating with specialized parsers.
 */
object BlockParser {

    // Delegate parsers (Now updated to return MarkdownElement)
    private val tableParser = TableParser()
    private val codeBlockParser = CodeBlockParser
    private val listParser = ListParser

    // Constants
    internal const val INPUT_SPACES_PER_LEVEL = 2 // Used by ListParser

    // Simple patterns managed here
    private val horizontalRuleRegex = Regex("""^\s*([-*_])\s*\1\s*\1+\s*$""")
    private val headerPrefixRegex = Regex("""^#{1,6}\s+.*""") // Basic check for # prefix + space
    private val footnoteDefinitionRegex = Regex("""^\s*\[\^([^\]\s]+)]:\s?(.*)""") // [^id]: text (single line)
    private val codeBlockFenceRegex = Regex("""^\s*```(\w*)\s*$""") // Define regex needed by isStartOfBlock

    /**
     * Parses the input Markdown string into block-level nodes and footnote definitions.
     * @param input The Markdown text to parse.
     * @return A BlockParseResult containing the list of main elements and a map of footnote definitions.
     */
    fun parseBlocks(input: String): BlockParseResult { // Returns BlockParseResult with IR elements
        Log.d(TAG, "--- Starting parseBlocks (IR) ---")
        val elements = mutableListOf<MarkdownElement>() // Changed from nodes
        val definitions = mutableMapOf<String, FootnoteDefinitionElement>() // Changed type
        val lines = input.lines()

        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            var consumedLines = 1
            Log.d(TAG, "Processing line $i: \"$line\"")

            if (line.isBlank()) {
                // Blank lines are just separators between blocks.
                // Don't add LineBreakElement here; spacing should be handled by block renderers (e.g., paragraph padding).
                Log.v(TAG, "Skipping blank line $i (block separator).")
                i++
                continue
            }

            // --- Coordination Logic ---
            // Check block types in a specific order (e.g., multi-line before single-line)

            // 1. Table Check (Potential multi-line)
            val tableParseResult = tableParser.tryParseTable(lines, i) // Returns Pair<TableElement, Int>?
            if (tableParseResult != null) {
                elements.add(tableParseResult.first) // Add the parsed TableElement
                consumedLines = tableParseResult.second
                Log.d(TAG, "Parsed TableElement ending at line ${i + consumedLines - 1}")
                i += consumedLines
                continue // Move to next line after table
            }

            // 2. Code Block Check (Potential multi-line)
            if (codeBlockParser.isStartOfCodeBlock(line)) {
                val codeBlockParseResult = codeBlockParser.parse(lines, i) // Returns Pair<CodeElement, Int>?
                if (codeBlockParseResult != null) {
                    elements.add(codeBlockParseResult.first) // Add the parsed CodeElement
                    consumedLines = codeBlockParseResult.second
                    Log.d(TAG, "Parsed CodeElement (block) ending at line ${i + consumedLines - 1}")
                    i += consumedLines
                    continue // Move to next line after code block
                } else {
                    // Failed to parse (e.g., no closing fence), treat the first line as text below
                    Log.w(TAG, "Code block detected but failed to parse. Treating line $i as text.")
                }
            }

            // 3. Footnote Definition Check
            // TODO: Add support for multi-line footnote definitions.
            val footnoteDefElement = parseFootnoteDefinition(line) // Now returns FootnoteDefinitionElement?
            if (footnoteDefElement != null) {
                if (!definitions.containsKey(footnoteDefElement.identifier)) { // First definition wins
                    definitions[footnoteDefElement.identifier] = footnoteDefElement
                    Log.d(TAG, "Parsed FootnoteDefinitionElement for [^${footnoteDefElement.identifier}] at line $i")
                } else {
                    Log.w(TAG, "Duplicate footnote definition ignored for [^${footnoteDefElement.identifier}] at line $i")
                }
                i++ // Consume one line for definition
                continue
            }

            // 4. List Item Check (Single line per item, parsed by ListParser)
            // Note: BlockParser currently only adds individual items. Grouping into ListElement happens later or in the renderer.
            if (listParser.isStartOfListItem(line)) {
                val listItemElement = listParser.parseListItem(line) // Returns ListItemElement or TaskListItemElement
                if (listItemElement != null) {
                    elements.add(listItemElement) // Add the parsed list item element
                    Log.d(TAG, "Parsed ${listItemElement::class.simpleName} at line $i")
                    i++ // Consumes only one line for the item itself
                    continue // Move to next line
                } else {
                    Log.w(TAG, "Line started like a list item but failed parsing. Treating as text. Line: $line")
                    // Fallthrough to treat as text
                }
            }

            // 5. Header Check (Single line)
            val headerElement = parseHeader(line) // Returns HeaderElement?
            if (headerElement != null) {
                elements.add(headerElement)
                Log.d(TAG, "Parsed HeaderElement (H${headerElement.level}) at line $i")
                i++
                continue
            }

            // 6. Block Quote Check (Single line) - More complex quotes might need refinement later
            val blockQuoteElement = parseBlockQuote(line) // Returns BlockQuoteElement?
            if (blockQuoteElement != null) {
                elements.add(blockQuoteElement)
                Log.d(TAG, "Parsed BlockQuoteElement at line $i")
                i++
                continue
            }

            // 7. Horizontal Rule Check (Single line)
            val horizontalRuleElement = parseHorizontalRule(line) // Returns HorizontalRuleElement?
            if (horizontalRuleElement != null) {
                if (elements.isNotEmpty() && elements.last() is LineBreakElement) { // Check for LineBreakElement safely
                    Log.d(TAG, "Removing preceding LineBreakElement before HR.")
                    elements.removeAt(elements.lastIndex) // Use removeAt for compatibility
                }
                elements.add(horizontalRuleElement)
                Log.d(TAG, "Parsed HorizontalRuleElement at line $i")
                i++
                continue
            }

            // 8. Definition List Check (before paragraph fallback)
            val definitionListResult = parseDefinitionList(lines, i) // Returns Pair<DefinitionListElement, Int>?
            if (definitionListResult != null) {
                elements.add(definitionListResult.first)
                consumedLines = definitionListResult.second
                Log.d(TAG, "Parsed DefinitionListElement ending at line ${i + consumedLines - 1}")
                i += consumedLines
                continue
            }

            // 9. Default: Treat as Paragraph
            // Collect consecutive lines that are not blank and don't start another block type.
            Log.d(TAG, "Potential paragraph start at line $i: \"$line\"")
            val paragraphLines = mutableListOf<String>()
            var paragraphEndIndex = i
            while (paragraphEndIndex < lines.size) {
                val currentLine = lines[paragraphEndIndex]
                if (currentLine.isBlank() || isStartOfBlock(currentLine, lines, paragraphEndIndex)) {
                    // Paragraph ends here (blank line or start of new block)
                    Log.v(TAG, "Paragraph ends before line $paragraphEndIndex.")
                    break
                }
                paragraphLines.add(currentLine.trim()) // Add trimmed line to paragraph
                paragraphEndIndex++
            }

            if (paragraphLines.isNotEmpty()) {
                // Join lines with spaces (Markdown standard for line breaks within paragraphs)
                val paragraphText = paragraphLines.joinToString(" ")
                Log.d(TAG, "Parsing paragraph content (lines $i-${paragraphEndIndex - 1}): \"$paragraphText\"")
                val inlineElements = InlineParser.parseInline(paragraphText) // Parse the whole paragraph

                if (inlineElements.isNotEmpty()) {
                    elements.add(ParagraphElement(children = inlineElements)) // Add as ParagraphElement
                    Log.d(TAG, "Added ParagraphElement consuming lines $i to ${paragraphEndIndex - 1}")
                } else {
                    // Handle cases where joined paragraph text results in no inline elements (e.g., only whitespace after joining?)
                    Log.w(TAG, "Joined paragraph text resulted in empty inline elements: \"$paragraphText\"")
                }
                consumedLines = paragraphEndIndex - i // Update consumed lines count
            } else {
                // Should not happen if the initial line wasn't blank, but handle defensively
                Log.w(TAG, "Detected paragraph start at line $i but collected no lines.")
                consumedLines = 1 // Consume just the one line
            }

            i += consumedLines // Advance main loop index

        } // End while loop

        // Remove trailing blank line element if present (This check might be redundant now)
        if (elements.isNotEmpty() && elements.last() is LineBreakElement) { // Check for LineBreakElement safely
            Log.d(TAG,"Removing trailing LineBreakElement.")
            elements.removeAt(elements.lastIndex) // Use removeAt for compatibility
        }

        Log.d(TAG, "--- Finished parseBlocks (IR). Elements: ${elements.size}, Definitions: ${definitions.size} ---")
        return BlockParseResult(elements.toList(), definitions.toMap()) // Return IR result
    }

    /**
     * Helper function to check if a line indicates the start of a known block type
     * (excluding paragraph, which is the fallback).
     * This is used to determine where a paragraph ends.
     *
     * @param line The line to check.
     * @param allLines All lines of the input (needed for context checks like tables).
     * @param index The index of the current line in allLines.
     * @return True if the line starts a known block type, false otherwise.
     */
    private fun isStartOfBlock(line: String, allLines: List<String>, index: Int): Boolean {
        // Order matters - check potentially ambiguous or multi-line blocks first

        // 1. Table Check (Replicate logic from TableParser.tryParseTable start)
        val nextLine = allLines.getOrNull(index + 1)?.trim()
        if (nextLine != null) {
            val headerLine = line.trim() // Current line is potential header
            val separatorLine = nextLine
            // Use the regex defined in the object scope
            val isPotentialTable = headerLine.contains("|") &&
                    separatorLine.contains("|") &&
                    separatorLine.contains("-") &&
                    separatorLine.all { it == '|' || it == '-' || it == ':' || it.isWhitespace() } &&
                    !codeBlockFenceRegex.matches(headerLine) &&
                    !codeBlockFenceRegex.matches(separatorLine)

            if (isPotentialTable) {
                Log.v(TAG, "isStartOfBlock: Detected potential table start at line $index")
                return true
            }
        }

        // 2. Other block checks
        if (codeBlockParser.isStartOfCodeBlock(line)) return true
        if (footnoteDefinitionRegex.matches(line)) return true
        if (listParser.isStartOfListItem(line)) return true
        if (headerPrefixRegex.matches(line)) return true // Header check needs refinement if '#' can be paragraph start
        if (line.startsWith("> ")) return true // Block quote
        if (horizontalRuleRegex.matches(line)) return true
        if (isStartOfDefinitionList(allLines, index)) return true // Check definition list

        // Add checks for other block types here if they are introduced

        return false // If none of the above match, it's likely part of a paragraph
    }

    /**
     * Helper to check specifically for the start of a definition list,
     * used by isStartOfBlock. Avoids parsing the whole list just for the check.
     */
    private fun isStartOfDefinitionList(lines: List<String>, startIndex: Int): Boolean {
        val termLine = lines.getOrNull(startIndex) ?: return false
        val detailsLine = lines.getOrNull(startIndex + 1) ?: return false

        // Basic check: Term line is not blank/block marker, next line starts with ':'
        val isTermLike = !termLine.isBlank() && !termLine.startsWith(listOf(">", "#", "-", "*", "+", "1.", "```", "|", "[^", "    ", "\t"))
        val isDetailsLike = detailsLine.trimStart().startsWith(':')

        return isTermLike && isDetailsLike
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
     * @return A Pair containing the DefinitionListElement and the number of lines consumed, or null if not a definition list.
     */
    private fun parseDefinitionList(lines: List<String>, startIndex: Int): Pair<DefinitionListElement, Int>? { // Return DefinitionListElement
        Log.v(TAG, "Checking for Definition List at line $startIndex")
        val items = mutableListOf<DefinitionItemElement>() // Use DefinitionItemElement
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
            val termContent = InlineParser.parseInline(termLine.trim()) // Returns List<MarkdownElement>
            val currentTerm = DefinitionTermElement(termContent) // Create DefinitionTermElement
            Log.d(TAG, "Potential Term found at line $currentIndex: '$termLine'")

            // --- Parse Details ---
            val currentDetails = mutableListOf<DefinitionDetailsElement>() // Use DefinitionDetailsElement
            while (detailsIndex < lines.size) {
                val detailsLine = lines[detailsIndex]
                val trimmedDetailsLine = detailsLine.trimStart()
                if (trimmedDetailsLine.startsWith(':')) {
                    // Remove leading ':' and optional space, then parse inline content
                    val detailsContentString = trimmedDetailsLine.substring(1).trimStart()
                    val detailsContent = InlineParser.parseInline(detailsContentString) // Returns List<MarkdownElement>
                    currentDetails.add(DefinitionDetailsElement(detailsContent)) // Create DefinitionDetailsElement
                    Log.d(TAG, "Added Details line $detailsIndex: '$detailsContentString'")
                    detailsIndex++
                } else {
                    // Line doesn't start with ':', so details for this term end
                    break
                }
            }

            // --- Add Item and Update Index ---
            if (currentDetails.isNotEmpty()) {
                items.add(DefinitionItemElement(currentTerm, currentDetails.toList())) // Use DefinitionItemElement
                Log.d(TAG,"Added DefinitionItemElement. Term: '${termLine}', Details lines: ${currentDetails.size}")
                currentIndex = detailsIndex // Move main index past consumed term and details
            } else {
                // Term found but no valid details lines followed. This isn't a definition list item.
                Log.v(TAG, "Term found at line ${currentIndex}, but no valid details lines followed. Stopping list parse.")
                break
            }
        } // End while loop

        return if (items.isNotEmpty()) {
            val consumed = currentIndex - startIndex
            Log.d(TAG, "Successfully parsed DefinitionListElement with ${items.size} items, consuming $consumed lines.")
            Pair(DefinitionListElement(items), consumed) // Return DefinitionListElement
        } else {
            Log.v(TAG, "No definition list items found starting at line $startIndex.")
            null
        }
    }


    // --- Private Helper Functions for Simple Blocks (Updated for IR) ---

    /** Parses H1-H6 headers. Returns HeaderElement or null. */
    private fun parseHeader(line: String): HeaderElement? { // Return HeaderElement
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
        val children = InlineParser.parseInline(content) // Returns List<MarkdownElement>
        return HeaderElement(level = level, children = children) // Create HeaderElement
    }

    /** Parses simple single-line block quotes. Returns BlockQuoteElement or null. */
    private fun parseBlockQuote(line: String): BlockQuoteElement? { // Return BlockQuoteElement
        if (!line.startsWith("> ")) return null
        val content = line.removePrefix("> ")
        Log.v(TAG, "Parsing BlockQuote content: \"$content\"")
        val children = InlineParser.parseInline(content) // Returns List<MarkdownElement>
        return BlockQuoteElement(children = children) // Create BlockQuoteElement
    }

    /** Parses horizontal rules. Returns HorizontalRuleElement or null. */
    private fun parseHorizontalRule(line: String): HorizontalRuleElement? { // Return HorizontalRuleElement
        return if (horizontalRuleRegex.matches(line)) HorizontalRuleElement else null // Use object directly
    }

    /** Parses a single-line footnote definition. Returns FootnoteDefinitionElement or null. */
    private fun parseFootnoteDefinition(line: String): FootnoteDefinitionElement? { // Return FootnoteDefinitionElement
        footnoteDefinitionRegex.matchEntire(line)?.let { match ->
            val identifier = match.groupValues[1]
            val contentString = match.groupValues[2] // Content is the rest of the line
            // Parse the content string using the inline parser
            val contentElements = InlineParser.parseInline(contentString) // Returns List<MarkdownElement>
            return FootnoteDefinitionElement(identifier, contentElements) // Create FootnoteDefinitionElement
        }
        return null
    }

    // Helper to check if a line starts with common block markers
    private fun String.startsWith(prefixes: List<String>): Boolean {
        return prefixes.any { this.trimStart().startsWith(it) }
    }
}
