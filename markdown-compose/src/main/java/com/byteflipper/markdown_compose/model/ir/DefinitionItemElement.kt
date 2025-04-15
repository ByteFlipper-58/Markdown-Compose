package com.byteflipper.markdown_compose.model.ir

/**
 * Represents a single item (term + one or more details) in a definition list in the Markdown IR.
 *
 * @property term The [DefinitionTermElement] for this item.
 * @property details A list of [DefinitionDetailsElement]s associated with the term.
 */
data class DefinitionItemElement(
    val term: DefinitionTermElement,
    val details: List<DefinitionDetailsElement>
) : MarkdownElement
