package com.byteflipper.markdown_compose.renderer.element

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.byteflipper.markdown_compose.model.ir.ListElement
import com.byteflipper.markdown_compose.renderer.ComposeMarkdownRenderer

/**
 * Renders a list element (ordered or unordered).
 * Handles indentation and spacing between items.
 */
internal object ListElementRenderer {
    // Removed @Composable annotation
    fun render(
        renderer: ComposeMarkdownRenderer,
        element: ListElement
        // TODO: Pass nesting level for proper indentation calculation
    ): @Composable () -> Unit = {
        val listStyle = renderer.styleSheet.listStyle
        // Apply basic indentation for the entire list block
        Column(
            modifier = Modifier
                .padding(start = listStyle.indentPadding) // Apply indent only once for the block
                .fillMaxWidth()
        ) {
            element.items.forEachIndexed { index, item ->
                // Render the list item using its specific renderer
                // The item renderer itself will handle the bullet/number and content
                renderer.renderElement(item)()

                // Add spacing between list items if specified
                if (index < element.items.size - 1 && listStyle.itemSpacing > 0.dp) {
                    Spacer(modifier = Modifier.height(listStyle.itemSpacing))
                }
            }
        }
    }
}
