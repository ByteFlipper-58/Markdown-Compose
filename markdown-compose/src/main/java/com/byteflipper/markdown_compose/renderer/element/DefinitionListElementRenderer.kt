package com.byteflipper.markdown_compose.renderer.element

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.byteflipper.markdown_compose.model.ir.DefinitionListElement
import com.byteflipper.markdown_compose.renderer.ComposeMarkdownRenderer

/**
 * Renders a definition list element.
 */
internal object DefinitionListElementRenderer {
    // Removed @Composable annotation
    fun render(
        renderer: ComposeMarkdownRenderer,
        element: DefinitionListElement
    ): @Composable () -> Unit = {
        // TODO: Apply specific styling for definition lists if available in styleSheet
        Column(modifier = Modifier.fillMaxWidth()) {
            element.items.forEachIndexed { index, item ->
                // Render term
                renderer.renderElement(item.term)() // Assuming DefinitionTermElement is handled by renderElement
                // Render details, potentially indented
                item.details.forEach { detail ->
                    // Add padding for details or handle in DefinitionDetailsElementRenderer
                    Box(modifier = Modifier.padding(start = 16.dp)) { // Example indent
                        renderer.renderElement(detail)() // Assuming DefinitionDetailsElement is handled
                    }
                }
                // Add spacing between items
                if (index < element.items.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp)) // Example spacing
                }
            }
        }
    }
}
