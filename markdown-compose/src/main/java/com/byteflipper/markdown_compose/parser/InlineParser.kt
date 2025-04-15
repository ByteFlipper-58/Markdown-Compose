package com.byteflipper.markdown_compose.parser

import android.util.Log
import com.byteflipper.markdown_compose.model.ir.* // Import new IR elements

private const val TAG = "InlineParser"
private const val DEBUG = false // Toggle to enable/disable verbose logging

/**
 * Object responsible for parsing inline Markdown elements such as links, bold, italic,
 * strikethrough, code spans, images, and image links, returning MarkdownElement objects.
 */
object InlineParser {

    private val footnoteRefRegex = Regex("""\[\^([^\]\s]+)]""") // [^identifier] (no spaces in identifier)

    // Characters that can be escaped in Markdown
    private const val ESCAPABLE_CHARS = "\\`*_{}[]()#+-.!"

    /**
     * Parses inline Markdown elements within a given text string and returns a list of MarkdownNode objects.
     * Handles basic nesting and ignores markdown syntax within code spans. Recursively parses content within elements like bold, italic, etc.
     *
     * @param text The raw text line or part of a line containing potential inline markdown.
     * @return A list of parsed inline MarkdownElement objects (MarkdownTextElement, BoldElement, ImageElement, etc.).
     */
    fun parseInline(text: String): List<MarkdownElement> { // Return List<MarkdownElement>
        if (DEBUG) Log.d(TAG, "--- Starting parseInline (IR) for text: \"$text\" ---")

        val elements = mutableListOf<MarkdownElement>() // Changed from nodes
        val length = text.length
        var currentIndex = 0
        val currentText = StringBuilder(length / 2) // Pre-allocate with estimated capacity

        while (currentIndex < length) {
            if (DEBUG) Log.v(TAG, "Loop at $currentIndex: '${text.substring(currentIndex)}'")

            // Try to parse a special element
            val parseResult = parseNextElement(text, currentIndex)

            if (parseResult != null) {
                // A markdown element was found, add it to the list
                if (currentText.isNotEmpty()) {
                    elements.add(MarkdownTextElement(currentText.toString())) // Changed to MarkdownTextElement
                    currentText.clear()
                }
                elements.add(parseResult.first) // Add the parsed MarkdownElement
                currentIndex = parseResult.second
            } else {
                // No special element, append to plain text
                currentText.append(text[currentIndex])
                currentIndex++
            }
        }

        // Add any remaining text
        if (currentText.isNotEmpty()) {
            elements.add(MarkdownTextElement(currentText.toString())) // Changed to MarkdownTextElement
        }

        if (DEBUG) Log.d(TAG, "--- Finished parseInline (IR). Total elements: ${elements.size} ---")
        return elements // Return List<MarkdownElement>
    }

    /**
     * Attempts to parse the next markdown element at the given position.
     * Returns the parsed element and the next index, or null if no element was found.
     */
    private fun parseNextElement(text: String, index: Int): Pair<MarkdownElement, Int>? { // Return Pair<MarkdownElement, Int>?
        if (index >= text.length) return null

        // Try each markdown element in priority order
        return when {
            // 1. Escaped character
            text[index] == '\\' && index + 1 < text.length && ESCAPABLE_CHARS.contains(text[index + 1]) -> {
                val escapedChar = text[index + 1]
                Pair(MarkdownTextElement(escapedChar.toString()), index + 2) // Changed to MarkdownTextElement
            }

            // 2. Code span
            text[index] == '`' -> tryParseCodeSpan(text, index)

            // 3. Footnote Reference (check *before* links/images)
            text[index] == '[' && text.getOrNull(index + 1) == '^' ->
                tryParseFootnoteReference(text, index) // Returns Pair<FootnoteReferenceElement, Int>?

            // 4. Link or image link
            text[index] == '[' -> tryParseLinkOrImageLink(text, index) // Returns Pair<MarkdownElement, Int>?

            // 5. Image (must check before other elements as it starts with !)
            index + 1 < text.length && text[index] == '!' && text[index + 1] == '[' ->
                tryParseImage(text, index) // Returns Pair<ImageElement, Int>?

            // 6. Strikethrough
            index + 1 < text.length && text[index] == '~' && text[index + 1] == '~' ->
                tryParseStrikethrough(text, index)

            // 7. Bold (double star or double underscore)
            index + 1 < text.length && ((text[index] == '*' && text[index + 1] == '*') ||
                    (text[index] == '_' && text[index + 1] == '_')) -> {
                val delimiter = text.substring(index, index + 2)
                tryParseEmphasis(text, index, delimiter, isBold = true) // Returns Pair<BoldElement, Int>?
            }

            // 8. Italic (single star or single underscore)
            text[index] == '*' || text[index] == '_' -> {
                val delimiter = text[index].toString()
                tryParseEmphasis(text, index, delimiter, isBold = false) // Returns Pair<ItalicElement, Int>?
            }

            // No markdown element found
            else -> null
        }
    }

