package com.byteflipper.markdown_compose.renderer.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.* // Import all from text
import androidx.compose.ui.text.style.BaselineShift
import com.byteflipper.markdown_compose.model.ir.*
import com.byteflipper.markdown_compose.renderer.ComposeMarkdownRenderer

/**
 * Utility object for building AnnotatedString from Markdown elements.
 */
internal object AnnotatedStringRenderUtil {

    /**
     * Builds an AnnotatedString from a list of Markdown elements, applying styles recursively.
     *
     * @param elements The list of Markdown elements (typically inline) to render.
     * @param renderer The main renderer instance for accessing context like stylesheet.
     * @param baseTextStyle The base text style to apply to the entire string.
     * @return The fully built AnnotatedString.
     */
    fun buildAnnotatedString(
        elements: List<MarkdownElement>,
        renderer: ComposeMarkdownRenderer,
        baseTextStyle: TextStyle
    ): AnnotatedString {
        return buildAnnotatedString {
            // Apply the base style to the entire paragraph first
            withStyle(baseTextStyle.toSpanStyle()) {
                renderChildren(renderer, this, elements)
            }
        }
    }

    /**
     * Renders child elements into an AnnotatedString.Builder, applying nested styles.
 * Handles basic inline styling elements recursively within an existing builder context.
 * This function itself is NOT @Composable as it only manipulates the builder.
 * Made private as it's mainly used by buildAnnotatedString.
 */
// Removed @Composable annotation
internal fun renderChildren( // Changed visibility to internal
    renderer: ComposeMarkdownRenderer, // Pass the main renderer for context (stylesheet, footnote map)
    builder: AnnotatedString.Builder,
    children: List<MarkdownElement>
) {
    children.forEach { child ->
        when (child) {
            is MarkdownTextElement -> builder.append(child.text)
            is BoldElement -> builder.withStyle(renderer.styleSheet.boldTextStyle.toSpanStyle()) { // Use toSpanStyle()
                renderChildren(renderer, this, child.children) // Recursive call
            }
            is ItalicElement -> builder.withStyle(renderer.styleSheet.italicTextStyle.toSpanStyle()) { // Use toSpanStyle()
                renderChildren(renderer, this, child.children) // Recursive call
            }
            is StrikethroughElement -> builder.withStyle(renderer.styleSheet.strikethroughTextStyle.toSpanStyle()) { // Use toSpanStyle()
                renderChildren(renderer, this, child.children) // Recursive call
            }
            is CodeElement -> if (!child.isBlock) {
                // inlineCodeStyle is already a SpanStyle
                builder.withStyle(renderer.styleSheet.inlineCodeStyle) { builder.append(child.content) }
            } else {
                // Block code shouldn't appear inside AnnotatedString rendering for paragraphs etc.
                 android.util.Log.w("AnnotatedStringRender", "Block CodeElement encountered during inline rendering.")
                 builder.append("[Block Code]") // Placeholder
            }
            is LinkElement -> {
                // Apply style. Annotation (click handling) needs to be added separately
                // where the AnnotatedString is used (e.g., in LinkElementRenderer using ClickableText).
                val linkSpanStyle = SpanStyle(
                    color = renderer.styleSheet.linkStyle.color,
                    textDecoration = renderer.styleSheet.linkStyle.textDecoration
                )
                // We push a tag to identify the link range later if needed for click handling outside this util
                builder.pushStringAnnotation(tag = "URL", annotation = child.url)
                builder.withStyle(linkSpanStyle) {
                    renderChildren(renderer, this, child.children) // Render link text
                }
                builder.pop() // Pop the annotation
            }
            is FootnoteReferenceElement -> {
                // Apply style and superscript. Annotation handled separately.
                val footnoteNumber = renderer.getFootnoteNumber(child.identifier) // Get sequential number
                val text = footnoteNumber.toString()
                // Push annotation for click handling
                builder.pushStringAnnotation(tag = "FOOTNOTE_REF", annotation = child.identifier)
                builder.withStyle(renderer.styleSheet.footnoteReferenceStyle.merge(SpanStyle(baselineShift = BaselineShift.Superscript))) {
                    append(text)
                }
                builder.pop() // Pop the annotation
            }
            // Ignore block elements or elements not typically rendered inside AnnotatedString for paragraphs
            is ImageElement, is ImageLinkElement, is LineBreakElement, is ParagraphElement, is HeaderElement, is ListElement, is ListItemElement, is TaskListItemElement, is BlockQuoteElement, is HorizontalRuleElement, is TableElement, is FootnoteDefinitionElement, is DefinitionListElement -> {
                 android.util.Log.w("AnnotatedStringRender", "Ignoring block element ${child::class.simpleName} during inline rendering.")
                 /* Usually not rendered directly into AnnotatedString for a paragraph */
            }
            else -> {
                 android.util.Log.w("AnnotatedStringRender", "Unsupported element ${child::class.simpleName} during inline rendering.")
                 builder.append(" [${child::class.simpleName}] ") // Placeholder for unsupported
            }
        }
    }
}
}
