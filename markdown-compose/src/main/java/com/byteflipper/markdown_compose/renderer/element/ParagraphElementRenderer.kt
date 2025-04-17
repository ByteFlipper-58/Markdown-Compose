package com.byteflipper.markdown_compose.renderer.element

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.byteflipper.markdown_compose.model.ir.ParagraphElement
import com.byteflipper.markdown_compose.renderer.ComposeMarkdownRenderer
import com.byteflipper.markdown_compose.renderer.util.AnnotatedStringRenderUtil.buildAnnotatedString

/**
 * Renders a ParagraphElement.
 * It builds an AnnotatedString from the paragraph's inline children and displays it using Text,
 * applying appropriate padding from the style sheet.
 */
internal object ParagraphElementRenderer {

    fun render(
        renderer: ComposeMarkdownRenderer,
        element: ParagraphElement
    ): @Composable () -> Unit = {
        val annotatedString = buildAnnotatedString(
            elements = element.children,
            renderer = renderer,
            baseTextStyle = renderer.styleSheet.textStyle // Use base text style from stylesheet
        )

        // Apply paragraph padding from the stylesheet
        Box(modifier = Modifier.padding(renderer.styleSheet.paragraphPadding)) {
            Text(
                text = annotatedString,
                style = LocalTextStyle.current.merge(renderer.styleSheet.textStyle) // Ensure base style is applied
                // Add other Text parameters like overflow, maxLines if needed
            )
        }
    }
}
