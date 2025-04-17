package com.byteflipper.markdown_compose.renderer.element

import android.util.Log
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import com.byteflipper.markdown_compose.model.ir.FootnoteReferenceElement
import com.byteflipper.markdown_compose.renderer.ComposeMarkdownRenderer

/**
 * Renders a footnote reference element as a superscript link.
 */
internal object FootnoteReferenceElementRenderer {
    private const val FOOTNOTE_REF_TAG = "FOOTNOTE_REF" // Consistent tag

    // Removed @Composable annotation
    fun render(
        renderer: ComposeMarkdownRenderer,
        element: FootnoteReferenceElement
    ): @Composable () -> Unit = {
        // Get the correct sequential number based on reference order
        val footnoteNumber = renderer.getFootnoteNumber(element.identifier)
        // Store the number in the element if needed elsewhere, though rendering uses it directly
        element.displayIndex = if (footnoteNumber > 0) footnoteNumber else null

        // Display the sequential number. Fallback to identifier might not be desired.
        // If footnoteNumber is 0 (not found), maybe log an error or display '?'
        val text = if (footnoteNumber > 0) footnoteNumber.toString() else "?"
        val style = renderer.styleSheet.footnoteReferenceStyle.merge(
            SpanStyle(baselineShift = BaselineShift.Superscript) // Apply superscript
        )

        val annotatedString = buildAnnotatedString {
            pushStringAnnotation(tag = FOOTNOTE_REF_TAG, annotation = element.identifier)
            withStyle(style) {
                append(text)
            }
            pop()
        }

        ClickableText(
            text = annotatedString,
            style = renderer.styleSheet.textStyle, // Base style for clickable area
            onClick = { offset ->
                annotatedString.getStringAnnotations(tag = FOOTNOTE_REF_TAG, start = offset, end = offset)
                    .firstOrNull()?.let { annotation ->
                        Log.d("FootnoteRefRenderer", "Footnote ref [^${annotation.item}] clicked")
                        renderer.onFootnoteReferenceClick?.invoke(annotation.item)
                    }
            }
        )
    }
}
