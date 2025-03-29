package com.byteflipper.markdown_compose.parser

import android.util.Log
import com.byteflipper.markdown_compose.model.*

private const val TAG = "InlineParser"

/**
 * Object responsible for parsing inline Markdown elements such as links, bold, italic, strikethrough, and code spans.
 */
object InlineParser {

    /**
     * Parses inline Markdown elements within a given text string and returns a list of MarkdownNode objects.
     * Handles basic nesting and ignores markdown syntax within code spans.
     *
     * @param text The raw text line or part of a line containing potential inline markdown.
     * @return A list of parsed inline MarkdownNode objects (TextNode, BoldTextNode, etc.).
     */
    fun parseInline(text: String): List<MarkdownNode> {
        Log.d(TAG, "--- Starting parseInline for text: \"$text\" ---")
        val nodes = mutableListOf<MarkdownNode>()
        var currentIndex = 0
        val length = text.length
        val currentText = StringBuilder()

        while (currentIndex < length) {
            Log.v(TAG, "Loop iteration: currentIndex=$currentIndex, remaining='${text.substring(currentIndex)}', currentText=\"$currentText\"")

            var nodeParsed = false

            // Try parsing different inline elements in order of potential precedence/complexity
            tryParseCodeSpan(text, currentIndex)?.let { (node, nextIndex) ->
                flushText(currentText, nodes)
                nodes.add(node)
                currentIndex = nextIndex
                nodeParsed = true
            } ?: tryParseLink(text, currentIndex)?.let { (node, nextIndex) ->
                flushText(currentText, nodes)
                nodes.add(node)
                currentIndex = nextIndex
                nodeParsed = true
            } ?: tryParseStrikethrough(text, currentIndex)?.let { (node, nextIndex) ->
                flushText(currentText, nodes)
                nodes.add(node)
                currentIndex = nextIndex
                nodeParsed = true
            } ?: tryParseEmphasis(text, currentIndex, "**")?.let { (node, nextIndex) -> // Bold **
                flushText(currentText, nodes)
                nodes.add(node)
                currentIndex = nextIndex
                nodeParsed = true
            } ?: tryParseEmphasis(text, currentIndex, "__")?.let { (node, nextIndex) -> // Bold __
                flushText(currentText, nodes)
                nodes.add(node)
                currentIndex = nextIndex
                nodeParsed = true
            } ?: tryParseEmphasis(text, currentIndex, "*")?.let { (node, nextIndex) -> // Italic *
                flushText(currentText, nodes)
                nodes.add(node)
                currentIndex = nextIndex
                nodeParsed = true
            } ?: tryParseEmphasis(text, currentIndex, "_")?.let { (node, nextIndex) -> // Italic _
                flushText(currentText, nodes)
                nodes.add(node)
                currentIndex = nextIndex
                nodeParsed = true
            }


            if (!nodeParsed) {
                // If no specific markdown element was parsed, append the character as plain text
                currentText.append(text[currentIndex])
                currentIndex++
            }
        } // End while loop

        flushText(currentText, nodes)
        Log.d(TAG, "--- Finished parseInline. Total nodes created: ${nodes.size} ---")
        return nodes
    }

    // --- Private Parsing Helper Functions ---

    private fun tryParseCodeSpan(text: String, startIndex: Int): Pair<CodeNode, Int>? {
        if (startIndex >= text.length || text[startIndex] != '`') return null

        val end = findClosingTag(text, startIndex + 1, "`")
        if (end != -1) {
            Log.d(TAG, "Found code span from $startIndex to $end")
            val codeContent = text.substring(startIndex + 1, end)
            val node = CodeNode(codeContent, isBlock = false)
            return Pair(node, end + 1) // New index is after closing tag
        } else {
            Log.w(TAG, "Found opening backtick at $startIndex but no closing backtick found.")
            return null
        }
    }

    private fun tryParseLink(text: String, startIndex: Int): Pair<LinkNode, Int>? {
        if (!text.startsWith("[", startIndex)) return null

        val linkTextEnd = text.indexOf(']', startIndex + 1)
        if (linkTextEnd == -1 || linkTextEnd + 1 >= text.length || text[linkTextEnd + 1] != '(') {
            //Log.d(TAG, "Found '[' at $startIndex but not followed by '](...'")
            return null // Not a link start
        }

        val linkUrlEnd = text.indexOf(')', linkTextEnd + 2)
        if (linkUrlEnd == -1) {
            Log.d(TAG, "Found '[text](...' at $startIndex but missing closing parenthesis ')' for link.")
            return null
        }

        Log.d(TAG, "Found potential link structure.")
        val linkText = text.substring(startIndex + 1, linkTextEnd)
        val linkUrl = text.substring(linkTextEnd + 2, linkUrlEnd)
        Log.i(TAG, ">>> Creating LinkNode: [$linkText]($linkUrl)")
        val node = LinkNode(linkText, linkUrl)
        return Pair(node, linkUrlEnd + 1) // New index is after closing parenthesis
    }

