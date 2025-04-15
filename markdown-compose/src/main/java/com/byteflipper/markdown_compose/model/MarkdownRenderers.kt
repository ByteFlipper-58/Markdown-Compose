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
// Import FootnoteInfo from its new location
import com.byteflipper.markdown_compose.model.FootnoteInfo
import com.byteflipper.markdown_compose.renderer.FOOTNOTE_REF_TAG
import com.byteflipper.markdown_compose.renderer.MarkdownRenderer
import com.byteflipper.markdown_compose.renderer.builders.*
import com.byteflipper.markdown_compose.renderer.canvas.HorizontalRule

/**
 * Defines a set of composable functions used to render different types of Markdown nodes.
 * This allows users to customize the rendering of specific elements by providing their own
 * implementations.
 *
 * Obtain a default set of renderers using `defaultMarkdownRenderers()`.
 *
 * @param renderHeader Composable function to render a HeaderNode.
 * @param renderTable Composable function to render a TableNode.
 * @param renderBlockQuote Composable function to render a BlockQuoteNode.
 * @param renderCodeBlock Composable function to render a block-level CodeNode.
 * @param renderImage Composable function to render an ImageNode.
 * @param renderImageLink Composable function to render an ImageLinkNode.
 * @param renderListItem Composable function to render a ListItemNode (ordered or unordered).
 * @param renderTaskListItem Composable function to render a TaskListItemNode.
 * @param renderHorizontalRule Composable function to render a HorizontalRuleNode.
 * @param renderParagraph Composable function to render a sequence of inline nodes (TextNode, Bold, Italic, Link, etc.) as a paragraph.
 * @param renderFootnoteDefinitions Composable function to render the block of footnote definitions.
 */
@Immutable
data class MarkdownRenderers(
    // Change types to return @Composable () -> Unit
    val renderHeader: (node: HeaderNode, styleSheet: MarkdownStyleSheet, modifier: Modifier) -> @Composable () -> Unit,
    val renderTable: (node: TableNode, styleSheet: MarkdownStyleSheet, modifier: Modifier, footnoteReferenceMap: Map<String, Int>?, linkHandler: (String) -> Unit, onFootnoteReferenceClick: ((String) -> Unit)?) -> @Composable () -> Unit,
    val renderBlockQuote: (node: BlockQuoteNode, styleSheet: MarkdownStyleSheet, modifier: Modifier, footnoteReferenceMap: Map<String, Int>?, linkHandler: (String) -> Unit, onFootnoteReferenceClick: ((String) -> Unit)?) -> @Composable () -> Unit,
    val renderCodeBlock: (node: CodeNode, styleSheet: MarkdownStyleSheet, modifier: Modifier) -> @Composable () -> Unit,
    val renderImage: (node: ImageNode, styleSheet: MarkdownStyleSheet, modifier: Modifier) -> @Composable () -> Unit,
    val renderImageLink: (node: ImageLinkNode, styleSheet: MarkdownStyleSheet, modifier: Modifier) -> @Composable () -> Unit,
    val renderListItem: (node: ListItemNode, styleSheet: MarkdownStyleSheet, modifier: Modifier, footnoteReferenceMap: Map<String, Int>?, linkHandler: (String) -> Unit, onFootnoteReferenceClick: ((String) -> Unit)?) -> @Composable () -> Unit,
    val renderTaskListItem: (node: TaskListItemNode, styleSheet: MarkdownStyleSheet, modifier: Modifier, footnoteReferenceMap: Map<String, Int>?, linkHandler: (String) -> Unit, onFootnoteReferenceClick: ((String) -> Unit)?, onCheckedChange: (TaskListItemNode, Boolean) -> Unit) -> @Composable () -> Unit,
    val renderHorizontalRule: (node: HorizontalRuleNode, styleSheet: MarkdownStyleSheet, modifier: Modifier) -> @Composable () -> Unit,
    val renderParagraph: (nodes: List<MarkdownNode>, styleSheet: MarkdownStyleSheet, modifier: Modifier, footnoteReferenceMap: Map<String, Int>?, linkHandler: (String) -> Unit, onFootnoteReferenceClick: ((String) -> Unit)?) -> @Composable () -> Unit,
    val renderFootnoteDefinitions: (footnoteInfo: FootnoteInfo?, styleSheet: MarkdownStyleSheet, modifier: Modifier, footnotePositions: MutableMap<String, Float>, linkHandler: (String) -> Unit, onFootnoteReferenceClick: ((String) -> Unit)?) -> @Composable () -> Unit,
    // Add renderer for DefinitionListNode
    val renderDefinitionList: (node: DefinitionListNode, styleSheet: MarkdownStyleSheet, modifier: Modifier, footnoteReferenceMap: Map<String, Int>?, linkHandler: (String) -> Unit, onFootnoteReferenceClick: ((String) -> Unit)?) -> @Composable () -> Unit
)

// Removed private FootnoteInfo definition

/**
 * Creates a default set of Markdown renderers using the standard composables provided
 * by the library.
 *
 * @return A remembered instance of [MarkdownRenderers] with default implementations.
 */
