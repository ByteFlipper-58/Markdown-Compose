package com.byteflipper.markdown_compose.model

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
// Import IR elements instead of old nodes and builders
import com.byteflipper.markdown_compose.model.ir.*
import com.byteflipper.markdown_compose.renderer.ComposeMarkdownRenderer // Needed for context in defaults
import com.byteflipper.markdown_compose.renderer.element.* // Import element renderers

/**
 * Defines a set of composable functions used to render different types of Markdown nodes.
 * This allows users to customize the rendering of specific elements by providing their own
 * implementations.
 *
 * Obtain a default set of renderers using `defaultMarkdownRenderers()`.
 *
 * @param renderHeader Composable function to render a HeaderElement.
 * @param renderTable Composable function to render a TableElement.
 * @param renderBlockQuote Composable function to render a BlockQuoteElement.
 * @param renderCode Composable function to render a CodeElement (handles block/inline).
 * @param renderImage Composable function to render an ImageElement.
 * @param renderImageLink Composable function to render an ImageLinkElement.
 * @param renderList Composable function to render a ListElement.
 * @param renderListItem Composable function to render a ListItemElement.
 * @param renderTaskListItem Composable function to render a TaskListItemElement.
 * @param renderHorizontalRule Composable function to render a HorizontalRuleElement.
 * @param renderParagraph Composable function to render a sequence of inline elements as a paragraph (might be less needed now).
 * @param renderFootnoteDefinitions Composable function to render the block of footnote definitions.
 * @param renderDefinitionList Composable function to render a DefinitionListElement.
 * @param renderText Composable function to render a MarkdownTextElement.
 * @param renderBold Composable function to render a BoldElement.
 * @param renderItalic Composable function to render an ItalicElement.
 * @param renderStrikethrough Composable function to render a StrikethroughElement.
 * @param renderLink Composable function to render a LinkElement.
 * @param renderFootnoteReference Composable function to render a FootnoteReferenceElement.
 */
@Immutable
data class MarkdownRenderers(
    // Update signatures to use Element types and pass ComposeMarkdownRenderer context
    val renderHeader: @Composable (renderer: ComposeMarkdownRenderer, element: HeaderElement) -> Unit = { r, e -> HeaderElementRenderer.render(r, e)() },
    val renderTable: @Composable (renderer: ComposeMarkdownRenderer, element: TableElement) -> Unit = { r, e -> TableElementRenderer.render(r, e)() },
    val renderBlockQuote: @Composable (renderer: ComposeMarkdownRenderer, element: BlockQuoteElement) -> Unit = { r, e -> BlockQuoteElementRenderer.render(r, e)() },
    val renderCode: @Composable (renderer: ComposeMarkdownRenderer, element: CodeElement) -> Unit = { r, e -> CodeElementRenderer.render(r, e)() },
    val renderImage: @Composable (renderer: ComposeMarkdownRenderer, element: ImageElement) -> Unit = { r, e -> ImageElementRenderer.render(r, e)() },
    val renderImageLink: @Composable (renderer: ComposeMarkdownRenderer, element: ImageLinkElement) -> Unit = { r, e -> ImageLinkElementRenderer.render(r, e)() },
    val renderList: @Composable (renderer: ComposeMarkdownRenderer, element: ListElement) -> Unit = { r, e -> ListElementRenderer.render(r, e)() },
    val renderListItem: @Composable (renderer: ComposeMarkdownRenderer, element: ListItemElement) -> Unit = { r, e -> ListItemElementRenderer.render(r, e)() },
    val renderTaskListItem: @Composable (renderer: ComposeMarkdownRenderer, element: TaskListItemElement) -> Unit = { r, e -> TaskListItemElementRenderer.render(r, e)() },
    val renderHorizontalRule: @Composable (renderer: ComposeMarkdownRenderer, element: HorizontalRuleElement) -> Unit = { r, e -> HorizontalRuleElementRenderer.render(r, e)() },
    val renderParagraph: @Composable (renderer: ComposeMarkdownRenderer, children: List<MarkdownElement>) -> Unit = { r, c -> ParagraphRenderer.render(r, c)() }, // Example for paragraph
    val renderFootnoteDefinitions: @Composable (renderer: ComposeMarkdownRenderer) -> Unit = { /* TODO: Implement default footnote section rendering */ },
    val renderDefinitionList: @Composable (renderer: ComposeMarkdownRenderer, element: DefinitionListElement) -> Unit = { r, e -> DefinitionListElementRenderer.render(r, e)() },
    val renderText: @Composable (renderer: ComposeMarkdownRenderer, element: MarkdownTextElement) -> Unit = { r, e -> TextElementRenderer.render(r, e)() },
    val renderBold: @Composable (renderer: ComposeMarkdownRenderer, element: BoldElement) -> Unit = { r, e -> BoldElementRenderer.render(r, e)() },
    val renderItalic: @Composable (renderer: ComposeMarkdownRenderer, element: ItalicElement) -> Unit = { r, e -> ItalicElementRenderer.render(r, e)() },
    val renderStrikethrough: @Composable (renderer: ComposeMarkdownRenderer, element: StrikethroughElement) -> Unit = { r, e -> StrikethroughElementRenderer.render(r, e)() },
    val renderLink: @Composable (renderer: ComposeMarkdownRenderer, element: LinkElement) -> Unit = { r, e -> LinkElementRenderer.render(r, e)() },
    val renderFootnoteReference: @Composable (renderer: ComposeMarkdownRenderer, element: FootnoteReferenceElement) -> Unit = { r, e -> FootnoteReferenceElementRenderer.render(r, e)() }
    // Note: renderTableRow, renderTableCell, renderDefinitionTerm, renderDefinitionDetails are not typically overridden directly
)

/**
 * Creates a default set of Markdown renderers using the standard element renderers.
 *
 * @return A remembered instance of [MarkdownRenderers] with default implementations.
 */
@Composable
fun defaultMarkdownRenderers(): MarkdownRenderers {
    // The default implementation now simply uses the standard element renderers.
    // Customization happens by creating a MarkdownRenderers instance and overriding specific lambdas.
    return remember { MarkdownRenderers() } // Return default instance
}

// Placeholder for ParagraphRenderer if needed, or handle paragraphs directly in ComposeMarkdownRenderer
internal object ParagraphRenderer {
     @Composable
     fun render(
         renderer: ComposeMarkdownRenderer,
         children: List<MarkdownElement>
     ): @Composable () -> Unit = {
         // Default paragraph handling: Render children sequentially.
         // Wrapping logic (like FlowRow) might be better handled by the caller or within specific block elements.
         // For now, just render children directly.
         renderer.renderChildren(children)
     }
}