    /**
     * Tries to parse an inline code span `code`.
     */
    private fun tryParseCodeSpan(text: String, startIndex: Int): Pair<CodeElement, Int>? { // Return CodeElement
        if (startIndex >= text.length || text[startIndex] != '`') return null

        var endIndex = startIndex + 1
        while (endIndex < text.length) {
            if (text[endIndex] == '`') {
                val codeContent = text.substring(startIndex + 1, endIndex).trim()
                // language is null for inline code
                return Pair(CodeElement(content = codeContent, language = null, isBlock = false), endIndex + 1) // Create CodeElement
            }
            endIndex++
        }

        // No closing backtick found
        return null
    }

    /**
     * Tries to parse an image ![alt](url)
     */
    private fun tryParseImage(text: String, startIndex: Int): Pair<ImageElement, Int>? { // Return ImageElement
        if (!text.startsWith("![", startIndex)) return null

        val altTextEnd = findMatchingBracket(text, startIndex + 1, '[', ']')
        if (altTextEnd == -1 || altTextEnd + 1 >= text.length || text[altTextEnd + 1] != '(') return null

        val urlEnd = findMatchingBracket(text, altTextEnd + 1, '(', ')')
        if (urlEnd == -1) return null

        val altText = text.substring(startIndex + 2, altTextEnd)
        val url = text.substring(altTextEnd + 2, urlEnd)

        // Skip empty URLs
        if (url.isBlank()) return null

        return Pair(ImageElement(url = url, altText = altText), urlEnd + 1) // Create ImageElement
    }

    /**
     * Tries parsing a link [text](url) or an image link [![alt](img)](url).
     */
    private fun tryParseLinkOrImageLink(text: String, startIndex: Int): Pair<MarkdownElement, Int>? { // Return MarkdownElement
        if (!text.startsWith("[", startIndex)) return null

        val contentEnd = findMatchingBracket(text, startIndex, '[', ']')
        if (contentEnd == -1 || contentEnd + 1 >= text.length || text[contentEnd + 1] != '(') return null

        val linkUrlEnd = findMatchingBracket(text, contentEnd + 1, '(', ')')
        if (linkUrlEnd == -1) return null

        val content = text.substring(startIndex + 1, contentEnd)
        val linkUrl = text.substring(contentEnd + 2, linkUrlEnd)
        val finalNextIndex = linkUrlEnd + 1

        // Check if the content is actually an image: ![...](...)
        val trimmedContent = content.trim()

        if (trimmedContent.startsWith("![")) {
            // Attempt to recognize image pattern inside link content
            val altTextEnd = findUnescapedChar(trimmedContent, 2, ']')
            if (altTextEnd != -1 && altTextEnd + 1 < trimmedContent.length &&
                trimmedContent[altTextEnd + 1] == '(' &&
                findMatchingBracket(trimmedContent, altTextEnd + 1, '(', ')') == trimmedContent.length - 1) {

                val imgAltText = trimmedContent.substring(2, altTextEnd)
                val imgUrl = trimmedContent.substring(altTextEnd + 2, trimmedContent.length - 1)

                // Create ImageLinkElement
                return Pair(ImageLinkElement(imageUrl = imgUrl, altText = imgAltText, linkUrl = linkUrl), finalNextIndex)
            }
        }

        // Default: create regular link element, parse content recursively
        val linkChildren = parseInline(content) // Recursively parse link content
        return Pair(LinkElement(url = linkUrl, children = linkChildren), finalNextIndex) // Create LinkElement
    }

    /**
     * Tries parsing strikethrough ~~text~~.
     */
    private fun tryParseStrikethrough(text: String, startIndex: Int): Pair<StrikethroughElement, Int>? { // Return StrikethroughElement
        val delimiter = "~~"
        if (!text.startsWith(delimiter, startIndex)) return null

        val end = findClosingTag(text, startIndex + delimiter.length, delimiter)
        if (end != -1) {
            val content = text.substring(startIndex + delimiter.length, end)
            val children = parseInline(content) // Recursively parse content
            return Pair(StrikethroughElement(children = children), end + delimiter.length) // Create StrikethroughElement
        }

        return null
    }

