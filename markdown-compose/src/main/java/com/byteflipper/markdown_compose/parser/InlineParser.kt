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
        val nodes = mutableListOf<MarkdownNode>()
        var currentIndex = 0
        val length = text.length
        val currentText = StringBuilder()

        while (currentIndex < length) {
            val remainingText = text.substring(currentIndex)

            if (remainingText.startsWith("`")) {
                val end = findClosingTag(text, currentIndex + 1, "`")
                if (end != -1) {
                    flushText(currentText, nodes)
                    val codeContent = text.substring(currentIndex + 1, end)
                    nodes.add(CodeNode(codeContent, isBlock = false))
                    Log.d(TAG, "Added Inline CodeNode: $codeContent")
                    currentIndex = end + 1
                    continue
                }
            }

            if (remainingText.startsWith("[")) {
                val linkTextEnd = text.indexOf(']', currentIndex + 1)
                if (linkTextEnd != -1 && linkTextEnd + 1 < length && text[linkTextEnd + 1] == '(') {
                    val linkUrlEnd = text.indexOf(')', linkTextEnd + 2)
                    if (linkUrlEnd != -1) {
                        flushText(currentText, nodes)
                        val linkText = text.substring(currentIndex + 1, linkTextEnd)
                        val linkUrl = text.substring(linkTextEnd + 2, linkUrlEnd)
                        nodes.add(LinkNode(linkText, linkUrl))
                        Log.d(TAG, "Added LinkNode: [$linkText]($linkUrl)")
                        currentIndex = linkUrlEnd + 1
                        continue
                    }
                }
            }

            if (remainingText.startsWith("**")) {
                val end = findClosingTag(text, currentIndex + 2, "**")
                if (end != -1) {
                    flushText(currentText, nodes)
                    val boldContent = text.substring(currentIndex + 2, end)
                    nodes.add(BoldTextNode(boldContent))
                    Log.d(TAG, "Added BoldTextNode (double asterisk): $boldContent")
                    currentIndex = end + 2
                    continue
                }
            }
            if (remainingText.startsWith("__")) {
                val end = findClosingTag(text, currentIndex + 2, "__")
                if (end != -1) {
                    flushText(currentText, nodes)
                    val boldContent = text.substring(currentIndex + 2, end)
                    nodes.add(BoldTextNode(boldContent))
                    Log.d(TAG, "Added BoldTextNode (double underscore): $boldContent")
                    currentIndex = end + 2
                    continue
                }
            }

            if (remainingText.startsWith("*")) {
                val end = findClosingTag(text, currentIndex + 1, "*")
                if (end != -1) {
                    flushText(currentText, nodes)
                    val italicContent = text.substring(currentIndex + 1, end)
                    nodes.add(ItalicTextNode(italicContent))
                    Log.d(TAG, "Added ItalicTextNode (single asterisk): $italicContent")
                    currentIndex = end + 1
                    continue
                }
            }
            if (remainingText.startsWith("_")) {
                val end = findClosingTag(text, currentIndex + 1, "_")
                if (end != -1) {
                    flushText(currentText, nodes)
                    val italicContent = text.substring(currentIndex + 1, end)
                    nodes.add(ItalicTextNode(italicContent))
                    Log.d(TAG, "Added ItalicTextNode (single underscore): $italicContent")
                    currentIndex = end + 1
                    continue
                }
            }

            if (remainingText.startsWith("~~")) {
                val end = findClosingTag(text, currentIndex + 2, "~~")
                if (end != -1) {
                    flushText(currentText, nodes)
                    val strikethroughContent = text.substring(currentIndex + 2, end)
                    nodes.add(StrikethroughTextNode(strikethroughContent))
                    Log.d(TAG, "Added StrikethroughTextNode: $strikethroughContent")
                    currentIndex = end + 2
                    continue
                }
            }

            currentText.append(text[currentIndex])
            currentIndex++

        }

        flushText(currentText, nodes)
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
            nodes.add(TextNode(textToAdd))
            Log.d(TAG, "Flushed TextNode: \"$textToAdd\"")
            currentText.clear()
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
        var inCodeSpan = false
        while (i < text.length) {
            if (text[i] == '`') {
                inCodeSpan = !inCodeSpan
                i++
                continue
            }

            if (!inCodeSpan) {
                if (text.startsWith(tag, i)) {
                    // Ensure tag is not preceded by escape character (simple check)
                    val isEscaped = i > 0 && text[i-1] == '\\'
                    if (!isEscaped) {
                        return i
                    }
                }
            }
            i++
        }
        return -1
    }
}