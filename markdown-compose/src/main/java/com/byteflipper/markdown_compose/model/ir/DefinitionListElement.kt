package com.byteflipper.markdown_compose.model.ir

/**
 * Represents the entire definition list block in the Markdown IR.
 *
 * @property items The list of [DefinitionItemElement]s within this definition list.
 */
data class DefinitionListElement(
    val items: List<DefinitionItemElement>
) : MarkdownElement
