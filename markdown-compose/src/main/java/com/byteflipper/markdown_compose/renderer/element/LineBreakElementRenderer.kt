package com.byteflipper.markdown_compose.renderer.element

import androidx.compose.foundation.layout.Spacer // Re-import Spacer
import androidx.compose.foundation.layout.height // Re-import height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier // Re-import Modifier
import com.byteflipper.markdown_compose.model.ir.LineBreakElement
import com.byteflipper.markdown_compose.renderer.ComposeMarkdownRenderer

/**
 * Renders a line break element, typically as vertical spacing.
 */
internal object LineBreakElementRenderer {
    // Removed @Composable annotation
    fun render(
        renderer: ComposeMarkdownRenderer,
        element: LineBreakElement // Parameter is unused but kept for consistency
    ): @Composable () -> Unit = {
        // Add vertical spacing based on the stylesheet's line break spacing
        // Note: The old logic in MarkdownText added spacing *before* the next block if the previous wasn't a LineBreak.
        // This simple renderer just adds space where the LineBreakElement occurs.
        // More complex spacing logic might need adjustments in the main render loop or here.
        // Revert back to Spacer for block-level spacing
        Spacer(modifier = Modifier.height(renderer.styleSheet.lineBreakSpacing))
    }
}
