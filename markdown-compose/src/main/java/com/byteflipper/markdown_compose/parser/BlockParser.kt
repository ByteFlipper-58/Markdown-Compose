package com.byteflipper.markdown_compose.parser

import android.util.Log
import com.byteflipper.markdown_compose.model.*

private const val TAG = "BlockParser"

object BlockParser {
    fun parseBlocks(input: String): List<MarkdownNode> {
        val nodes = mutableListOf<MarkdownNode>()
        val lines = input.lines()

        for (line in lines) {
            when {
                line.isBlank() -> {
                    nodes.add(LineBreakNode)
                    Log.d(TAG, "Добавлен LineBreakNode")
                }
                line.startsWith("#### ") -> {
                    val content = line.removePrefix("#### ")
                    val inlineNodes = InlineParser.parseInline(content)
                    nodes.add(HeaderNode(inlineNodes, level = 4))
                    Log.d(TAG, "Добавлен HeaderNode(4): $content")
                }
                line.startsWith("### ") -> {
                    val content = line.removePrefix("### ")
                    val inlineNodes = InlineParser.parseInline(content)
                    nodes.add(HeaderNode(inlineNodes, level = 3))
                    Log.d(TAG, "Добавлен HeaderNode(3): $content")
                }
                line.startsWith("## ") -> {
                    val content = line.removePrefix("## ")
                    val inlineNodes = InlineParser.parseInline(content)
                    nodes.add(HeaderNode(inlineNodes, level = 2))
                    Log.d(TAG, "Добавлен HeaderNode(2): $content")
                }
                line.startsWith("# ") -> {
                    val content = line.removePrefix("# ")
                    val inlineNodes = InlineParser.parseInline(content)
                    nodes.add(HeaderNode(inlineNodes, level = 1))
                    Log.d(TAG, "Добавлен HeaderNode(1): $content")
                }
                line.startsWith("> ") -> {
                    val content = line.removePrefix("> ")
                    val inlineNodes = InlineParser.parseInline(content)
                    nodes.add(BlockQuoteNode(inlineNodes))
                    Log.d(TAG, "Добавлен BlockQuoteNode: $content")
                }
                line.startsWith("- ") -> {
                    val content = line.removePrefix("- ")
                    val inlineNodes = InlineParser.parseInline(content)
                    nodes.add(ListItemNode(inlineNodes))
                    Log.d(TAG, "Добавлен ListItemNode: $content")
                }
                line.startsWith("• ") -> {
                    val content = line.removePrefix("• ")
                    val inlineNodes = InlineParser.parseInline(content)
                    nodes.add(ListItemNode(inlineNodes))
                    Log.d(TAG, "Добавлен ListItemNode с bullet: $content")
                }
                else -> {
                    if (line.startsWith("```") && line.endsWith("```") && line.length > 6) {
                        val code = line.substring(3, line.length - 3)
                        nodes.add(CodeNode(code))
                        Log.d(TAG, "Добавлен CodeNode из строки: $code")
                    } else {
                        nodes.addAll(InlineParser.parseInline(line))
                        Log.d(TAG, "Добавлен обычный текст: $line")
                    }
                }
            }
        }

        return nodes
    }
}