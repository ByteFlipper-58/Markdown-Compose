package com.byteflipper.markdown_compose.parser

import android.util.Log
import com.byteflipper.markdown_compose.model.CodeNode

private const val TAG = "CodeBlockParser"

/**
 * Parses fenced code blocks (``` ```).
 */
internal object CodeBlockParser {

    private val codeBlockFenceRegex = Regex("""^\s*```(\w*)\s*$""")

    /**
     * Checks if a line starts a fenced code block.
     */
    fun isStartOfCodeBlock(line: String): Boolean {
        return codeBlockFenceRegex.matches(line.trim())
    }

    /**
     * Parses a fenced code block starting at `startIndex`.
     *
     * @param lines The list of all lines in the input.
     * @param startIndex The index of the line where the code block starts (the opening fence).
     * @return A Pair containing the parsed `CodeNode` and the number of lines consumed (including fences),
     *         or null if parsing fails (e.g., no closing fence).
     */
    fun parse(lines: List<String>, startIndex: Int): Pair<CodeNode, Int>? {
        val startLine = lines[startIndex].trim()
        val codeBlockMatch = codeBlockFenceRegex.matchEntire(startLine)
            ?: return null // Should not happen if isStartOfCodeBlock was true

        val language = codeBlockMatch.groupValues[1].takeIf { it.isNotEmpty() }
        Log.d(TAG, "Detected Code Block start at line $startIndex with language: $language")

        val codeContentBuilder = StringBuilder()
        var currentIndex = startIndex + 1
        var consumedLines: Int = 1 // Start with the opening fence line

        while (currentIndex < lines.size) {
            val currentLine = lines[currentIndex]
            consumedLines++
            if (codeBlockFenceRegex.matches(currentLine.trim())) {
                Log.d(TAG, "Found Code Block end at line $currentIndex")
                // Successfully found end fence
                val codeString = codeContentBuilder.toString().dropLastWhile { it == '\n' }
                val node = CodeNode(codeString, language = language, isBlock = true)
                return Pair(node, consumedLines)
            }
            codeContentBuilder.append(currentLine).append("\n")
            currentIndex++
        }

        // If we reach here, the closing fence was not found
        Log.w(TAG, "Code block starting at line $startIndex did not find a closing fence '```'.")
        return null // Indicate failure
    }
}