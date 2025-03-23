package com.byteflipper.markdown_compose.parser

import android.util.Log
import com.byteflipper.markdown_compose.model.*

private const val TAG = "MarkdownParser"

/**
 * Object responsible for parsing Markdown input and converting it into a list of MarkdownNode objects.
 * It uses the BlockParser to handle different block-level elements of the Markdown.
 */
object MarkdownParser {

    /**
     * Parses the provided Markdown input and returns a list of MarkdownNode objects representing
     * the parsed structure. In case of an error, it logs the error and returns the input as plain text.
     *
     * @param input The raw Markdown input as a string.
     * @return A list of parsed MarkdownNode objects.
     */
    fun parse(input: String): List<MarkdownNode> {
        return try {
            Log.d(TAG, "Starting markdown parsing")
            // Parsing the input using BlockParser to handle block-level elements.
            val result = BlockParser.parseBlocks(input)
            Log.d(TAG, "Completed markdown parsing successfully")
            result
        } catch (e: Exception) {
            // In case of an error, log the exception and return the input as plain text.
            Log.e(TAG, "Error parsing markdown: ${e.message}", e)
            listOf(TextNode(input))
        }
    }
}