package com.byteflipper.markdown_compose.renderer.element

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.byteflipper.markdown_compose.model.ir.StrikethroughElement
import com.byteflipper.markdown_compose.renderer.ComposeMarkdownRenderer
import com.byteflipper.markdown_compose.renderer.util.AnnotatedStringRenderUtil // Import the util

/**
 * Renders a strikethrough text element.
 */
internal object StrikethroughElementRenderer {
    // Removed @Composable annotation
    fun render(
        renderer: ComposeMarkdownRenderer,
        element: StrikethroughElement
    ): @Composable () -> Unit = {
        val annotatedString = buildAnnotatedString {
            withStyle(renderer.styleSheet.strikethroughTextStyle.toSpanStyle()) {
                // Use the utility function
                AnnotatedStringRenderUtil.renderChildren(renderer, this, element.children)
            }
        }
        Text(text = annotatedString)
    }
}
