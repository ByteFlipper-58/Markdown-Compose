package com.byteflipper.markdown_compose.model

import androidx.compose.runtime.Immutable

/**
 * Holds pre-processed information about footnotes found in the parsed Markdown document.
 * Used by the renderer to correctly display references and definitions.
 *
 * @property orderedIdentifiers List of footnote identifiers (e.g., "1", "note", "ref") in the order
 *                              they first appear as references in the main text body.
 * @property identifierToIndexMap Map associating each footnote identifier with its sequential display
 *                                index (starting from 1). Used for rendering `[1]`, `[2]`, etc.
 * @property definitions Map associating each footnote identifier with its corresponding
 *                       [FootnoteDefinitionNode] containing the definition content.
 */
@Immutable // Assuming FootnoteDefinitionNode content is also effectively immutable after parsing
data class FootnoteInfo( // Keep internal for now, expose via MarkdownText if needed later
    val orderedIdentifiers: List<String>,
    val identifierToIndexMap: Map<String, Int>,
    val definitions: Map<String, FootnoteDefinitionNode>
)
