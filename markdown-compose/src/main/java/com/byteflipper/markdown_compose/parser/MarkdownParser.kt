// File: markdown-compose/src/main/java/com/byteflipper/markdown_compose/parser/MarkdownParser.kt
package com.byteflipper.markdown_compose.parser

import android.util.Log
import com.byteflipper.markdown_compose.model.ir.MarkdownDocument
import com.byteflipper.markdown_compose.model.ir.MarkdownElement
import com.byteflipper.markdown_compose.model.ir.MarkdownTextElement
// Import FootnoteDefinitionElement if BlockParser returns it, adjust as needed
// import com.byteflipper.markdown_compose.model.ir.FootnoteDefinitionElement

private const val TAG = "MarkdownParser"

/**
 * Object responsible for parsing Markdown input and converting it into a MarkdownDocument
 * representing the Intermediate Representation (IR) of the document.
 * It uses BlockParser to handle block-level elements.
 */
object MarkdownParser {

    // TODO: Define a result class for BlockParser if needed, e.g.:
    // data class ParseResult(val elements: List<MarkdownElement>, val definitions: Map<String, FootnoteDefinitionElement>)

    /**
     * Parses the provided Markdown input and returns a MarkdownDocument representing
     * the parsed structure. Footnote definitions are extracted but stored separately
     * (handling deferred to renderer).
     * In case of an error, it logs the error and returns a document containing the input as plain text.
     *
     * @param input The raw Markdown input as a string.
     * @return A MarkdownDocument object.
     */
    fun parse(input: String): MarkdownDocument {
        return try {
            Log.d(TAG, "Starting markdown parsing for IR")
            // BlockParser needs to be updated to return List<MarkdownElement>
            // and potentially Map<String, FootnoteDefinitionElement>
            // For now, assume it returns the list of top-level elements.
            // val parseResult = BlockParser.parseBlocks(input) // Assuming BlockParser is updated
            // val elements = parseResult.elements
            val parseResult = BlockParser.parseBlocks(input) // Returns BlockParseResult
            val elements = parseResult.elements // Extract the elements list
            // val definitions = parseResult.definitions // Definitions are handled by the renderer

            // Footnote definitions are now collected by the renderer during the renderDocument phase,
            // so no need to add a special block here.

            Log.d(TAG, "Completed markdown parsing successfully. Root elements: ${elements.size}")
            MarkdownDocument(children = elements)
        } catch (e: Exception) {
            // In case of an error, log the exception and return the input as a single text element.
            Log.e(TAG, "Error parsing markdown into IR: ${e.message}", e)
            MarkdownDocument(children = listOf(MarkdownTextElement(input))) // Fallback
        }
    }
}
