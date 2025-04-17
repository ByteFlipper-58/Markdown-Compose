package com.byteflipper.markdown_compose.renderer.element

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.byteflipper.markdown_compose.model.ir.MarkdownTextElement
import com.byteflipper.markdown_compose.renderer.ComposeMarkdownRenderer

/**
 * Renders a plain text element.
 */
internal object TextElementRenderer {
    // Removed @Composable annotation from the function signature
    fun render(
        renderer: ComposeMarkdownRenderer,
        element: MarkdownTextElement
    ): @Composable () -> Unit = {
        // Explicitly merge the provided style with the LocalTextStyle
        val currentStyle = androidx.compose.material3.LocalTextStyle.current
        Text(
            text = element.text,
            style = currentStyle.merge(renderer.styleSheet.textStyle) // Merge styles
        )
    }
}
