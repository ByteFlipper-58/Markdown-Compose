package com.byteflipper.markdown_compose

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
// import androidx.compose.material3.LocalTextStyle // Not used directly anymore
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
// import androidx.compose.ui.unit.dp // Not used directly anymore
import com.byteflipper.markdown_compose.model.MarkdownStyleSheet // Keep for now
import com.byteflipper.markdown_compose.model.defaultMarkdownStyleSheet // Keep for now
import com.byteflipper.markdown_compose.model.ir.TaskListItemElement // Import specific IR type needed for callback
import com.byteflipper.markdown_compose.parser.MarkdownParser // Use updated parser
import com.byteflipper.markdown_compose.renderer.ComposeMarkdownRenderer // Import the new renderer
// Remove old model imports if no longer needed
// import com.byteflipper.markdown_compose.model.*

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
 *                            Provides the specific [TaskListItemElement] and the intended new checked state.
 *                            The caller is responsible for updating the source data and triggering recomposition.
 * // TODO: Update renderers parameter to use MarkdownElement types when refactored
 * // @param renderers A set of composable functions to render different Markdown elements. Allows customization.
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    styleSheet: MarkdownStyleSheet = defaultMarkdownStyleSheet(),
    footnotePositions: MutableMap<String, Float>, // TODO: Pass this to the renderer or handle position measurement differently
    onLinkClick: ((url: String) -> Unit)? = null, // TODO: Pass this to the renderer
    onFootnoteReferenceClick: ((identifier: String) -> Unit)? = { id -> Log.i(TAG, "Footnote ref clicked: [^$id]") }, // TODO: Pass this to the renderer
    onTaskCheckedChange: ((node: TaskListItemElement, isChecked: Boolean) -> Unit)? = { node, checked -> Log.i(TAG, "Task item checked: $checked (Default handler)") }, // Updated signature, TODO: Pass this to the renderer
    // renderers: MarkdownRenderers = defaultMarkdownRenderers() // Commented out - incompatible with IR
) {
    // --- Parsing ---
    // Use the updated parser which returns MarkdownDocument (IR)
    val document = remember(markdown) { MarkdownParser.parse(markdown) }
    Log.d(TAG, "Parsed markdown into IR document with ${document.children.size} root elements.")

    // --- Renderer Instantiation ---
    // TODO: Pass callbacks (onLinkClick, etc.) and footnotePositions map to the renderer instance
    val renderer = remember(styleSheet, modifier /*, other dependencies like callbacks */) {
        ComposeMarkdownRenderer(
            styleSheet = styleSheet,
            modifier = modifier
            // customRenderers = renderers // TODO: Update customRenderers to work with IR
        )
    }

    // --- Rendering ---
    // Call the renderer's top-level function and invoke the returned Composable
    renderer.renderDocument(document)()

    Log.d(TAG, "MarkdownText rendering initiated via ComposeMarkdownRenderer.")

    // --- Old Logic Removed ---
    // The complex Column loop, flushTextGroup, spacing logic, footnote extraction,
    // and direct calls to old renderers are now handled within ComposeMarkdownRenderer.
}

// --- extractFootnoteInfo Function Removed ---
// Footnote handling logic is now encapsulated within ComposeMarkdownRenderer.