@Composable
fun defaultMarkdownRenderers(): MarkdownRenderers {
    // Remember the default renderers to avoid recreating lambdas on recomposition
    return remember {
        MarkdownRenderers(
            renderHeader = { node, styleSheet, modifier -> { // Return composable lambda
                // Default Header rendering (simple Text)
                Text(
                    text = MarkdownRenderer.render(listOf(node), styleSheet, null), // Headers usually don't have footnotes inside
                    style = styleSheet.textStyle.merge(LocalTextStyle.current),
                    modifier = modifier.padding(bottom = styleSheet.headerStyle.bottomPadding)
                )
            }}, // End composable lambda
            renderTable = { node, styleSheet, modifier, footnoteMap, linkHandler, footnoteClickHandler -> { // Return composable lambda
                // Use the existing Table Composable
                Table.RenderTable(
                    tableNode = node,
                    styleSheet = styleSheet,
                    modifier = modifier,
                    footnoteReferenceMap = footnoteMap,
                    linkHandler = linkHandler,
                    onFootnoteReferenceClick = footnoteClickHandler
                )
            }}, // End composable lambda
            renderBlockQuote = { node, styleSheet, modifier, footnoteMap, linkHandler, footnoteClickHandler -> { // Return composable lambda
                // Use the existing BlockQuote Composable
                BlockQuoteComposable(
                    node = node,
                    styleSheet = styleSheet,
                    modifier = modifier,
                    footnoteReferenceMap = footnoteMap,
                    linkHandler = linkHandler,
                    onFootnoteReferenceClick = footnoteClickHandler
                )
            }}, // End composable lambda
            renderCodeBlock = { node, styleSheet, modifier -> { // Return composable lambda
                // Use the existing CodeBlock Composable
                CodeBlockComposable(node = node, styleSheet = styleSheet, modifier = modifier)
            }}, // End composable lambda
            renderImage = { node, styleSheet, modifier -> { // Return composable lambda
                // Use the existing Image Composable
                ImageComposable(node = node, styleSheet = styleSheet, modifier = modifier)
            }}, // End composable lambda
            renderImageLink = { node, styleSheet, modifier -> { // Return composable lambda
                // Use the existing ImageLink Composable
                ImageLinkComposable(node = node, styleSheet = styleSheet, modifier = modifier)
            }}, // End composable lambda
            renderListItem = { node, styleSheet, modifier, footnoteMap, linkHandler, footnoteClickHandler -> { // Return composable lambda
                // Default ListItem rendering (ClickableText)
                val listString = MarkdownRenderer.render(listOf(node), styleSheet, footnoteMap)
                val baseTextStyle = styleSheet.textStyle.merge(LocalTextStyle.current)
                ClickableText(
                    text = listString,
                    style = baseTextStyle,
                    modifier = modifier,
                    onClick = { offset ->
                        listString.getStringAnnotations(Link.URL_TAG, offset, offset).firstOrNull()
                            ?.let { linkHandler(it.item); return@ClickableText }
                        listString.getStringAnnotations(FOOTNOTE_REF_TAG, offset, offset).firstOrNull()
                            ?.let { footnoteClickHandler?.invoke(it.item) }
                    }
                )
            }}, // End composable lambda
            // Updated default implementation for renderTaskListItem
            renderTaskListItem = { node, styleSheet, modifier, footnoteMap, linkHandler, footnoteClickHandler, checkedChangeHandler -> { // Return composable lambda
                // Use the existing TaskListItem Composable, passing all parameters
                TaskListItem(
                    node = node,
                    styleSheet = styleSheet,
                    modifier = modifier,
                    footnoteReferenceMap = footnoteMap,
                    linkHandler = linkHandler,
                    onFootnoteReferenceClick = footnoteClickHandler,
                    onCheckedChange = checkedChangeHandler
                )
            }}, // End composable lambda
            renderHorizontalRule = { node, styleSheet, modifier -> { // Return composable lambda
                // Use the existing HorizontalRule Composable
                HorizontalRule.Render(
                    color = styleSheet.horizontalRuleStyle.color,
                    thickness = styleSheet.horizontalRuleStyle.thickness,
                    modifier = modifier
                )
            }}, // End composable lambda
            renderParagraph = { nodes, styleSheet, modifier, footnoteMap, linkHandler, footnoteClickHandler -> { // Return composable lambda
                // Default Paragraph rendering (ClickableText for inline elements)
                val annotatedString = MarkdownRenderer.render(nodes, styleSheet, footnoteMap)
                val baseTextStyle = styleSheet.textStyle.merge(LocalTextStyle.current)
                ClickableText(
                    text = annotatedString,
                    style = baseTextStyle,
                    modifier = modifier,
                    onClick = { offset ->
                        annotatedString.getStringAnnotations(Link.URL_TAG, offset, offset).firstOrNull()
                            ?.let { linkHandler(it.item); return@ClickableText }
                        annotatedString.getStringAnnotations(FOOTNOTE_REF_TAG, offset, offset).firstOrNull()
                            ?.let { footnoteClickHandler?.invoke(it.item) }
                    }
                )
            }}, // End composable lambda
            renderFootnoteDefinitions = { footnoteInfo, styleSheet, modifier, footnotePositions, linkHandler, footnoteClickHandler -> { // Return composable lambda
                // Default Footnote Definitions rendering block (footnoteInfo is already FootnoteInfo?)
                if (footnoteInfo != null && footnoteInfo.definitions.isNotEmpty()) {
                    Column(modifier = modifier) { // Wrap in a Column for spacing/rule
                        Spacer(modifier = Modifier.height(styleSheet.footnoteBlockPadding))
                        HorizontalRule.Render(
                            color = styleSheet.horizontalRuleStyle.color.copy(alpha = 0.5f),
                            thickness = styleSheet.horizontalRuleStyle.thickness
                        )
                        Spacer(modifier = Modifier.height(styleSheet.blockSpacing / 2))

                        footnoteInfo.orderedIdentifiers.forEach { identifier ->
                            val definitionNode = footnoteInfo.definitions[identifier]
                            val displayIndex = footnoteInfo.identifierToIndexMap[identifier]

                            if (definitionNode != null && displayIndex != null) {
                                // Change prefix from "[index]: " to "index. "
                                val definitionPrefix = "$displayIndex. "
                                val definitionContent = MarkdownRenderer.render(
                                    definitionNode.content,
                                    styleSheet.copy(textStyle = styleSheet.footnoteDefinitionStyle),
                                    footnoteInfo.identifierToIndexMap // Pass map for refs inside defs
                                )

                                val fullDefinitionString = buildAnnotatedString {
                                    withStyle(styleSheet.footnoteDefinitionStyle.toSpanStyle().copy(fontWeight = FontWeight.Bold)) {
                                        append(definitionPrefix)
                                    }
                                    append(definitionContent)
                                }

                                ClickableText(
                                    text = fullDefinitionString,
                                    style = styleSheet.footnoteDefinitionStyle,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = styleSheet.lineBreakSpacing / 2)
                                        .onPlaced { layoutCoordinates -> // Measure position
                                            val positionY = layoutCoordinates.positionInParent().y
                                            footnotePositions[identifier] = positionY // Update external map
                                            Log.d("FootnoteRenderer", "Stored position for footnote [^$identifier]: $positionY")
                                        },
                                    onClick = { offset ->
                                        // Handle clicks inside definition content
                                        fullDefinitionString.getStringAnnotations(Link.URL_TAG, offset, offset).firstOrNull()
                                            ?.let { linkHandler(it.item); return@ClickableText }
                                        fullDefinitionString.getStringAnnotations(FOOTNOTE_REF_TAG, offset, offset).firstOrNull()
                                            ?.let { footnoteClickHandler?.invoke(it.item) }
                                    }
                                )
                            } else {
                                Log.w("FootnoteRenderer", "Could not find definition or index for ordered identifier: $identifier")
                            }
                        }
                    }
                }
            }}, // End composable lambda
            // Add default renderer for DefinitionListNode
            renderDefinitionList = { node, styleSheet, modifier, footnoteMap, linkHandler, footnoteClickHandler -> {
                Column(modifier = modifier) {
                    node.items.forEachIndexed { index, item ->
                        // Render Term
                        val termText = MarkdownRenderer.render(item.term.content, styleSheet, footnoteMap)
                        ClickableText(
                            text = termText,
                            style = styleSheet.definitionListStyle.termTextStyle,
                            onClick = { offset ->
                                termText.getStringAnnotations(Link.URL_TAG, offset, offset).firstOrNull()
                                    ?.let { linkHandler(it.item); return@ClickableText }
                                termText.getStringAnnotations(FOOTNOTE_REF_TAG, offset, offset).firstOrNull()
                                    ?.let { footnoteClickHandler?.invoke(it.item) }
                            }
                        )
                        // Render Details with indent
                        item.details.forEach { detail ->
                            val detailText = MarkdownRenderer.render(detail.content, styleSheet, footnoteMap)
                            ClickableText(
                                text = detailText,
                                style = styleSheet.definitionListStyle.detailsTextStyle,
                                modifier = Modifier.padding(start = styleSheet.definitionListStyle.detailsIndent),
                                onClick = { offset ->
                                    detailText.getStringAnnotations(Link.URL_TAG, offset, offset).firstOrNull()
                                        ?.let { linkHandler(it.item); return@ClickableText }
                                    detailText.getStringAnnotations(FOOTNOTE_REF_TAG, offset, offset).firstOrNull()
                                        ?.let { footnoteClickHandler?.invoke(it.item) }
                                }
                            )
                        }
                        // Add spacing between items
                        if (index < node.items.size - 1) {
                            Spacer(modifier = Modifier.height(styleSheet.definitionListStyle.itemSpacing))
                        }
                    }
                }
            }} // End composable lambda
        )
    }
}
