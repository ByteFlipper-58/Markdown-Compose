package com.byteflipper.markdown_compose.renderer.element

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.byteflipper.markdown_compose.model.ir.ListItemElement
import com.byteflipper.markdown_compose.renderer.ComposeMarkdownRenderer

/**
 * Renders a list item element (ordered or unordered).
 * Note: Indentation and bullet/numbering logic might need context from the parent ListElementRenderer.
 */
internal object ListItemElementRenderer {
    // Removed @Composable annotation
    fun render(
        renderer: ComposeMarkdownRenderer,
        element: ListItemElement
        // TODO: Pass nesting level and potentially parent list type (ordered/unordered) for correct prefix/indent
    ): @Composable () -> Unit = {
        val listStyle = renderer.styleSheet.listStyle
        // Basic prefix determination - needs improvement for nested lists/bullet cycling
        val prefix = element.order?.let { listStyle.numberPrefix(it) } ?: "${listStyle.bulletChars.firstOrNull() ?: '*'} "

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top // Align prefix with the first line of content
        ) {
            Text(
                text = prefix,
                style = renderer.styleSheet.textStyle // Use base style for prefix
            )
            // Render content next to the prefix
            Column {
                CompositionLocalProvider(LocalTextStyle provides renderer.styleSheet.textStyle) {
                    renderer.renderChildren(element.children)
                }
            }
        }
    }
}
