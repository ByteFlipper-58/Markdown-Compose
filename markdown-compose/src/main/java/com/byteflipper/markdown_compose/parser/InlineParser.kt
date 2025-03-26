package com.byteflipper.markdown_compose.parser

import android.util.Log
import com.byteflipper.markdown_compose.model.*

private const val TAG = "InlineParser"

/**
 * Object responsible for parsing inline Markdown elements such as links, bold, italic, strikethrough and code text.
 */
object InlineParser {

    // Regex to find inline elements more robustly, considering escapes maybe later
    // For now, simplified parsing. Add regex later if needed for complex cases.

    /**
     * Parses inline Markdown elements within a given text string and returns a list of MarkdownNode objects.
     */
    fun parseInline(text: String): List<MarkdownNode> {
        val nodes = mutableListOf<MarkdownNode>()
        var currentIndex = 0
        val length = text.length
        val currentText = StringBuilder()

        while (currentIndex < length) {
            when {
                text.startsWith("`", currentIndex) -> {
                    val end = findClosingTag(text, currentIndex + 1, "`")
                    if (end != -1) {
                        flushText(currentText, nodes)
                        // Treat single `code` as CodeNode for consistency? Or new InlineCodeNode?
                        // Let's use CodeNode for now. Requires Code.render to handle inline vs block.
                        // Might need InlineCodeNode later.
                        val codeContent = text.substring(currentIndex + 1, end)
                        nodes.add(CodeNode(codeContent)) // Assuming CodeNode handles inline too for now
                        Log.d(TAG, "Added Inline CodeNode: $codeContent")
                        currentIndex = end + 1
                        continue
                    }
                }

                text.startsWith("[", currentIndex) -> {
                    val linkTextEnd = text.indexOf(']', currentIndex)
                    if (linkTextEnd != -1 && linkTextEnd + 1 < length && text[linkTextEnd + 1] == '(') {
                        val linkUrlEnd = text.indexOf(')', linkTextEnd + 2)
                        if (linkUrlEnd != -1) {
                            flushText(currentText, nodes)
                            val linkText = text.substring(currentIndex + 1, linkTextEnd)
                            val linkUrl = text.substring(linkTextEnd + 2, linkUrlEnd)
                            nodes.add(LinkNode(linkText, linkUrl))
                            Log.d(TAG, "Added LinkNode: $linkText -> $linkUrl")
                            currentIndex = linkUrlEnd + 1
                            continue
                        }
                    }
                }

                text.startsWith("**", currentIndex) && !text.startsWith("***", currentIndex) -> {
                    val end = findClosingTag(text, currentIndex + 2, "**")
                    if (end != -1) {
                        flushText(currentText, nodes)
                        nodes.add(BoldTextNode(text.substring(currentIndex + 2, end)))
                        Log.d(TAG, "Added BoldTextNode: ${text.substring(currentIndex + 2, end)}")
                        currentIndex = end + 2
                        continue
                    }
                }


                (text.startsWith("*", currentIndex) && !text.startsWith("**", currentIndex)) ||
                        (text.startsWith("_", currentIndex) && !text.startsWith("__", currentIndex)) -> {
                    val tag = text[currentIndex].toString()
                    val end = findClosingTag(text, currentIndex + 1, tag)
                    if (end != -1) {
                        flushText(currentText, nodes)
                        nodes.add(ItalicTextNode(text.substring(currentIndex + 1, end)))
                        Log.d(TAG, "Added ItalicTextNode: ${text.substring(currentIndex + 1, end)}")
                        currentIndex = end + 1
                        continue
                    }
                }


                text.startsWith("~~", currentIndex) -> {
                    flushText(currentText, nodes)
                    val end = findClosingTag(text, currentIndex + 2, "~~")
                    if (end != -1) {
                        nodes.add(StrikethroughTextNode(text.substring(currentIndex + 2, end)))
                        Log.d(TAG, "Added StrikethroughTextNode: ${text.substring(currentIndex + 2, end)}")
                        currentIndex = end + 2
                        continue
                    }
                }

                else -> {
                    currentText.append(text[currentIndex])
                    currentIndex++
                }
            }
        }

        flushText(currentText, nodes)
        return nodes
    }

    /**
     * Appends the accumulated text in `currentText` as a `TextNode` to the `nodes` list
     * and clears the `currentText` builder.
     */
    private fun flushText(currentText: StringBuilder, nodes: MutableList<MarkdownNode>) {
        if (currentText.isNotEmpty()) {
            nodes.add(TextNode(currentText.toString()))
            Log.d(TAG, "Flushed TextNode: ${currentText.toString()}")
            currentText.clear()
        }
    }

    /**
     * Finds the next occurrence of a closing `tag` starting from `startIndex`.
     * Basic implementation, doesn't handle nested tags or escapes.
     */
    private fun findClosingTag(text: String, startIndex: Int, tag: String): Int {
        return text.indexOf(tag, startIndex)
    }
}