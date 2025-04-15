package com.byteflipper.markdown_compose.renderer

import com.byteflipper.markdown_compose.model.ir.*

/**
 * Generic interface for rendering a Markdown Intermediate Representation (IR) tree
 * into a specific target output type `T`.
 *
 * Implementations of this interface will handle the conversion of each
 * MarkdownElement type into the desired output format (e.g., Composable functions,
 * HTML strings, PDF elements).
 *
 * @param T The target output type for the rendering process.
 */
interface MarkdownRenderer<T> {

    /** Renders the entire Markdown document. */
    fun renderDocument(document: MarkdownDocument): T

    /** Renders a header element. */
    fun renderHeader(header: HeaderElement): T

    /** Renders a plain text element. */
    fun renderText(text: MarkdownTextElement): T

    /** Renders a bold text element. */
    fun renderBold(bold: BoldElement): T

    /** Renders an italic text element. */
    fun renderItalic(italic: ItalicElement): T

    /** Renders a strikethrough text element. */
    fun renderStrikethrough(strikethrough: StrikethroughElement): T

    /** Renders an inline or block code element. */
    fun renderCode(code: CodeElement): T

    /** Renders a link element. */
    fun renderLink(link: LinkElement): T

    /** Renders an image element. */
    fun renderImage(image: ImageElement): T

    /** Renders an image link element. */
    fun renderImageLink(imageLink: ImageLinkElement): T

    /** Renders a block quote element. */
    fun renderBlockQuote(blockQuote: BlockQuoteElement): T

    /** Renders a list element (ordered or unordered). */
    fun renderList(list: ListElement): T

    /** Renders a list item element. */
    fun renderListItem(listItem: ListItemElement): T

    /** Renders a task list item element. */
    fun renderTaskListItem(taskListItem: TaskListItemElement): T // Might be handled within renderListItem

    /** Renders a table element. */
    fun renderTable(table: TableElement): T

    /** Renders a table row element. */
    fun renderTableRow(tableRow: TableRowElement): T // Might be handled within renderTable

    /** Renders a table cell element. */
    fun renderTableCell(tableCell: TableCellElement): T // Might be handled within renderTableRow

    /** Renders a horizontal rule element. */
    fun renderHorizontalRule(horizontalRule: HorizontalRuleElement): T

    /** Renders a line break element. */
    fun renderLineBreak(lineBreak: LineBreakElement): T

    /** Renders a footnote reference element. */
    fun renderFootnoteReference(footnoteRef: FootnoteReferenceElement): T

    /** Renders a footnote definition element (often handled separately, e.g., at the end). */
    fun renderFootnoteDefinition(footnoteDef: FootnoteDefinitionElement): T // Might return Unit or be handled differently

    /** Renders a definition list element. */
    fun renderDefinitionList(definitionList: DefinitionListElement): T

    /** Renders a definition term element. */
    fun renderDefinitionTerm(definitionTerm: DefinitionTermElement): T // Might be handled within renderDefinitionList

    /** Renders a definition details element. */
    fun renderDefinitionDetails(definitionDetails: DefinitionDetailsElement): T // Might be handled within renderDefinitionList

    // --- Helper or Generic Rendering Method ---

    /**
     * Generic method to render a list of child elements.
     * This is often used internally by render methods for container elements
     * (like Bold, Italic, ListItem, etc.).
     * The exact return type might vary based on the implementation (e.g., List<T>, Unit).
     */
    fun renderChildren(children: List<MarkdownElement>) // Return type depends on T and implementation

    /**
     * Generic method to render a single element. Can be used for dispatching.
     */
    fun renderElement(element: MarkdownElement): T {
        return when (element) {
            is MarkdownDocument -> renderDocument(element)
            is HeaderElement -> renderHeader(element)
            is MarkdownTextElement -> renderText(element)
            is BoldElement -> renderBold(element)
            is ItalicElement -> renderItalic(element)
            is StrikethroughElement -> renderStrikethrough(element)
            is CodeElement -> renderCode(element)
            is LinkElement -> renderLink(element)
            is ImageElement -> renderImage(element)
            is ImageLinkElement -> renderImageLink(element)
            is BlockQuoteElement -> renderBlockQuote(element)
            is ListElement -> renderList(element)
            is ListItemElement -> renderListItem(element)
            is TaskListItemElement -> renderTaskListItem(element)
            is TableElement -> renderTable(element)
            is TableRowElement -> renderTableRow(element)
            is TableCellElement -> renderTableCell(element)
            is HorizontalRuleElement -> renderHorizontalRule(element)
            is LineBreakElement -> renderLineBreak(element)
            is FootnoteReferenceElement -> renderFootnoteReference(element)
            is FootnoteDefinitionElement -> renderFootnoteDefinition(element)
            is DefinitionListElement -> renderDefinitionList(element)
            is DefinitionTermElement -> renderDefinitionTerm(element)
            is DefinitionDetailsElement -> renderDefinitionDetails(element)
            // Add cases for any other MarkdownElement subtypes
            else -> throw IllegalArgumentException("Unsupported MarkdownElement type: ${element::class.simpleName}")
        }
    }
}
