// File: markdown-compose/src/main/java/com/byteflipper/markdown_compose/parser/MarkdownParser.kt
package com.byteflipper.markdown_compose.parser

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.byteflipper.markdown_compose.model.*

private const val TAG = "MarkdownParser"

/**
 * Object responsible for parsing Markdown input and converting it into a list of MarkdownNode objects.
 * It uses the BlockParser to handle different block-level elements of the Markdown.
 */
object MarkdownParser {

    /**
     * Parses the provided Markdown input and returns a list of MarkdownNode objects representing
     * the parsed structure, potentially including a FootnoteDefinitionsBlockNode at the end.
     * In case of an error, it logs the error and returns the input as plain text.
     *
     * @param input The raw Markdown input as a string.
     * @return A list of parsed MarkdownNode objects.
     */
    // @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM) // Removed - Not needed by underlying parsers
    fun parse(input: String): List<MarkdownNode> {
        return try {
            Log.d(TAG, "Starting markdown parsing")
            // Parsing the input using BlockParser to handle block-level elements and definitions.
            val parseResult = BlockParser.parseBlocks(input)
            val bodyNodes = parseResult.nodes.toMutableList()
            val definitions = parseResult.definitions

            // If definitions were found, add the special block node at the end.
            if (definitions.isNotEmpty()) {
                bodyNodes.add(FootnoteDefinitionsBlockNode(definitions))
                Log.d(TAG, "Added FootnoteDefinitionsBlockNode with ${definitions.size} definitions.")
            }

            Log.d(TAG, "Completed markdown parsing successfully. Total nodes: ${bodyNodes.size}")
            bodyNodes.toList() // Return immutable list
        } catch (e: Exception) {
            // In case of an error, log the exception and return the input as plain text.
            Log.e(TAG, "Error parsing markdown: ${e.message}", e)
            listOf(TextNode(input)) // Fallback to plain text
        }
    }
}
