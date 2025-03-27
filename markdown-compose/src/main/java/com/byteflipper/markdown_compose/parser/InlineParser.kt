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
            val char = text[currentIndex]
            val remainingText = text.substring(currentIndex)
            Log.v(TAG, "Loop iteration: currentIndex=$currentIndex, char='$char', currentText=\"$currentText\"") // Verbose log

            if (char == '`') {
                Log.d(TAG, "Found potential opening backtick at index $currentIndex")
                val end = findClosingTag(text, currentIndex + 1, "`")
                if (end != -1) {
                    Log.d(TAG, "Found closing backtick at index $end")
                    flushText(currentText, nodes) // Flush any preceding text
                    val codeContent = text.substring(currentIndex + 1, end)
                    Log.i(TAG, ">>> Creating Inline CodeNode with content: \"$codeContent\"") // Important Log
                    nodes.add(CodeNode(codeContent, isBlock = false))
                    currentIndex = end + 1 // Move index past the closing backtick
                    Log.d(TAG, "Advanced currentIndex to $currentIndex after code block.")
                    continue
                } else {
                    Log.w(TAG, "Found opening backtick at $currentIndex but no closing backtick found. Treating as literal.")
                }
            }
            else if (char == '[' && remainingText.startsWith("[")) {
                val linkTextEnd = text.indexOf(']', currentIndex + 1)
                if (linkTextEnd != -1 && linkTextEnd + 1 < length && text[linkTextEnd + 1] == '(') {
                    val linkUrlEnd = text.indexOf(')', linkTextEnd + 2)
                    if (linkUrlEnd != -1) {
                        Log.d(TAG, "Found potential link structure.")
                        flushText(currentText, nodes)
                        val linkText = text.substring(currentIndex + 1, linkTextEnd)
                        val linkUrl = text.substring(linkTextEnd + 2, linkUrlEnd)
                        Log.i(TAG, ">>> Creating LinkNode: [$linkText]($linkUrl)")
                        nodes.add(LinkNode(linkText, linkUrl))
                        currentIndex = linkUrlEnd + 1
                        Log.d(TAG, "Advanced currentIndex to $currentIndex after link.")
                        continue
                    } else {
                        Log.d(TAG, "Found '[text](...' but missing closing parenthesis ')' for link.")
                    }
                } else {
                    Log.d(TAG, "Found '[' but not a valid link structure '[text](url)'.")
                }
            }
            else if (remainingText.startsWith("**")) {
                Log.d(TAG, "Found potential opening bold '**' at $currentIndex")
                val end = findClosingTag(text, currentIndex + 2, "**")
                if (end != -1) {
                    Log.d(TAG, "Found closing bold '**' at $end")
                    flushText(currentText, nodes)
                    val boldContent = text.substring(currentIndex + 2, end)
                    Log.i(TAG, ">>> Creating BoldTextNode (double asterisk): \"$boldContent\"")
                    nodes.add(BoldTextNode(boldContent))
                    currentIndex = end + 2
                    Log.d(TAG, "Advanced currentIndex to $currentIndex after bold.")
                    continue
                } else {
                    Log.w(TAG, "Found opening bold '**' but no closing tag found. Treating as literal.")
                }
            }
            else if (remainingText.startsWith("__")) {
                Log.d(TAG, "Found potential opening bold '__' at $currentIndex")
                val end = findClosingTag(text, currentIndex + 2, "__")
                if (end != -1) {
                    Log.d(TAG, "Found closing bold '__' at $end")
                    flushText(currentText, nodes)
                    val boldContent = text.substring(currentIndex + 2, end)
                    Log.i(TAG, ">>> Creating BoldTextNode (double underscore): \"$boldContent\"")
                    nodes.add(BoldTextNode(boldContent))
                    currentIndex = end + 2
                    Log.d(TAG, "Advanced currentIndex to $currentIndex after bold.")
                    continue
                } else {
                    Log.w(TAG, "Found opening bold '__' but no closing tag found. Treating as literal.")
                }
            }
            else if (char == '*' && !remainingText.startsWith("**")) { // Avoid matching bold
                Log.d(TAG, "Found potential opening italic '*' at $currentIndex")
                val end = findClosingTag(text, currentIndex + 1, "*")
                if (end != -1) {
                    Log.d(TAG, "Found closing italic '*' at $end")
                    flushText(currentText, nodes)
                    val italicContent = text.substring(currentIndex + 1, end)
                    Log.i(TAG, ">>> Creating ItalicTextNode (single asterisk): \"$italicContent\"")
                    nodes.add(ItalicTextNode(italicContent))
                    currentIndex = end + 1
                    Log.d(TAG, "Advanced currentIndex to $currentIndex after italic.")
                    continue
                } else {
                    Log.w(TAG, "Found opening italic '*' but no closing tag found. Treating as literal.")
                }
            }
            else if (char == '_' && !remainingText.startsWith("__")) { // Avoid matching bold
                Log.d(TAG, "Found potential opening italic '_' at $currentIndex")
                val end = findClosingTag(text, currentIndex + 1, "_")
                if (end != -1) {
                    Log.d(TAG, "Found closing italic '_' at $end")
                    flushText(currentText, nodes)
                    val italicContent = text.substring(currentIndex + 1, end)
                    Log.i(TAG, ">>> Creating ItalicTextNode (single underscore): \"$italicContent\"")
                    nodes.add(ItalicTextNode(italicContent))
                    currentIndex = end + 1
                    Log.d(TAG, "Advanced currentIndex to $currentIndex after italic.")
                    continue
                } else {
                    Log.w(TAG, "Found opening italic '_' but no closing tag found. Treating as literal.")
                }
            }

            else if (remainingText.startsWith("~~")) {
                Log.d(TAG, "Found potential opening strikethrough '~~' at $currentIndex")
                val end = findClosingTag(text, currentIndex + 2, "~~")
                if (end != -1) {
                    Log.d(TAG, "Found closing strikethrough '~~' at $end")
                    flushText(currentText, nodes)
                    val strikethroughContent = text.substring(currentIndex + 2, end)
                    Log.i(TAG, ">>> Creating StrikethroughTextNode: \"$strikethroughContent\"")
                    nodes.add(StrikethroughTextNode(strikethroughContent))
                    currentIndex = end + 2
                    Log.d(TAG, "Advanced currentIndex to $currentIndex after strikethrough.")
                    continue
                } else {
                    Log.w(TAG, "Found opening strikethrough '~~' but no closing tag found. Treating as literal.")
                }
            }

            Log.v(TAG, "Appending literal char '$char' to currentText.")
            currentText.append(char)
            currentIndex++

        }

        flushText(currentText, nodes)
        Log.d(TAG, "--- Finished parseInline. Total nodes created: ${nodes.size} ---")
        return nodes
    }

    /**
     * Appends the accumulated text in `currentText` as a `TextNode` to the `nodes` list
     * and clears the `currentText` builder. Only adds if `currentText` is not empty.
     * @param currentText The StringBuilder accumulating plain text.
     * @param nodes The list of parsed nodes to add to.
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
     * IMPORTANT: Skips matching tags if they appear within an inline code span (` `).
     * Does not handle escaped tags (e.g., \`).
     *
     * @param text The full text being parsed.
     * @param startIndex The index to start searching from (exclusive of the opening tag).
     * @param tag The closing tag string to search for (e.g., "`", "**", "*").
     * @return The index where the closing tag starts, or -1 if not found or only found within code spans.
     */
    private fun findClosingTag(text: String, startIndex: Int, tag: String): Int {
        var i = startIndex
        Log.v(TAG, "findClosingTag called: text=\"...\", startIndex=$startIndex, tag=\"$tag\"")

        if (tag == "`") {
            val nextBacktick = text.indexOf('`', startIndex)
            Log.v(TAG, "findClosingTag for '`': result=$nextBacktick")
            return nextBacktick
        }

        var inCodeSpan = false
        while (i < text.length) {
            if (text[i] == '`') {
                inCodeSpan = !inCodeSpan
                Log.v(TAG, "findClosingTag: Toggled inCodeSpan to $inCodeSpan at index $i")
                i++
                continue
            }

            if (!inCodeSpan) {
                if (text.startsWith(tag, i)) {
                    val isEscaped = i > 0 && text[i-1] == '\\'
                    if (!isEscaped) {
                        Log.v(TAG, "findClosingTag: Found non-escaped tag '$tag' at index $i")
                        return i
                    } else {
                        Log.v(TAG, "findClosingTag: Found tag '$tag' at index $i, but it was escaped.")
                     }
                }
            } else {
                Log.v(TAG, "findClosingTag: Skipping index $i because inCodeSpan is true.")
            }
            i++
        }
        Log.v(TAG, "findClosingTag: Reached end of text without finding tag '$tag'. Returning -1.")
        return -1
    }
}