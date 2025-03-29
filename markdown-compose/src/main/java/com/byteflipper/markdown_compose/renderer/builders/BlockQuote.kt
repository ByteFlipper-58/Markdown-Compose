package com.byteflipper.markdown_compose.renderer.builders

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText // Import ClickableText
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
import com.byteflipper.markdown_compose.renderer.MarkdownRenderer

private const val TAG = "BlockQuoteRenderer"

object BlockQuote {
    /**
     * Renders block quote *content* into an AnnotatedString builder.
     * This is used when the block quote itself isn't the top-level element being rendered.
     */
    fun render(builder: AnnotatedString.Builder, node: BlockQuoteNode, styleSheet: MarkdownStyleSheet) {
        val blockQuoteStyle = styleSheet.blockQuoteStyle
        val contentStyleSheet = styleSheet.copy(textStyle = blockQuoteStyle.textStyle) // Apply BQ text style to children

        with(builder) {
            withStyle(blockQuoteStyle.textStyle.toSpanStyle()) {
                node.content.forEach { contentNode ->
                    MarkdownRenderer.renderNode(this, contentNode, contentStyleSheet)
                }
            }
        }
    }
}

/**
 * Renders a BlockQuoteNode using a dedicated Composable with background, padding, vertical bar,
 * and clickable link support.
 *
 * @param node The BlockQuoteNode to render.
 * @param styleSheet The stylesheet.
 * @param modifier Modifier for the outer Box.
 * @param linkHandler The callback to handle link clicks within the block quote.
 */
@Composable
fun BlockQuoteComposable(
    node: BlockQuoteNode,
    styleSheet: MarkdownStyleSheet,
    modifier: Modifier = Modifier,
    linkHandler: (url: String) -> Unit // Pass handler
) {
    val blockQuoteStyle = styleSheet.blockQuoteStyle
    val barColor = blockQuoteStyle.verticalBarColor
    val barWidthPx = blockQuoteStyle.verticalBarWidth.value * LocalDensity.current.density

    // Prepare the text content once
    val contentString = MarkdownRenderer.render(
        node.content,
        styleSheet.copy(textStyle = blockQuoteStyle.textStyle) // Apply BQ text style to children
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (barColor != null && barWidthPx > 0f) {
                    Modifier.drawBehind {
                        drawLine(
                            brush = SolidColor(barColor),
                            start = Offset(barWidthPx / 2f, 0f),
                            end = Offset(barWidthPx / 2f, size.height),
                            strokeWidth = barWidthPx
                        )
                    }
                } else Modifier
            )
            .padding(
                start = if (barColor != null) blockQuoteStyle.verticalBarWidth + blockQuoteStyle.padding else blockQuoteStyle.padding,
                top = blockQuoteStyle.padding / 2, // Adjust padding if needed
                end = blockQuoteStyle.padding,
                bottom = blockQuoteStyle.padding / 2
            )
            .then(
                if (blockQuoteStyle.backgroundColor != null && blockQuoteStyle.backgroundColor != Color.Transparent) {
                    Modifier.background(blockQuoteStyle.backgroundColor)
                } else Modifier
            )
            .padding(blockQuoteStyle.padding) // Apply inner padding AFTER background/border

    ) {
        // Use ClickableText to render the content
        ClickableText(
            text = contentString,
            style = blockQuoteStyle.textStyle, // Apply the BQ's specific text style
            onClick = { offset ->
                contentString
                    .getStringAnnotations(Link.URL_TAG, offset, offset)
                    .firstOrNull()?.let { annotation ->
                        Log.i(TAG, "Link clicked in BlockQuote: ${annotation.item}")
                        linkHandler(annotation.item) // Use the passed handler
                    }
            }
        )
    }
}