    /**
     * Tries parsing bold (**, __) or italic (*, _) emphasis.
     */
    private fun tryParseEmphasis(
        text: String,
        startIndex: Int,
        delimiter: String,
        isBold: Boolean
    ): Pair<MarkdownElement, Int>? { // Return MarkdownElement (BoldElement or ItalicElement)
        if (!text.startsWith(delimiter, startIndex)) return null

        // For italic (single char delimiter), avoid matching inside bold
        if (delimiter.length == 1 && startIndex + 1 < text.length &&
            text[startIndex + 1] == delimiter[0]) {
            return null  // Let the double delimiter rule handle it
        }

        val end = findClosingTag(text, startIndex + delimiter.length, delimiter)
        if (end != -1) {
            val content = text.substring(startIndex + delimiter.length, end)
            val children = parseInline(content) // Recursively parse content
            val element: MarkdownElement = if (isBold) {
                BoldElement(children = children) // Create BoldElement
            } else {
                ItalicElement(children = children) // Create ItalicElement
            }
            return Pair(element, end + delimiter.length)
        }

        return null
    }

    /**
     * Tries to parse a footnote reference like [^identifier].
     */
    private fun tryParseFootnoteReference(text: String, startIndex: Int): Pair<FootnoteReferenceElement, Int>? { // Return FootnoteReferenceElement
        // Match the pattern starting from the current index
        val matchResult = footnoteRefRegex.find(text, startIndex)

        // Check if the match starts exactly at our startIndex
        if (matchResult != null && matchResult.range.first == startIndex) {
            val identifier = matchResult.groupValues[1]
            val nextIndex = matchResult.range.last + 1
            if (DEBUG) Log.d(TAG, "Parsed Footnote Reference: identifier='$identifier', nextIndex=$nextIndex")
            // displayIndex is populated later by the renderer or a pre-pass
            return Pair(FootnoteReferenceElement(identifier = identifier, displayIndex = null), nextIndex) // Create FootnoteReferenceElement
        }
        if (DEBUG) Log.v(TAG, "Potential footnote ref start '[' found, but pattern mismatch at index $startIndex")
        return null // Didn't match correctly at the start index
    }


    /**
     * Finds the index of the next unescaped occurrence of a character.
     */
    private fun findUnescapedChar(text: String, startIndex: Int, targetChar: Char): Int {
        var i = startIndex
        while (i < text.length) {
            if (text[i] == '\\' && i + 1 < text.length) {
                i += 2  // Skip escaped char
            } else if (text[i] == targetChar) {
                return i
            } else {
                i++
            }
        }
        return -1
    }

    /**
     * Finds the index of the next occurrence of a closing `tag`.
     * Skips escaped characters and code spans.
     */
    private fun findClosingTag(text: String, searchStartIndex: Int, tag: String): Int {
        var i = searchStartIndex
        var inCodeSpan = false

        while (i < text.length) {
            // Handle escape character
            if (text[i] == '\\' && i + 1 < text.length) {
                i += 2  // Skip escaped char
                continue
            }

            // Handle code spans
            if (text[i] == '`') {
                inCodeSpan = !inCodeSpan
                i++
                continue
            }

            // Skip processing inside code spans
            if (inCodeSpan) {
                i++
                continue
            }

            // Check for the closing tag
            if (text.startsWith(tag, i)) {
                // For emphasis, apply validation rules
                if ((tag == "*" || tag == "_") && !isValidEmphasisDelimiter(text, i, tag)) {
                    i += tag.length
                    continue
                }

                return i
            }

            i++
        }

        return -1  // Tag not found
    }

    /**
     * Validates emphasis delimiter according to CommonMark rules (simplified).
     */
    private fun isValidEmphasisDelimiter(text: String, index: Int, delimiter: String): Boolean {
        // Simplified rule: emphasis delimiter is valid if it's not surrounded by spaces
        // (unless at start/end of text)
        val previousChar = if (index > 0) text[index - 1] else ' '
        val nextIndex = index + delimiter.length
        val nextChar = if (nextIndex < text.length) text[nextIndex] else ' '

        // Both surrounded by whitespace = invalid
        if (previousChar.isWhitespace() && nextChar.isWhitespace()) {
            return false
        }

        return true
    }

    /**
     * Finds the matching closing bracket, handling nesting, escapes, and code spans.
     */
    private fun findMatchingBracket(text: String, openingBracketIndex: Int, openChar: Char, closeChar: Char): Int {
        if (openingBracketIndex < 0 || openingBracketIndex >= text.length ||
            text[openingBracketIndex] != openChar) {
            return -1
        }

        var balance = 1  // Start with 1 for the opening bracket
        var i = openingBracketIndex + 1
        var inCodeSpan = false

        while (i < text.length) {
            // Handle escape character
            if (text[i] == '\\' && i + 1 < text.length) {
                i += 2  // Skip escaped char
                continue
            }

            // Handle code spans
            if (text[i] == '`') {
                inCodeSpan = !inCodeSpan
                i++
                continue
            }

            // Skip bracket processing inside code spans
            if (inCodeSpan) {
                i++
                continue
            }

            // Check for brackets
            if (text[i] == openChar) {
                balance++
            } else if (text[i] == closeChar) {
                balance--
                if (balance == 0) {
                    return i  // Found matching bracket
                }
            }

            i++
        }

        return -1  // No matching bracket found
    }
}
