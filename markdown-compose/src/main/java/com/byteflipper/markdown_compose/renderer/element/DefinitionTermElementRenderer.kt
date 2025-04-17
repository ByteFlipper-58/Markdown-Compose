package com.byteflipper.markdown_compose.renderer.element

import androidx.compose.foundation.layout.Box // Use Box or another simple container
import androidx.compose.runtime.Composable
import com.byteflipper.markdown_compose.model.ir.DefinitionTermElement
import com.byteflipper.markdown_compose.renderer.ComposeMarkdownRenderer

/**
 * Renders a definition term element.
 * Typically, this involves rendering its children inline.
 */
internal object DefinitionTermElementRenderer {
    fun render(
        renderer: ComposeMarkdownRenderer,
        element: DefinitionTermElement
    ): @Composable () -> Unit = {
        // TODO: Apply specific styling for definition terms if available in styleSheet
        // Render children sequentially within a Box or similar container
        Box { // Using Box to contain the children
             renderer.renderChildren(element.children)
        }
    }
}
