// markdown-compose/src/main/java/com/byteflipper/markdown_compose/renderer/builders/BlockQuote.kt
package com.byteflipper.markdown_compose.renderer.builders

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.withStyle
import com.byteflipper.markdown_compose.model.BlockQuoteNode
import com.byteflipper.markdown_compose.model.MarkdownStyleSheet
import com.byteflipper.markdown_compose.renderer.FOOTNOTE_REF_TAG // Import footnote tag
import com.byteflipper.markdown_compose.renderer.MarkdownRenderer

private const val TAG = "BlockQuoteRenderer"

object BlockQuote {
    /**
     * Renders block quote content into an AnnotatedString builder.
     * Ensures footnote map is passed down for correct nested rendering.
     */
    fun render(
        builder: AnnotatedString.Builder,
        node: BlockQuoteNode,
        styleSheet: MarkdownStyleSheet,
        footnoteReferenceMap: Map<String, Int>? // Add map parameter
    ) {
        val blockQuoteStyle = styleSheet.blockQuoteStyle
        // Use the quote's text style as the base for its content
        val contentStyleSheet = styleSheet.copy(textStyle = blockQuoteStyle.textStyle)

        with(builder) {
            withStyle(blockQuoteStyle.textStyle.toSpanStyle()) {
                node.content.forEach { contentNode ->
                    // Pass the map down when rendering children
                    MarkdownRenderer.renderNode(this, contentNode, contentStyleSheet, footnoteReferenceMap)
                }
            }
        }
    }
}

/** BlockQuoteComposable remains unchanged as it already passed the map correctly to MarkdownRenderer.render */
@Composable
fun BlockQuoteComposable(
    node: BlockQuoteNode,
    styleSheet: MarkdownStyleSheet,
    modifier: Modifier = Modifier,
    footnoteReferenceMap: Map<String, Int>?,
    linkHandler: (url: String) -> Unit,
    onFootnoteReferenceClick: ((identifier: String) -> Unit)?
) {
    val blockQuoteStyle = styleSheet.blockQuoteStyle
    val barColor = blockQuoteStyle.verticalBarColor
    val barWidthPx = blockQuoteStyle.verticalBarWidth.value * LocalDensity.current.density

    // This call correctly passes the map already
    val contentString = MarkdownRenderer.render(
        node.content,
        styleSheet.copy(textStyle = blockQuoteStyle.textStyle),
        footnoteReferenceMap
    )

    Box(
        modifier = modifier
            // ... (drawing and padding modifiers as before) ...
            .fillMaxWidth()
            .then( // Apply vertical bar modifier if needed
                if (barColor != null && barWidthPx > 0f) {
                    Modifier.drawBehind {
                        drawLine(
                            brush = SolidColor(barColor),
                            start = Offset(barWidthPx / 2f, 0f), // Center of the bar stroke
                            end = Offset(barWidthPx / 2f, size.height),
                            strokeWidth = barWidthPx
                        )
                    }
                } else Modifier // Otherwise, no bar drawing
            )
            .padding( // Outer padding, includes space for the bar
                start = if (barColor != null) blockQuoteStyle.verticalBarWidth + blockQuoteStyle.padding else blockQuoteStyle.padding,
                top = blockQuoteStyle.padding / 2,
                end = blockQuoteStyle.padding,
                bottom = blockQuoteStyle.padding / 2
            )
            .then( // Apply background modifier if needed
                if (blockQuoteStyle.backgroundColor != null && blockQuoteStyle.backgroundColor != Color.Transparent) {
                    Modifier.background(blockQuoteStyle.backgroundColor)
                } else Modifier // Otherwise, no background
            )
            .padding(blockQuoteStyle.padding) // Apply inner padding AFTER background/bar space is accounted for

    ) {
        ClickableText(
            text = contentString,
            style = blockQuoteStyle.textStyle,
            onClick = { offset ->
                contentString.getStringAnnotations(Link.URL_TAG, offset, offset).firstOrNull()?.let {
                    Log.i(TAG, "Link clicked in BlockQuote: ${it.item}")
                    linkHandler(it.item)
                    return@ClickableText
                }
                contentString.getStringAnnotations(FOOTNOTE_REF_TAG, offset, offset).firstOrNull()?.let {
                    Log.i(TAG, "Footnote ref [^${it.item}] clicked in BlockQuote")
                    onFootnoteReferenceClick?.invoke(it.item)
                }
            }
        )
    }
}