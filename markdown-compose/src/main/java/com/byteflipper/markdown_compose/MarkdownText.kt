package com.byteflipper.markdown_compose

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.ScrollState // Import ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope // Import rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.byteflipper.markdown_compose.model.MarkdownStyleSheet
import com.byteflipper.markdown_compose.model.defaultMarkdownStyleSheet
import kotlinx.coroutines.CoroutineScope // Import CoroutineScope
import kotlinx.coroutines.launch // Import launch
import com.byteflipper.markdown_compose.model.ir.TaskListItemElement
import com.byteflipper.markdown_compose.parser.MarkdownParser
import com.byteflipper.markdown_compose.renderer.ComposeMarkdownRenderer

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
 *                          This parameter is no longer used for scrolling, which now relies on ScrollState/LazyListState. Kept for potential future use or remove if definitely unused.
 * @param scrollState The ScrollState of the parent scrollable container (e.g., Column with verticalScroll). Required for footnote scrolling.
 * @param coroutineScope A CoroutineScope to launch the scroll animation. Typically obtained via `rememberCoroutineScope()`.
 * @param onLinkClick Custom handler for external links ([text](url)). If null, uses default ACTION_VIEW intent.
 * @param onFootnoteReferenceClick Custom handler for footnote reference clicks ([^id]). Parameter is the identifier.
 *                                 Default implementation uses the provided scrollState and coroutineScope to scroll towards the bottom.
 * @param onTaskCheckedChange Callback invoked when a task list item's checkbox is clicked.
 *                            Provides the specific [TaskListItemElement] and the intended new checked state.
 *                            The caller is responsible for updating the source data and triggering recomposition.
 * // TODO: Update renderers parameter to use MarkdownElement types when refactored
 * // @param renderers A set of composable functions to render different Markdown elements. Allows customization.
 */
// Added SuppressLint because default value for coroutineScope uses rememberCoroutineScope()
// which should ideally be called directly in the composable body, but here it's for the default lambda.
@SuppressLint("ComposableLambdaParameterPosition")
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    styleSheet: MarkdownStyleSheet = defaultMarkdownStyleSheet(),
    scrollState: ScrollState, // Added ScrollState parameter
    coroutineScope: CoroutineScope = rememberCoroutineScope(), // Added CoroutineScope with default
    footnotePositions: MutableMap<String, Float>? = null, // Made optional, as it's not used for scrolling now
    onLinkClick: ((url: String) -> Unit)? = null,
    onFootnoteReferenceClick: ((identifier: String) -> Unit)? = { identifier ->
        // Default handler: Scroll towards the bottom using provided scope and state
        Log.d(TAG, "Default footnote click handler: Scrolling towards bottom for ref [^$identifier]")
        coroutineScope.launch {
            // Scroll to the end where footnotes are located
            scrollState.animateScrollTo(scrollState.maxValue)
            // Note: Precise scrolling to the specific footnote definition requires
            // either LazyListState or manual position measurement, which is more complex.
        }
    },
    onTaskCheckedChange: ((node: TaskListItemElement, isChecked: Boolean) -> Unit)? = { node, checked -> Log.i(TAG, "Task item checked: $checked (Default handler)") },
) {
    // --- Parsing ---
    val document = remember(markdown) { MarkdownParser.parse(markdown) }
    Log.d(TAG, "Parsed markdown into IR document with ${document.children.size} root elements.")

    // --- Renderer Instantiation ---
    // Pass the potentially overridden onFootnoteReferenceClick lambda
    val actualOnFootnoteReferenceClick = onFootnoteReferenceClick ?: { identifier ->
        // Replicate default logic if user passes null explicitly
        Log.d(TAG, "Default footnote click handler (invoked via null override): Scrolling towards bottom for ref [^$identifier]")
        coroutineScope.launch {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    val renderer = remember(styleSheet, modifier, onLinkClick, actualOnFootnoteReferenceClick, onTaskCheckedChange) {
        ComposeMarkdownRenderer(
            styleSheet = styleSheet,
            modifier = modifier, // Modifier is applied within the renderer's renderDocument Column
            onLinkClick = onLinkClick,
            onFootnoteReferenceClick = onFootnoteReferenceClick,
            onTaskCheckedChange = onTaskCheckedChange
            // TODO: Pass footnotePositions map if needed by renderer internally
            // customRenderers = renderers // TODO: Update customRenderers to work with IR
        )
    }

    // --- Rendering ---
    // Call the renderer's top-level function to get the Composable lambda
    val renderFunction = renderer.renderDocument(document)
    // Invoke the returned Composable lambda to perform the rendering
    renderFunction()

    Log.d(TAG, "MarkdownText rendering initiated via ComposeMarkdownRenderer.")

    // --- Old Logic Removed ---
    // The complex Column loop, flushTextGroup, spacing logic, footnote extraction,
    // and direct calls to old renderers are now handled within ComposeMarkdownRenderer.
}
