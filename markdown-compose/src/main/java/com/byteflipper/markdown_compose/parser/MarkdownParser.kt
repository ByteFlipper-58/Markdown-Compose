package com.byteflipper.markdown_compose.parser

import com.byteflipper.markdown_compose.model.*

object MarkdownParser {
    fun parse(input: String): List<MarkdownNode> {
        val nodes = mutableListOf<MarkdownNode>()
        val lines = input.split("\n")

        for (line in lines) {
            if (line.isBlank()) {
                nodes.add(LineBreakNode)
            } else {
                when {
                    line.startsWith("# ") -> {
                        val content = parseInlineFormatting(line.removePrefix("# "))
                        nodes.add(HeaderNode(content, level = 1))
                    }
                    line.startsWith("## ") -> {
                        val content = parseInlineFormatting(line.removePrefix("## "))
                        nodes.add(HeaderNode(content, level = 2))
                    }
                    line.startsWith("### ") -> {
                        val content = parseInlineFormatting(line.removePrefix("### "))
                        nodes.add(HeaderNode(content, level = 3))
                    }
                    line.startsWith("- ") -> {
                        val content = parseInlineFormatting(line.removePrefix("- "))
                        nodes.add(ListItemNode(content))
                    }
                    else -> nodes.addAll(parseInlineFormatting(line))
                }
            }
        }
        return nodes
    }

    private fun parseInlineFormatting(text: String): List<MarkdownNode> {
        return parseRichText(text)
    }

    private fun parseRichText(text: String): List<MarkdownNode> {
        val nodes = mutableListOf<MarkdownNode>()
        var currentIndex = 0
        val length = text.length
        var currentText = StringBuilder()

        while (currentIndex < length) {
            // Handle links [text](url)
            if (currentIndex + 1 < length && text[currentIndex] == '[') {
                val linkTextEnd = text.indexOf(']', currentIndex)
                if (linkTextEnd != -1 && linkTextEnd + 1 < length && text[linkTextEnd + 1] == '(') {
                    val linkUrlEnd = text.indexOf(')', linkTextEnd + 2)
                    if (linkUrlEnd != -1) {
                        if (currentText.isNotEmpty()) {
                            nodes.add(TextNode(currentText.toString()))
                            currentText = StringBuilder()
                        }

                        val linkText = text.substring(currentIndex + 1, linkTextEnd)
                        val linkUrl = text.substring(linkTextEnd + 2, linkUrlEnd)
                        nodes.add(LinkNode(linkText, linkUrl))

                        currentIndex = linkUrlEnd + 1
                        continue
                    }
                }
            }

            // Handle bold text (**text**)
            if (currentIndex + 1 < length &&
                text[currentIndex] == '*' &&
                text[currentIndex + 1] == '*') {

                // Add any accumulated text before the formatting
                if (currentText.isNotEmpty()) {
                    nodes.add(TextNode(currentText.toString()))
                    currentText = StringBuilder()
                }

                val boldStart = currentIndex + 2
                val boldEnd = findClosingTag(text, boldStart, "**")

                if (boldEnd != -1) {
                    val boldText = text.substring(boldStart, boldEnd)
                    nodes.add(BoldTextNode(boldText))
                    currentIndex = boldEnd + 2 // Skip past the closing **
                } else {
                    // If no closing tag is found, treat it as regular text
                    currentText.append("**")
                    currentIndex += 2
                }
            }
            // Handle italic text (*text*)
            else if (text[currentIndex] == '*') {
                // Add any accumulated text before the formatting
                if (currentText.isNotEmpty()) {
                    nodes.add(TextNode(currentText.toString()))
                    currentText = StringBuilder()
                }

                val italicStart = currentIndex + 1
                val italicEnd = findClosingTag(text, italicStart, "*")

                if (italicEnd != -1) {
                    val italicText = text.substring(italicStart, italicEnd)
                    nodes.add(ItalicTextNode(italicText))
                    currentIndex = italicEnd + 1 // Skip past the closing *
                } else {
                    // If no closing tag is found, treat it as regular text
                    currentText.append('*')
                    currentIndex++
                }
            } else {
                currentText.append(text[currentIndex])
                currentIndex++
            }
        }

        // Add any remaining text
        if (currentText.isNotEmpty()) {
            nodes.add(TextNode(currentText.toString()))
        }

        return nodes
    }

    // Helper function to find closing tags while respecting nesting
    private fun findClosingTag(text: String, startIndex: Int, tag: String): Int {
        var i = startIndex
        while (i <= text.length - tag.length) {
            if (text.substring(i, i + tag.length) == tag) {
                // Found potential closing tag
                return i
            }
            i++
        }
        return -1
    }
}