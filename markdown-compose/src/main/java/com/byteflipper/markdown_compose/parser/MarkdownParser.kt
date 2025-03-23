package com.byteflipper.markdown_compose.parser

import android.util.Log
import com.byteflipper.markdown_compose.model.*

private const val TAG = "MarkdownParser"

object MarkdownParser {
    fun parse(input: String): List<MarkdownNode> {
        return try {
            Log.d(TAG, "Starting markdown parsing")
            val result = BlockParser.parseBlocks(input)
            Log.d(TAG, "Completed markdown parsing successfully")
            result
        } catch (e: Exception) {
            // Log the error but return an empty list to prevent app crashes
            Log.e(TAG, "Error parsing markdown: ${e.message}", e)
            listOf(TextNode(input)) // Fallback to plain text when parsing fails
        }
    }
}