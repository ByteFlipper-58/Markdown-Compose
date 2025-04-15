package com.byteflipper.markdown_compose.renderer

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import com.byteflipper.markdown_compose.model.MarkdownRenderers // Keep old import for now
import com.byteflipper.markdown_compose.model.MarkdownStyleSheet // Keep old import for now
import com.byteflipper.markdown_compose.model.ir.*
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier

/**
 * A MarkdownRenderer implementation that targets Jetpack Compose.
 * It converts MarkdownElement nodes into Composable functions.
 *
 * @param styleSheet The styles to apply to the rendered elements.
 * @param customRenderers Optional custom renderers for specific element types.
 * @param modifier Optional modifier to apply to the root composable.
 */
class ComposeMarkdownRenderer(
    private val styleSheet: MarkdownStyleSheet,
    private val customRenderers: MarkdownRenderers? = null, // TODO: Update MarkdownRenderers to use MarkdownElement
    private val modifier: Modifier = Modifier
) : MarkdownRenderer<@Composable () -> Unit> {

    // TODO: Implement footnote collection and rendering logic
    private val footnoteDefinitions = mutableMapOf<String, FootnoteDefinitionElement>()
    private var footnoteCounter = 0

    @Composable
    override fun renderDocument(document: MarkdownDocument): @Composable () -> Unit {
        // TODO: Pre-process to collect footnotes?
        footnoteDefinitions.clear()
        footnoteCounter = 0
        // Collect definitions (simple approach for now)
        document.children.filterIsInstance<FootnoteDefinitionElement>().forEach {
            if (!footnoteDefinitions.containsKey(it.identifier)) {
                footnoteDefinitions[it.identifier] = it
            }
        }

        return {
            Column(modifier = modifier) {
                renderChildren(document.children.filter { it !is FootnoteDefinitionElement }) // Render main content
                // TODO: Render collected footnotes at the end if any
                if (footnoteDefinitions.isNotEmpty()) {
                    // Placeholder for footnote section rendering
                    Text("--- Footnotes ---") // Example separator
                    footnoteDefinitions.values.forEach { renderFootnoteDefinition(it)() }
                }
            }
        }
    }

    @Composable
    override fun renderHeader(header: HeaderElement): @Composable () -> Unit = {
        // Placeholder - TODO: Migrate logic from Header builder
        val style = when (header.level) {
            1 -> styleSheet.h1
            2 -> styleSheet.h2
            3 -> styleSheet.h3
            4 -> styleSheet.h4
            5 -> styleSheet.h5
            else -> styleSheet.h6
        }
        Text(text = "H${header.level} Placeholder", style = style)
        renderChildren(header.children) // Render inline content within header
    }

    @Composable
    override fun renderText(text: MarkdownTextElement): @Composable () -> Unit = {
        Text(text = text.text, style = styleSheet.textStyle)
    }

    @Composable
    override fun renderBold(bold: BoldElement): @Composable () -> Unit = {
        // Placeholder - TODO: Apply bold style and render children
        Text("Bold Placeholder: ")
        renderChildren(bold.children)
    }

    @Composable
    override fun renderItalic(italic: ItalicElement): @Composable () -> Unit = {
        // Placeholder - TODO: Apply italic style and render children
        Text("Italic Placeholder: ")
        renderChildren(italic.children)
    }

    @Composable
    override fun renderStrikethrough(strikethrough: StrikethroughElement): @Composable () -> Unit = {
        // Placeholder - TODO: Apply strikethrough style and render children
        Text("Strikethrough Placeholder: ")
        renderChildren(strikethrough.children)
    }

    @Composable
    override fun renderCode(code: CodeElement): @Composable () -> Unit = {
        // Placeholder - TODO: Migrate logic from Code/CodeBlock builders
        val text = if (code.isBlock) "Code Block Placeholder:\n${code.content}" else "`Code Span Placeholder: ${code.content}`"
        Text(text = text, style = styleSheet.codeBlockStyle ?: styleSheet.codeSpanStyle) // Example style usage
    }

    @Composable
    override fun renderLink(link: LinkElement): @Composable () -> Unit = {
        // Placeholder - TODO: Migrate logic from Link builder
        Text("Link Placeholder [")
        renderChildren(link.children)
        Text("](${link.url})")
    }

    @Composable
    override fun renderImage(image: ImageElement): @Composable () -> Unit = {
        // Placeholder - TODO: Migrate logic from Image builder
        Text("Image Placeholder: ![${image.altText}](${image.url})")
    }

     @Composable
    override fun renderImageLink(imageLink: ImageLinkElement): @Composable () -> Unit = {
         // Placeholder - TODO: Migrate logic from ImageLink builder
         Text("ImageLink Placeholder: [![${imageLink.altText}](${imageLink.imageUrl})](${imageLink.linkUrl})")
    }

    @Composable
    override fun renderBlockQuote(blockQuote: BlockQuoteElement): @Composable () -> Unit = {
        // Placeholder - TODO: Migrate logic from BlockQuote builder
        Column { // Example structure
             Text("> BlockQuote Placeholder:")
             renderChildren(blockQuote.children)
        }
    }

    @Composable
    override fun renderList(list: ListElement): @Composable () -> Unit = {
         // Placeholder - TODO: Migrate logic from list rendering (needs state for ordered lists)
         val listType = if (list.isOrdered) "Ordered" else "Unordered"
         Column {
             Text("$listType List Placeholder:")
             list.items.forEachIndexed { index, item ->
                 // Pass context if needed (e.g., order number)
                 renderListItem(item)() // Assuming renderListItem handles its content
             }
         }
    }

    @Composable
    override fun renderListItem(listItem: ListItemElement): @Composable () -> Unit = {
        // Placeholder - TODO: Migrate logic from ListItem builder
        val prefix = listItem.order?.let { "$it. " } ?: "- "
        Column { // Or Row depending on desired layout
            Text("$prefix List Item Placeholder:")
            renderChildren(listItem.children)
        }
    }

    @Composable
    override fun renderTaskListItem(taskListItem: TaskListItemElement): @Composable () -> Unit = {
        // Placeholder - TODO: Migrate logic from TaskListItem builder
        val check = if (taskListItem.isChecked) "[x]" else "[ ]"
        Column { // Or Row
            Text("- $check Task List Item Placeholder:")
            renderChildren(taskListItem.children)
        }
    }

    @Composable
    override fun renderTable(table: TableElement): @Composable () -> Unit = {
        // Placeholder - TODO: Migrate logic from Table builder
        Column {
            Text("Table Placeholder:")
            table.rows.forEach { renderTableRow(it)() }
        }
    }

    @Composable
    override fun renderTableRow(tableRow: TableRowElement): @Composable () -> Unit = {
        // Placeholder - TODO: Render a Row with cells
        val rowType = if (tableRow.isHeader) "Header" else "Data"
        Column { // Use Row in actual implementation
            Text("Table $rowType Row Placeholder:")
            tableRow.cells.forEach { renderTableCell(it)() }
        }
    }

    @Composable
    override fun renderTableCell(tableCell: TableCellElement): @Composable () -> Unit = {
        // Placeholder - TODO: Render cell content within appropriate layout
        val cellType = if (tableCell.isHeader) "Header" else "Data"
         Column { // Or Box/other layout
             Text("Table $cellType Cell Placeholder:")
             renderChildren(tableCell.children)
         }
    }

    @Composable
    override fun renderHorizontalRule(horizontalRule: HorizontalRuleElement): @Composable () -> Unit = {
        // Placeholder - TODO: Migrate logic from HorizontalRule canvas renderer
        Text("--- Horizontal Rule Placeholder ---")
    }

    @Composable
    override fun renderLineBreak(lineBreak: LineBreakElement): @Composable () -> Unit = {
        // Placeholder - TODO: Implement actual line break (often just spacing or newline in text)
         Text("Line Break Placeholder") // Or just Spacer() or newline in Text
    }

    @Composable
    override fun renderFootnoteReference(footnoteRef: FootnoteReferenceElement): @Composable () -> Unit = {
        // Placeholder - TODO: Implement footnote reference rendering (e.g., superscript link)
        val index = footnoteDefinitions.keys.indexOf(footnoteRef.identifier) + 1 // Simple index lookup
        footnoteRef.displayIndex = index // Assign index
        Text("[^${footnoteRef.displayIndex ?: footnoteRef.identifier}]") // Display index or identifier
    }

    @Composable
    override fun renderFootnoteDefinition(footnoteDef: FootnoteDefinitionElement): @Composable () -> Unit = {
        // Placeholder - TODO: Implement footnote definition rendering (e.g., numbered item)
        val index = footnoteDefinitions.keys.indexOf(footnoteDef.identifier) + 1
        Column { // Or Row
            Text("${index}. [^${footnoteDef.identifier}]: ")
            renderChildren(footnoteDef.children)
        }
    }

    @Composable
    override fun renderDefinitionList(definitionList: DefinitionListElement): @Composable () -> Unit = {
        // Placeholder - TODO: Implement definition list rendering
        Column {
            Text("Definition List Placeholder:")
            definitionList.items.forEach { item ->
                renderDefinitionTerm(item.term)()
                item.details.forEach { renderDefinitionDetails(it)() }
            }
        }
    }

    @Composable
    override fun renderDefinitionTerm(definitionTerm: DefinitionTermElement): @Composable () -> Unit = {
        // Placeholder - TODO: Render term, possibly with specific style
        Column { // Or Row
             Text("Term Placeholder:")
             renderChildren(definitionTerm.children)
        }
    }

    @Composable
    override fun renderDefinitionDetails(definitionDetails: DefinitionDetailsElement): @Composable () -> Unit = {
        // Placeholder - TODO: Render details, possibly indented
         Column { // Or Row
             Text(": Details Placeholder:")
             renderChildren(definitionDetails.children)
         }
    }

    // --- Helper Implementation ---

    @Composable
    override fun renderChildren(children: List<MarkdownElement>) {
        // Simple sequential rendering for now
        children.forEach { element ->
            renderElement(element)() // Invoke the returned Composable
        }
    }
}
