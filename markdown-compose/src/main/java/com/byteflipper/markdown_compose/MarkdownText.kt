package com.byteflipper.markdown_compose

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onPlaced // Import onPlaced
import androidx.compose.ui.layout.positionInParent // Import positionInParent
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.byteflipper.markdown_compose.model.*
import com.byteflipper.markdown_compose.parser.MarkdownParser
import com.byteflipper.markdown_compose.renderer.* // Import FOOTNOTE_REF_TAG
import com.byteflipper.markdown_compose.renderer.builders.*
import com.byteflipper.markdown_compose.renderer.canvas.HorizontalRule
import android.util.Log
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.platform.LocalContext

private const val TAG = "MarkdownText"

/** Data class to hold pre-processed footnote information. */
private data class FootnoteInfo(
    val orderedIdentifiers: List<String>, // Identifiers in the order they appear
    val identifierToIndexMap: Map<String, Int>, // Map: identifier -> display index (1, 2, ...)
    val definitions: Map<String, FootnoteDefinitionNode> // Map: identifier -> definition node
)

/**
 * Composable function to render Markdown content as Jetpack Compose UI components.
 * Handles block layout, spacing, footnote processing, and rendering of different elements.
 *
 * @param markdown The Markdown string to be rendered.
 * @param modifier Modifier for layout adjustments.
 * @param styleSheet The stylesheet defining the visual appearance of Markdown elements.
 * @param footnotePositions A mutable map provided by the caller to store the measured Y positions
 *                          (relative to the start of the scrollable content) of footnote definitions.
 *                          The key is the footnote identifier (e.g., "1", "note"), the value is the top Y position in pixels.
 *                          `MarkdownText` will update this map.
 * @param onLinkClick Custom handler for external links ([text](url)). If null, uses default ACTION_VIEW intent.
 * @param onFootnoteReferenceClick Custom handler for footnote reference clicks ([^id]). Parameter is the identifier.
 *                                 The caller should use this identifier to look up the position in `footnotePositions`
 *                                 and trigger scrolling. Default implementation logs the click.
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM) // From Parser
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    styleSheet: MarkdownStyleSheet = defaultMarkdownStyleSheet(),
    footnotePositions: MutableMap<String, Float>, // Map for updating positions
    onLinkClick: ((url: String) -> Unit)? = null,
    onFootnoteReferenceClick: ((identifier: String) -> Unit)? = { id -> Log.i(TAG, "Footnote ref clicked: [^$id]") } // Callback only passes ID
) {
    // --- Parsing and footnote pre-processing ---
    val parsedNodes = remember(markdown) { MarkdownParser.parse(markdown) }
    Log.d(TAG, "Parsed ${parsedNodes.size} top-level nodes.")
    val footnoteInfo = remember(parsedNodes) { extractFootnoteInfo(parsedNodes) }
    val bodyNodes = remember(parsedNodes) {
        if (parsedNodes.lastOrNull() is FootnoteDefinitionsBlockNode) {
            parsedNodes.dropLast(1)
        } else {
            parsedNodes
        }
    }

    // --- Other setups ---
    val baseTextStyle = styleSheet.textStyle.merge(LocalTextStyle.current)
    val context = LocalContext.current
    val defaultLinkHandler: (String) -> Unit = remember(context) {
        { url ->
            try {
                Log.i(TAG, "Attempting to open link (default handler): $url")
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Log.e(TAG, "Activity not found to handle link click for URL: $url", e)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open link for URL: $url", e)
            }
        }
    }
    val currentLinkHandler = onLinkClick ?: defaultLinkHandler

    // Clear the position map when markdown changes or on initial composition
    LaunchedEffect(markdown) {
        footnotePositions.clear()
        Log.d(TAG, "Cleared footnotePositions map.")
    }


    Column(modifier = modifier) {
        var lastRenderedNode: MarkdownNode? = null
        val textNodeGrouper = mutableListOf<MarkdownNode>()

        // --- flushTextGroup Function ---
        @Composable
        fun flushTextGroup() {
            if (textNodeGrouper.isNotEmpty()) {
                Log.d(TAG, "Flushing text group (${textNodeGrouper.size} nodes)")
                if (lastRenderedNode != null && lastRenderedNode !is LineBreakNode) {
                    Spacer(modifier = Modifier.height(styleSheet.blockSpacing))
                    Log.v(TAG, "Added block spacing before flushed text group.")
                }

                val annotatedString = MarkdownRenderer.render(
                    textNodeGrouper,
                    styleSheet,
                    footnoteInfo?.identifierToIndexMap // Pass footnote map
                )

                ClickableText(
                    text = annotatedString,
                    style = baseTextStyle,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { offset ->
                        Log.d(TAG, "Click detected at offset: $offset in flushed text group")
                        // Check for URL link first
                        annotatedString
                            .getStringAnnotations(tag = Link.URL_TAG, start = offset, end = offset)
                            .firstOrNull()?.let { annotation ->
                                Log.i(TAG, "Link clicked in paragraph/inline: ${annotation.item}")
                                currentLinkHandler(annotation.item)
                                return@ClickableText // Handled
                            }
                        // Check for Footnote reference link
                        annotatedString
                            .getStringAnnotations(tag = FOOTNOTE_REF_TAG, start = offset, end = offset)
                            .firstOrNull()?.let { annotation ->
                                Log.i(TAG, "Footnote ref [^${annotation.item}] clicked in paragraph/inline")
                                onFootnoteReferenceClick?.invoke(annotation.item) // Call callback with identifier
                            }
                    }
                )
                lastRenderedNode = textNodeGrouper.last()
                textNodeGrouper.clear()
            } else {
                Log.v(TAG, "flushTextGroup called, but buffer is empty.")
            }
        }

        // --- Iterate through BODY nodes only ---
        bodyNodes.forEachIndexed { index, node ->
            Log.v(TAG, "Processing node $index: ${node::class.simpleName}")
            val needsTopSpacing = lastRenderedNode != null && lastRenderedNode !is LineBreakNode

            // Spacing Calculation
            val spacingDp = when (node) {
                is LineBreakNode -> if (needsTopSpacing) styleSheet.lineBreakSpacing else 0.dp
                is HeaderNode, is TableNode, is HorizontalRuleNode, is BlockQuoteNode,
                is ImageNode, is ImageLinkNode -> if (needsTopSpacing) styleSheet.blockSpacing else 0.dp
                is CodeNode -> if (node.isBlock && needsTopSpacing) styleSheet.blockSpacing else 0.dp
                is ListItemNode -> if (needsTopSpacing && lastRenderedNode !is ListItemNode && lastRenderedNode !is TaskListItemNode) styleSheet.blockSpacing else 0.dp
                is TaskListItemNode -> if (needsTopSpacing && lastRenderedNode !is ListItemNode && lastRenderedNode !is TaskListItemNode) styleSheet.blockSpacing else 0.dp
                else -> 0.dp // Inline nodes handled by flush
            }
            Log.v(TAG, "Node $index (${node::class.simpleName}) calculated spacing: ${spacingDp.value} dp")

            // Flush Condition
            val shouldFlush = when (node) {
                is HeaderNode, is TableNode, is HorizontalRuleNode, is BlockQuoteNode,
                is ListItemNode, is TaskListItemNode, is ImageNode, is ImageLinkNode,
                is LineBreakNode -> true
                is CodeNode -> node.isBlock
                else -> false // Inline nodes don't flush
            }
            Log.v(TAG, "Node $index shouldFlush: $shouldFlush")

            // Flush Buffer if needed
            if (shouldFlush) {
                flushTextGroup()
            }

            // Add Vertical Spacing
            if (spacingDp > 0.dp) {
                Spacer(modifier = Modifier.height(spacingDp))
                Log.v(TAG, "Added Spacer height ${spacingDp.value} dp before node $index")
            }

            // --- Render the Current Node ---
            when (node) {
                is TableNode -> {
                    Table.RenderTable(
                        tableNode = node,
                        styleSheet = styleSheet,
                        modifier = Modifier.fillMaxWidth(),
                        footnoteReferenceMap = footnoteInfo?.identifierToIndexMap, // Pass map
                        linkHandler = currentLinkHandler,
                        onFootnoteReferenceClick = onFootnoteReferenceClick // Pass callback
                    )
                    lastRenderedNode = node
                    Log.d(TAG, "Rendered TableNode $index")
                }
                is HorizontalRuleNode -> {
                    HorizontalRule.Render(
                        color = styleSheet.horizontalRuleStyle.color,
                        thickness = styleSheet.horizontalRuleStyle.thickness
                    )
                    lastRenderedNode = node
                    Log.d(TAG, "Rendered HorizontalRuleNode $index")
                }
                is BlockQuoteNode -> {
                    BlockQuoteComposable(
                        node = node,
                        styleSheet = styleSheet,
                        modifier = Modifier.fillMaxWidth(),
                        footnoteReferenceMap = footnoteInfo?.identifierToIndexMap, // Pass map
                        linkHandler = currentLinkHandler, // Pass existing link handler
                        onFootnoteReferenceClick = onFootnoteReferenceClick // Pass footnote callback
                    )
                    lastRenderedNode = node
                    Log.d(TAG, "Rendered BlockQuoteNode $index")
                }
                is CodeNode -> {
                    if (node.isBlock) {
                        CodeBlockComposable(node = node, styleSheet = styleSheet, modifier = Modifier.fillMaxWidth())
                        lastRenderedNode = node
                        Log.d(TAG, "Rendered CodeBlockNode $index")
                    } else {
                        textNodeGrouper.add(node) // Buffer inline code
                    }
                }
                is TaskListItemNode -> {
                    // Note: TaskListItem currently does NOT support internal footnote clicks. Needs enhancement.
                    TaskListItem(
                        node = node,
                        styleSheet = styleSheet,
                        modifier = Modifier.fillMaxWidth(),
                        linkHandler = currentLinkHandler
                    )
                    lastRenderedNode = node
                    Log.d(TAG, "Rendered TaskListItemNode $index via Composable")
                }
                is ImageNode -> {
                    ImageComposable(node = node, styleSheet = styleSheet, modifier = Modifier.fillMaxWidth())
                    lastRenderedNode = node
                    Log.d(TAG, "Rendered ImageNode $index")
                }
                is ImageLinkNode -> {
                    ImageLinkComposable(node = node, styleSheet = styleSheet, modifier = Modifier.fillMaxWidth())
                    lastRenderedNode = node
                    Log.d(TAG, "Rendered ImageLinkNode $index")
                }
                is HeaderNode -> {
                    // Headers generally don't contain clickable items
                    Text(
                        text = MarkdownRenderer.render(listOf(node), styleSheet, footnoteInfo?.identifierToIndexMap),
                        style = baseTextStyle,
                        modifier = Modifier.fillMaxWidth().padding(bottom = styleSheet.headerStyle.bottomPadding)
                    )
                    lastRenderedNode = node
                    Log.d(TAG, "Rendered HeaderNode $index")
                }
                is ListItemNode -> {
                    val listString = MarkdownRenderer.render(listOf(node), styleSheet, footnoteInfo?.identifierToIndexMap)
                    ClickableText(
                        text = listString,
                        style = baseTextStyle,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { offset ->
                            listString.getStringAnnotations(Link.URL_TAG, offset, offset).firstOrNull()
                                ?.let { currentLinkHandler(it.item); return@ClickableText }
                            listString.getStringAnnotations(FOOTNOTE_REF_TAG, offset, offset).firstOrNull()
                                ?.let { onFootnoteReferenceClick?.invoke(it.item) } // Pass ID up
                        }
                    )
                    lastRenderedNode = node
                    Log.d(TAG, "Rendered ListItemNode $index")
                }
                is LineBreakNode -> {
                    lastRenderedNode = node
                    Log.d(TAG, "Processed LineBreakNode $index")
                }
                is TextNode, is BoldTextNode, is ItalicTextNode, is StrikethroughTextNode, is LinkNode,
                is FootnoteReferenceNode -> { // Add FootnoteReferenceNode to buffer
                    Log.v(TAG, "Buffering ${node::class.simpleName} $index")
                    textNodeGrouper.add(node)
                }
                // --- Unexpected Node Handling ---
                is FootnoteDefinitionsBlockNode, is FootnoteDefinitionNode, is TableCellNode, is TableRowNode -> {
                    Log.e(TAG, "Unexpected ${node::class.simpleName} encountered in body node rendering loop.")
                }
            }
        } // End bodyNodes loop

        // Flush any remaining text group
        flushTextGroup()

        // --- Render Footnote Definitions Block ---
        if (footnoteInfo != null && footnoteInfo.definitions.isNotEmpty()) {
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
                    val definitionPrefix = "[$displayIndex]: "
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
                                Log.d(TAG, "Stored position for footnote [^$identifier]: $positionY")
                            },
                        onClick = { offset ->
                            // Handle clicks inside definition content
                            fullDefinitionString.getStringAnnotations(Link.URL_TAG, offset, offset).firstOrNull()
                                ?.let { currentLinkHandler(it.item); return@ClickableText }
                            fullDefinitionString.getStringAnnotations(FOOTNOTE_REF_TAG, offset, offset).firstOrNull()
                                ?.let { onFootnoteReferenceClick?.invoke(it.item) } // Pass ID up
                        }
                    )
                } else {
                    Log.w(TAG, "Could not find definition or index for ordered identifier: $identifier")
                }
            }
        }
        Log.d(TAG, "MarkdownText rendering complete.")
    }
}

// --- extractFootnoteInfo Function (No changes from previous step) ---
private fun extractFootnoteInfo(allNodes: List<MarkdownNode>): FootnoteInfo? {
    val orderedIdentifiers = mutableListOf<String>()
    val identifierToIndexMap = mutableMapOf<String, Int>()
    var currentIndex = 1

    val definitionsMap = (allNodes.lastOrNull() as? FootnoteDefinitionsBlockNode)?.definitions ?: emptyMap()

    fun findReferences(nodes: List<MarkdownNode>) {
        nodes.forEach { node ->
            when (node) {
                is FootnoteReferenceNode -> {
                    if (definitionsMap.containsKey(node.identifier)) {
                        if (!identifierToIndexMap.containsKey(node.identifier)) {
                            identifierToIndexMap[node.identifier] = currentIndex
                            orderedIdentifiers.add(node.identifier)
                            currentIndex++
                        }
                    } else {
                        Log.w("extractFootnoteInfo", "Reference found for undefined footnote: [^${node.identifier}]")
                    }
                }
                is HeaderNode -> findReferences(node.content)
                is ListItemNode -> findReferences(node.content)
                is TaskListItemNode -> findReferences(node.content)
                is BlockQuoteNode -> findReferences(node.content)
                is TableNode -> node.rows.forEach { row -> row.cells.forEach { cell -> findReferences(cell.content) } }
                is LinkNode -> findReferences(listOf(TextNode(node.text)))
                else -> { /* No relevant children */ }
            }
        }
    }

    val bodyNodes = if (allNodes.lastOrNull() is FootnoteDefinitionsBlockNode) allNodes.dropLast(1) else allNodes
    findReferences(bodyNodes)

    return if (orderedIdentifiers.isNotEmpty()) {
        Log.d("extractFootnoteInfo", "Found ${orderedIdentifiers.size} unique footnote refs with definitions. Map: $identifierToIndexMap")
        FootnoteInfo(orderedIdentifiers, identifierToIndexMap, definitionsMap)
    } else {
        Log.d("extractFootnoteInfo", "No valid footnote references found.")
        null
    }
}