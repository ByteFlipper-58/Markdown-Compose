package com.byteflipper.markdown_compose.parser

import android.util.Log
import com.byteflipper.markdown_compose.model.*

private const val TAG = "InlineParser"

object InlineParser {
    fun parseInline(text: String): List<MarkdownNode> {
        val nodes = mutableListOf<MarkdownNode>()
        var currentIndex = 0
        val length = text.length
        val currentText = StringBuilder()

        while (currentIndex < length) {
            when {
                // Ссылки [Текст](URL)
                text.startsWith("[", currentIndex) -> {
                    val linkTextEnd = text.indexOf(']', currentIndex)
                    if (linkTextEnd != -1 && linkTextEnd + 1 < length && text[linkTextEnd + 1] == '(') {
                        val linkUrlEnd = text.indexOf(')', linkTextEnd + 2)
                        if (linkUrlEnd != -1) {
                            flushText(currentText, nodes)
                            val linkText = text.substring(currentIndex + 1, linkTextEnd)
                            val linkUrl = text.substring(linkTextEnd + 2, linkUrlEnd)
                            nodes.add(LinkNode(linkText, linkUrl))
                            Log.d(TAG, "Добавлена ссылка: $linkText -> $linkUrl")
                            currentIndex = linkUrlEnd + 1
                            continue
                        }
                    }
                }

                // **Жирный текст**
                text.startsWith("**", currentIndex) -> {
                    flushText(currentText, nodes)
                    val end = findClosingTag(text, currentIndex + 2, "**")
                    if (end != -1) {
                        nodes.add(BoldTextNode(text.substring(currentIndex + 2, end)))
                        Log.d(TAG, "Добавлен BoldTextNode: ${text.substring(currentIndex + 2, end)}")
                        currentIndex = end + 2
                        continue
                    }
                }

                // *Курсив*
                text.startsWith("*", currentIndex) -> {
                    flushText(currentText, nodes)
                    val end = findClosingTag(text, currentIndex + 1, "*")
                    if (end != -1) {
                        nodes.add(ItalicTextNode(text.substring(currentIndex + 1, end)))
                        Log.d(TAG, "Добавлен ItalicTextNode: ${text.substring(currentIndex + 1, end)}")
                        currentIndex = end + 1
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

    private fun flushText(currentText: StringBuilder, nodes: MutableList<MarkdownNode>) {
        if (currentText.isNotEmpty()) {
            nodes.add(TextNode(currentText.toString()))
            Log.d(TAG, "Добавлен TextNode: ${currentText.toString()}")
            currentText.clear()
        }
    }

    private fun findClosingTag(text: String, startIndex: Int, tag: String): Int {
        var i = startIndex
        while (i <= text.length - tag.length) {
            if (text.startsWith(tag, i)) return i
            i++
        }
        return -1
    }
}
