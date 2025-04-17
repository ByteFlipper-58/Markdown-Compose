package com.byteflipper.markdown_compose.renderer.element

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.byteflipper.markdown_compose.model.ir.LinkElement
// Removed unused imports for MarkdownElement and MarkdownTextElement as they are not directly used here
// import com.byteflipper.markdown_compose.model.ir.MarkdownElement
// import com.byteflipper.markdown_compose.model.ir.MarkdownTextElement
import com.byteflipper.markdown_compose.renderer.util.AnnotatedStringRenderUtil // Added import
import com.byteflipper.markdown_compose.renderer.ComposeMarkdownRenderer

/**
 * Renders a hyperlink element.
 */
internal object LinkElementRenderer {
    private const val URL_TAG = "URL" // Define the tag for annotation

    // Removed @Composable annotation
    fun render(
        renderer: ComposeMarkdownRenderer,
        element: LinkElement
    ): @Composable () -> Unit = {
        val context = LocalContext.current
        val linkStyleModel = renderer.styleSheet.linkStyle
        val textColor = linkStyleModel.color.takeOrElse { renderer.styleSheet.textStyle.color }
        val spanStyle = SpanStyle(
            color = textColor,
            textDecoration = linkStyleModel.textDecoration
        )

        val annotatedString = buildAnnotatedString {
            pushStringAnnotation(tag = URL_TAG, annotation = element.url)
            withStyle(spanStyle) {
                // Use the imported utility function
                AnnotatedStringRenderUtil.renderChildren(renderer, this, element.children)
            }
            pop()
        }

        ClickableText(
            text = annotatedString,
            style = renderer.styleSheet.textStyle, // Base style for the clickable text
            onClick = { offset ->
                annotatedString.getStringAnnotations(tag = URL_TAG, start = offset, end = offset)
                    .firstOrNull()?.let { annotation ->
                        val url = annotation.item
                        Log.d("LinkElementRenderer", "Link clicked: $url")
                        val handler = renderer.onLinkClick ?: { defaultUrl ->
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(defaultUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Log.e("LinkElementRenderer", "Failed to open link: $defaultUrl", e)
                            }
                        }
                        handler(url)
                    }
            }
        )
    }
}