    private fun tryParseStrikethrough(text: String, startIndex: Int): Pair<StrikethroughTextNode, Int>? {
        val delimiter = "~~"
        if (!text.startsWith(delimiter, startIndex)) return null

        val end = findClosingTag(text, startIndex + delimiter.length, delimiter)
        if (end != -1) {
            Log.d(TAG, "Found strikethrough from $startIndex to $end")
            val content = text.substring(startIndex + delimiter.length, end)
            val node = StrikethroughTextNode(content)
            return Pair(node, end + delimiter.length)
        } else {
            Log.w(TAG, "Found opening strikethrough '$delimiter' at $startIndex but no closing tag.")
            return null
        }
    }

    /** Tries parsing bold (**, __) or italic (*, _) */
    private fun tryParseEmphasis(text: String, startIndex: Int, delimiter: String): Pair<MarkdownNode, Int>? {
        if (!text.startsWith(delimiter, startIndex)) return null

        // Avoid matching * inside ** or _ inside __ inadvertently by the caller logic (check longer delimiter first)
        if ((delimiter == "*" && text.startsWith("**", startIndex)) || (delimiter == "_" && text.startsWith("__", startIndex))) {
            return null // Let the double-delimiter check handle this
        }

        val end = findClosingTag(text, startIndex + delimiter.length, delimiter)
        if (end != -1) {
            Log.d(TAG, "Found emphasis '$delimiter' from $startIndex to $end")
            val content = text.substring(startIndex + delimiter.length, end)
            val node: MarkdownNode = when (delimiter) {
                "**", "__" -> BoldTextNode(content)
                "*", "_" -> ItalicTextNode(content)
                else -> return null // Should not happen
            }
            return Pair(node, end + delimiter.length)
        } else {
            Log.w(TAG, "Found opening emphasis '$delimiter' at $startIndex but no closing tag.")
            return null
        }
    }


    // --- Utility Functions ---

    /**
     * Appends the accumulated text in `currentText` as a `TextNode` to the `nodes` list
     * and clears the `currentText` builder. Only adds if `currentText` is not empty.
     */
    private fun flushText(currentText: StringBuilder, nodes: MutableList<MarkdownNode>) {
        if (currentText.isNotEmpty()) {
            val textToAdd = currentText.toString()
            Log.d(TAG, "Flushing TextNode with content: \"$textToAdd\"")
            nodes.add(TextNode(textToAdd))
            currentText.clear()
        } else {
            Log.v(TAG, "flushText called but currentText is empty.")
        }
    }

    /**
     * Finds the next occurrence of a closing `tag` starting from `startIndex`.
     * Skips matching tags if they appear within an inline code span (` `).
     * Does not handle escaped tags (e.g., \`).
     */
    private fun findClosingTag(text: String, startIndex: Int, tag: String): Int {
        var i = startIndex
        Log.v(TAG, "findClosingTag called: text='${text.substring(startIndex)}', startIndex=$startIndex, tag=\"$tag\"")

        // Backtick search is simple - find the very next one.
        if (tag == "`") {
            val nextBacktick = text.indexOf('`', startIndex)
            Log.v(TAG, "findClosingTag for '`': result=$nextBacktick")
            return nextBacktick
        }

        var inCodeSpan = false
        while (i < text.length) {
            if (text[i] == '`') {
                // Find closing backtick for this code span to properly skip its content
                val codeSpanEnd = text.indexOf('`', i + 1)
                if (codeSpanEnd != -1) {
                    Log.v(TAG, "findClosingTag: Entering code span at $i, skipping until $codeSpanEnd")
                    i = codeSpanEnd + 1 // Move past the closing backtick
                    continue // Continue the outer loop
                } else {
                    // Unterminated code span, stop searching for the original tag
                    Log.v(TAG, "findClosingTag: Unterminated code span found starting at $i. Aborting search for '$tag'.")
                    return -1
                }
            }

            // Check for the target tag if not inside a (skipped) code span
            if (text.startsWith(tag, i)) {
                val isEscaped = i > 0 && text[i-1] == '\\' // Basic escape check
                if (!isEscaped) {
                    Log.v(TAG, "findClosingTag: Found non-escaped tag '$tag' at index $i")
                    return i
                } else {
                    Log.v(TAG, "findClosingTag: Found tag '$tag' at index $i, but it was escaped.")
                }
            }
            i++
        } // End while

        Log.v(TAG, "findClosingTag: Reached end of text without finding tag '$tag'. Returning -1.")
        return -1
    }
}