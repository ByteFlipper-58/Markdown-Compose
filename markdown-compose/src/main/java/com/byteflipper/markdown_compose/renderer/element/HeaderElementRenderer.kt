package com.byteflipper.markdown_compose.renderer.element

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.byteflipper.markdown_compose.model.ir.HeaderElement
import com.byteflipper.markdown_compose.renderer.ComposeMarkdownRenderer

/**
 * Renders a header element (H1-H6).
 */
internal object HeaderElementRenderer {
    // Removed @Composable annotation
    fun render(
        renderer: ComposeMarkdownRenderer,
        element: HeaderElement
    ): @Composable () -> Unit = {
        val style = when (element.level) {
            1 -> renderer.styleSheet.headerStyle.h1
            2 -> renderer.styleSheet.headerStyle.h2
            3 -> renderer.styleSheet.headerStyle.h3
            4 -> renderer.styleSheet.headerStyle.h4
            5 -> renderer.styleSheet.headerStyle.h5
            else -> renderer.styleSheet.headerStyle.h6
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            CompositionLocalProvider(LocalTextStyle provides style) {
                renderer.renderChildren(element.children)
            }
        }
    }
}
