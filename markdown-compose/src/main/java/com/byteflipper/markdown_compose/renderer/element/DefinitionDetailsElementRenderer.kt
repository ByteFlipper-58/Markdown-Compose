package com.byteflipper.markdown_compose.renderer.element

import androidx.compose.foundation.layout.Box // Use Box or another simple container
import androidx.compose.runtime.Composable
import com.byteflipper.markdown_compose.model.ir.DefinitionDetailsElement
import com.byteflipper.markdown_compose.renderer.ComposeMarkdownRenderer

/**
 * Renders a definition details element.
 * Typically, this involves rendering its children. Indentation might be handled
 * by the parent DefinitionListElementRenderer or here if needed.
 */
internal object DefinitionDetailsElementRenderer {
    fun render(
        renderer: ComposeMarkdownRenderer,
        element: DefinitionDetailsElement
    ): @Composable () -> Unit = {
        // TODO: Apply specific styling for definition details if available in styleSheet
        // Render children sequentially within a Box or similar container
        Box { // Using Box to contain the children
            renderer.renderChildren(element.children)
        }
    }
}
