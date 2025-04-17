package com.byteflipper.markdown_compose.renderer

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.byteflipper.markdown_compose.model.MarkdownStyleSheet // Keep old import for now
import com.byteflipper.markdown_compose.model.ir.*
// Import ParagraphElement explicitly if not covered by wildcard
import com.byteflipper.markdown_compose.model.ir.ParagraphElement
import com.byteflipper.markdown_compose.renderer.element.* // Import new element renderers package
// Import ParagraphElementRenderer explicitly if not covered by wildcard
import com.byteflipper.markdown_compose.renderer.element.ParagraphElementRenderer
import android.util.Log

/**
 * A MarkdownRenderer implementation that targets Jetpack Compose.
 * It converts MarkdownElement nodes into Composable functions by dispatching
 * to specific element renderers.
 *
 * @param styleSheet The styles to apply to the rendered elements.
 * @param modifier Optional modifier to apply to the root composable.
 * @param onLinkClick Callback for link clicks.
 * @param onFootnoteReferenceClick Callback for footnote reference clicks.
 * @param onTaskCheckedChange Callback for task list item check changes.
 */
class ComposeMarkdownRenderer(
    val styleSheet: MarkdownStyleSheet, // Made public for access by element renderers
    private val modifier: Modifier = Modifier,
    // Pass callbacks to the renderer instance
    val onLinkClick: ((url: String) -> Unit)? = null,
    val onFootnoteReferenceClick: ((identifier: String) -> Unit)? = null,
    val onTaskCheckedChange: ((node: TaskListItemElement, isChecked: Boolean) -> Unit)? = null
) : MarkdownRenderer<@Composable () -> Unit> {

    // Footnote handling needs careful consideration with separate renderers
    val footnoteDefinitions = mutableMapOf<String, FootnoteDefinitionElement>()
    val footnoteNumbers = mutableMapOf<String, Int>() // Map identifier to sequential number
    var footnoteCounter = 0 // Counter for assigning sequential numbers

    // Removed @Composable annotation
    override fun renderDocument(document: MarkdownDocument): @Composable () -> Unit {
        // Reset footnote state for this document render
        footnoteDefinitions.clear()
        footnoteNumbers.clear() // Reset numbers as well
        footnoteCounter = 0
        // Collect definitions first (important for reference lookup later if needed)
        document.children.filterIsInstance<FootnoteDefinitionElement>().forEach {
            if (!footnoteDefinitions.containsKey(it.identifier)) {
                footnoteDefinitions[it.identifier] = it
            }
        }

        // Return a Composable that renders the document content
        return {
            Column(modifier = modifier) {
                // Render children, skipping footnote definitions as they are handled separately
                document.children.filterNot { it is FootnoteDefinitionElement }.forEach { element ->
                    renderElement(element)() // Invoke the Composable returned by the dispatcher
                }
                // Footnote definitions are collected but rendered via FootnoteReferenceElementRenderer clicks or potentially a separate composable if needed.
            }
        }
    }

    // Removed @Composable annotation - Directly return the result of renderElement
    override fun renderHeader(header: HeaderElement): @Composable () -> Unit = renderElement(header)

    // Removed @Composable annotation - Directly return the result of renderElement
    override fun renderText(text: MarkdownTextElement): @Composable () -> Unit = renderElement(text)

    // Removed @Composable annotation - Directly return the result of renderElement
    override fun renderBold(bold: BoldElement): @Composable () -> Unit = renderElement(bold)

    // Implementations for abstract methods from MarkdownRenderer
    // These simply dispatch to the central renderElement logic

    // Removed @Composable annotation - Directly return the result of renderElement
    override fun renderItalic(italic: ItalicElement): @Composable () -> Unit = renderElement(italic)

    // Removed @Composable annotation - Directly return the result of renderElement
    override fun renderStrikethrough(strikethrough: StrikethroughElement): @Composable () -> Unit = renderElement(strikethrough)

    // Removed @Composable annotation - Directly return the result of renderElement
    override fun renderCode(code: CodeElement): @Composable () -> Unit = renderElement(code)

    // Removed @Composable annotation - Directly return the result of renderElement
    override fun renderLink(link: LinkElement): @Composable () -> Unit = renderElement(link)

    // Removed @Composable annotation - Directly return the result of renderElement
    override fun renderImage(image: ImageElement): @Composable () -> Unit = renderElement(image)

    // Removed @Composable annotation - Directly return the result of renderElement
    override fun renderImageLink(imageLink: ImageLinkElement): @Composable () -> Unit = renderElement(imageLink)

    // Removed @Composable annotation - Directly return the result of renderElement
    override fun renderBlockQuote(blockQuote: BlockQuoteElement): @Composable () -> Unit = renderElement(blockQuote)

    // Removed @Composable annotation - Directly return the result of renderElement
    override fun renderList(list: ListElement): @Composable () -> Unit = renderElement(list)

    // Removed @Composable annotation - Directly return the result of renderElement
    override fun renderListItem(listItem: ListItemElement): @Composable () -> Unit = renderElement(listItem)

    // Removed @Composable annotation - Directly return the result of renderElement
    override fun renderTaskListItem(taskListItem: TaskListItemElement): @Composable () -> Unit = renderElement(taskListItem)

    // Removed @Composable annotation - Directly return the result of renderElement
    override fun renderTable(table: TableElement): @Composable () -> Unit = renderElement(table)

    // Table Row/Cell rendering is handled internally by TableElementRenderer
    // Removed @Composable annotation - These are no-op as handled internally
    override fun renderTableRow(tableRow: TableRowElement): @Composable () -> Unit = { } // Still no-op

    // Removed @Composable annotation - These are no-op as handled internally
    override fun renderTableCell(tableCell: TableCellElement): @Composable () -> Unit = { } // Still no-op

    // Removed @Composable annotation - Directly return the result of renderElement
    override fun renderHorizontalRule(horizontalRule: HorizontalRuleElement): @Composable () -> Unit = renderElement(horizontalRule)

    // Removed @Composable annotation - Directly return the result of renderElement
    override fun renderLineBreak(lineBreak: LineBreakElement): @Composable () -> Unit = renderElement(lineBreak)

    // Removed @Composable annotation - Directly return the result of renderElement
    override fun renderFootnoteReference(footnoteRef: FootnoteReferenceElement): @Composable () -> Unit = renderElement(footnoteRef)

    // Removed @Composable annotation - Directly return the result of renderElement
    override fun renderFootnoteDefinition(footnoteDef: FootnoteDefinitionElement): @Composable () -> Unit = renderElement(footnoteDef)

    // Removed @Composable annotation - Directly return the result of renderElement
    override fun renderDefinitionList(definitionList: DefinitionListElement): @Composable () -> Unit = renderElement(definitionList)

    // Definition Term/Details/Item rendering handled by DefinitionListElementRenderer
    // Removed @Composable annotation - These are no-op as handled internally
    override fun renderDefinitionTerm(definitionTerm: DefinitionTermElement): @Composable () -> Unit = { } // Still no-op

    // Removed @Composable annotation - These are no-op as handled internally
    override fun renderDefinitionDetails(definitionDetails: DefinitionDetailsElement): @Composable () -> Unit = { } // Still no-op

    // Removed @Composable annotation - These are no-op as handled internally
    override fun renderDefinitionItem(definitionItem: DefinitionItemElement): @Composable () -> Unit = { } // Still no-op


    // --- Footnote Number Handling ---

    /**
     * Gets the sequential number assigned to a footnote identifier.
     * If the identifier hasn't been assigned a number yet (e.g., called from definition before reference),
     * it assigns the next available number. This ensures definitions rendered later still get a number.
     * Ideally, references are rendered first to establish the order.
     */
    internal fun getFootnoteNumber(identifier: String): Int {
        return footnoteNumbers.getOrPut(identifier) {
            ++footnoteCounter // Assign the next number if not found
        }
    }


    // --- Helper for rendering children ---

    // Composable helper function for use by element renderers within this module.
    @Composable
    internal fun renderChildren(children: List<MarkdownElement>) {
        // Simple sequential rendering for now
        children.forEach { element ->
            renderElement(element)() // Invoke the returned Composable
        }
    }


    // --- Central Dispatch Logic ---
    // --- Central Dispatch Logic ---
    // Removed @Composable annotation - this function itself doesn't need to be composable,
    // it just returns a composable lambda.
    // Added 'override' back as it now implements the abstract function from the interface.
    override fun renderElement(element: MarkdownElement): @Composable () -> Unit {
        // Custom renderer logic could be re-introduced here if needed in the future.

        // Default dispatch to specific element renderers
        // Return the result of the 'when' expression with an explicit cast
        return (when (element) {
            // Basic Inline Elements
            is MarkdownTextElement -> TextElementRenderer.render(this, element)
            is BoldElement -> BoldElementRenderer.render(this, element)
            is ItalicElement -> ItalicElementRenderer.render(this, element)
            is StrikethroughElement -> StrikethroughElementRenderer.render(this, element)
            is CodeElement -> CodeElementRenderer.render(this, element) // Handles both inline and block

            // Links & Images
            is LinkElement -> LinkElementRenderer.render(this, element)
            is ImageElement -> ImageElementRenderer.render(this, element)
            is ImageLinkElement -> ImageLinkElementRenderer.render(this, element)

            // Basic Block Elements
            is HeaderElement -> HeaderElementRenderer.render(this, element)
            is BlockQuoteElement -> BlockQuoteElementRenderer.render(this, element)
            is ParagraphElement -> ParagraphElementRenderer.render(this, element) // Add ParagraphElement handling
            is HorizontalRuleElement -> HorizontalRuleElementRenderer.render(this, element)
            is LineBreakElement -> LineBreakElementRenderer.render(this, element) // Keep LineBreak for explicit breaks (e.g., from parser if needed later)

            // Lists (Complex - might need context passing)
            is ListElement -> ListElementRenderer.render(this, element)
            is ListItemElement -> ListItemElementRenderer.render(this, element) // Needs context from ListElementRenderer
            is TaskListItemElement -> TaskListItemElementRenderer.render(this, element) // Needs context from ListElementRenderer

            // Table (Complex - Canvas based)
            is TableElement -> TableElementRenderer.render(this, element)

            // Footnotes (Complex - state management)
            is FootnoteReferenceElement -> FootnoteReferenceElementRenderer.render(this, element)
            is FootnoteDefinitionElement -> FootnoteDefinitionElementRenderer.render(this, element) // Likely rendered separately

             // Definition Lists
             is DefinitionListElement -> DefinitionListElementRenderer.render(this, element)
             is DefinitionTermElement -> DefinitionTermElementRenderer.render(this, element) // Use new renderer
             is DefinitionDetailsElement -> DefinitionDetailsElementRenderer.render(this, element) // Use new renderer
             // DefinitionItemElement is a container within DefinitionListElement, might not need direct dispatch

            // Elements handled internally by others (should not be dispatched directly)
            is MarkdownDocument -> {
                Log.e("ComposeMarkdownRenderer", "renderDocument called via renderElement dispatch!")
                 // Return an empty composable for unexpected direct calls
            }
            is TableRowElement, is TableCellElement -> {
                Log.e("ComposeMarkdownRenderer", "${element::class.simpleName} should be handled by TableElementRenderer")
                 // Return an empty composable as these are handled internally
            }
             // DefinitionItemElement is handled within DefinitionListElementRenderer
            is DefinitionItemElement -> {
                 Log.w("ComposeMarkdownRenderer", "DefinitionItemElement should be handled by DefinitionListElementRenderer")
                  // Return an empty composable if dispatched directly
            }

            else -> {
                Log.w("ComposeMarkdownRenderer", "Unsupported MarkdownElement type encountered in dispatch: ${element::class.simpleName}")
                 // Return an empty composable for unsupported types
            }
        } as @Composable () -> Unit) // Re-added explicit cast
    }
}
