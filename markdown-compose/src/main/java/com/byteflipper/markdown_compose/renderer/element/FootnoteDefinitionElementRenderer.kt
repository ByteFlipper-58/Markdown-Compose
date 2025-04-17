package com.byteflipper.markdown_compose.renderer.element

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics // Import semantics
import androidx.compose.ui.semantics.testTag // Import testTag for identification
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.byteflipper.markdown_compose.model.ir.FootnoteDefinitionElement
import com.byteflipper.markdown_compose.model.ir.MarkdownTextElement
import com.byteflipper.markdown_compose.renderer.ComposeMarkdownRenderer

/**
 * Renders a footnote definition element, typically displayed at the end of the document.
 */
internal object FootnoteDefinitionElementRenderer {
    // Removed @Composable annotation
    fun render(
        renderer: ComposeMarkdownRenderer,
        element: FootnoteDefinitionElement
    ): @Composable () -> Unit = {
        // Get the correct sequential number based on reference order from the main renderer
        val footnoteNumber = renderer.getFootnoteNumber(element.identifier)
        val indexToShow = if (footnoteNumber > 0) footnoteNumber.toString() else "?" // Use the correct number

        // Add semantics for identifying this footnote definition for scrolling
        val footnoteTag = "footnote_def_${element.identifier}"

        Row(
            modifier = Modifier
                .padding(bottom = 4.dp) // Add some spacing below definition
                .semantics { testTag = footnoteTag } // Add testTag for identification
        ) {
            // Create TextStyle from SpanStyle
            val referenceTextStyle =
                TextStyle.Default.merge(renderer.styleSheet.footnoteReferenceStyle)
            Text(
                text = "$indexToShow.",
                style = referenceTextStyle // Используем созданный TextStyle
            )
            Spacer(modifier = Modifier.width(4.dp))
            // Render the content of the footnote definition
            // Apply base text style using CompositionLocalProvider around the Column
            CompositionLocalProvider(LocalTextStyle provides renderer.styleSheet.textStyle) {
                Column { // Content might span multiple lines/blocks
                    // Directly iterate and render each child element
                    element.children.forEach { child ->
                        // Handle TextElement directly to avoid potential context issues
                        if (child is MarkdownTextElement) {
                            // Use the LocalTextStyle provided by the CompositionLocalProvider
                            Text(
                                text = child.text,
                                style = LocalTextStyle.current // Use the style from the provider
                            )
                        } else {
                            // Render other elements via the standard dispatch
                            renderer.renderElement(child)()
                        }
                    }
                }
            }
        }
    }
}
