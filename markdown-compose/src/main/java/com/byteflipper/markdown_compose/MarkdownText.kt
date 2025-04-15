package com.byteflipper.markdown_compose

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.byteflipper.markdown_compose.model.*
import com.byteflipper.markdown_compose.parser.MarkdownParser

private const val TAG = "MarkdownText"

// FootnoteInfo data class moved to model/FootnoteInfo.kt

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
 * @param onTaskCheckedChange Callback invoked when a task list item's checkbox is clicked.
 *                            Provides the specific [TaskListItemNode] and the intended new checked state.
 *                            The caller is responsible for updating the source data and triggering recomposition.
 * @param renderers A set of composable functions to render different Markdown elements. Allows customization.
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    styleSheet: MarkdownStyleSheet = defaultMarkdownStyleSheet(),
    footnotePositions: MutableMap<String, Float>, // Map for updating positions
    onLinkClick: ((url: String) -> Unit)? = null,
    onFootnoteReferenceClick: ((identifier: String) -> Unit)? = { id -> Log.i(TAG, "Footnote ref clicked: [^$id]") }, // Callback only passes ID
    onTaskCheckedChange: ((node: TaskListItemNode, isChecked: Boolean) -> Unit)? = { node, checked -> Log.i(TAG, "Task item '${node.content.firstOrNull()?.toString()?.take(20)}...' checked: $checked (Default handler)") }, // Added callback
    renderers: MarkdownRenderers = defaultMarkdownRenderers() // Add renderers parameter
) {
    // --- Parsing and footnote pre-processing ---
    val parsedNodes = remember(markdown) { MarkdownParser.parse(markdown) }
    Log.d(TAG, "Parsed ${parsedNodes.size} top-level nodes.")
    // Use the moved extractFootnoteInfo function (or ideally, pass FootnoteInfo as a parameter)
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

                // Use the paragraph renderer
                // Ensure all arguments are positional and match the definition:
                // (nodes: List<MarkdownNode>, styleSheet: MarkdownStyleSheet, modifier: Modifier, footnoteReferenceMap: Map<String, Int>?, linkHandler: (String) -> Unit, onFootnoteReferenceClick: ((String) -> Unit)?) -> @Composable () -> Unit
                renderers.renderParagraph( // Call the function to get the composable lambda
                    textNodeGrouper.toList(),
                    styleSheet,
                    Modifier.fillMaxWidth(),
                    footnoteInfo?.identifierToIndexMap,
                    currentLinkHandler,
                    onFootnoteReferenceClick
                )() // Invoke the returned composable lambda

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

            // --- Render the Current Node using Renderers ---
            when (node) {
                // Add () to invoke the returned composable for each renderer call
                is TableNode -> renderers.renderTable(node, styleSheet, Modifier.fillMaxWidth(), footnoteInfo?.identifierToIndexMap, currentLinkHandler, onFootnoteReferenceClick)()
                is HorizontalRuleNode -> renderers.renderHorizontalRule(node, styleSheet, Modifier.fillMaxWidth())()
                is BlockQuoteNode -> renderers.renderBlockQuote(node, styleSheet, Modifier.fillMaxWidth(), footnoteInfo?.identifierToIndexMap, currentLinkHandler, onFootnoteReferenceClick)()
                is CodeNode -> {
                    if (node.isBlock) {
                        renderers.renderCodeBlock(node, styleSheet, Modifier.fillMaxWidth())()
                    } else {
                        textNodeGrouper.add(node) // Buffer inline code
                    }
                }
                is TaskListItemNode -> {
                    // Ensure all arguments are positional and match the definition:
                    // (node: TaskListItemNode, styleSheet: MarkdownStyleSheet, modifier: Modifier, footnoteReferenceMap: Map<String, Int>?, linkHandler: (String) -> Unit, onFootnoteReferenceClick: ((String) -> Unit)?, onCheckedChange: (TaskListItemNode, Boolean) -> Unit) -> @Composable () -> Unit
                    renderers.renderTaskListItem( // Call the function
                        node,
                        styleSheet,
                        Modifier.fillMaxWidth(),
                        footnoteInfo?.identifierToIndexMap,
                        currentLinkHandler,
                        onFootnoteReferenceClick,
                        // Pass lambda directly matching the expected (..., Boolean) -> Unit signature
                        { taskNode, isChecked ->
                            onTaskCheckedChange?.invoke(taskNode, isChecked) // Safe call inside
                        }
                    )() // Invoke the returned composable lambda
                }
                is ImageNode -> renderers.renderImage(node, styleSheet, Modifier.fillMaxWidth())()
                is ImageLinkNode -> renderers.renderImageLink(node, styleSheet, Modifier.fillMaxWidth())()
                is HeaderNode -> renderers.renderHeader(node, styleSheet, Modifier.fillMaxWidth())()
                is ListItemNode -> renderers.renderListItem(node, styleSheet, Modifier.fillMaxWidth(), footnoteInfo?.identifierToIndexMap, currentLinkHandler, onFootnoteReferenceClick)()
                // Add case for DefinitionListNode
                is DefinitionListNode -> renderers.renderDefinitionList(node, styleSheet, Modifier.fillMaxWidth(), footnoteInfo?.identifierToIndexMap, currentLinkHandler, onFootnoteReferenceClick)()
                is LineBreakNode -> { /* Handled by spacing logic */ }
                is TextNode, is BoldTextNode, is ItalicTextNode, is StrikethroughTextNode, is LinkNode,
                is FootnoteReferenceNode -> { // Add FootnoteReferenceNode to buffer
                    Log.v(TAG, "Buffering ${node::class.simpleName} $index")
                    textNodeGrouper.add(node)
                }
                // --- Unexpected Node Handling ---
                // Add Definition nodes to expected skipped/handled elsewhere nodes
                is FootnoteDefinitionsBlockNode, is FootnoteDefinitionNode, is TableCellNode, is TableRowNode,
                is DefinitionTermNode, is DefinitionDetailsNode, is DefinitionItemNode -> {
                    Log.e(TAG, "Unexpected ${node::class.simpleName} encountered directly in body node rendering loop.")
                }
            }
            // Update lastRenderedNode only if the node was actually rendered (not buffered or just spacing)
            // Add DefinitionListNode to the list of rendered nodes
            if (node !is LineBreakNode && !(node is CodeNode && !node.isBlock) && node !is TextNode &&
                node !is BoldTextNode && node !is ItalicTextNode && node !is StrikethroughTextNode &&
                node !is LinkNode && node !is FootnoteReferenceNode && node !is DefinitionListNode) { // Check if it's NOT DefinitionListNode here
                 // If it's any other rendered block node, update lastRenderedNode
                 lastRenderedNode = node
                 Log.d(TAG, "Rendered ${node::class.simpleName} $index using renderer")
            } else if (node is DefinitionListNode) { // Explicitly handle DefinitionListNode
                 lastRenderedNode = node // Update lastRenderedNode for spacing calculation
                Log.d(TAG, "Rendered ${node::class.simpleName} $index using renderer")
            } else if (node is LineBreakNode) {
                 lastRenderedNode = node // Keep track of line breaks for spacing
                 Log.d(TAG, "Processed LineBreakNode $index")
            }

        } // End bodyNodes loop

        // Flush any remaining text group
        flushTextGroup()

        // --- Render Footnote Definitions Block using Renderer ---
        // Ensure all arguments are positional and match the definition:
        // (footnoteInfo: FootnoteInfo?, styleSheet: MarkdownStyleSheet, modifier: Modifier, footnotePositions: MutableMap<String, Float>, linkHandler: (String) -> Unit, onFootnoteReferenceClick: ((String) -> Unit)?) -> @Composable () -> Unit
        renderers.renderFootnoteDefinitions( // Call the function
            footnoteInfo,
            styleSheet,
            Modifier.fillMaxWidth(),
            footnotePositions,
            currentLinkHandler,
            onFootnoteReferenceClick
        )() // Invoke the returned composable lambda

        Log.d(TAG, "MarkdownText rendering complete.")
    }
}

// --- extractFootnoteInfo Function (Keep for now, but use imported FootnoteInfo) ---
// Ideally, this logic should move outside MarkdownText or FootnoteInfo should be passed in.
private fun extractFootnoteInfo(allNodes: List<MarkdownNode>): FootnoteInfo? { // Return type uses imported FootnoteInfo
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
