package com.byteflipper.markdown_compose.renderer.builders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
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



object BlockQuote {
    /**
     * Renders a block quote into an AnnotatedString builder.
     * This version primarily applies text style and potentially a simple prefix.
     * More complex background/border requires a dedicated Composable.
     */
    fun render(builder: AnnotatedString.Builder, node: BlockQuoteNode, styleSheet: MarkdownStyleSheet) {
        val blockQuoteStyle = styleSheet.blockQuoteStyle
       with(builder) {
            withStyle(blockQuoteStyle.textStyle.toSpanStyle()) {
               node.content.forEach { contentNode ->
                    MarkdownRenderer.renderNode(this, contentNode, styleSheet.copy(textStyle = blockQuoteStyle.textStyle))
                }
            }
        }
    }
}

/**
 * Renders a BlockQuoteNode using a dedicated Composable for better control over
 * background, padding, and the vertical bar.
 */
@Composable
fun BlockQuoteComposable(
    node: BlockQuoteNode,
    styleSheet: MarkdownStyleSheet,
    modifier: Modifier = Modifier
) {
    val blockQuoteStyle = styleSheet.blockQuoteStyle
    val barColor = blockQuoteStyle.verticalBarColor
    val barWidthPx = blockQuoteStyle.verticalBarWidth.value * LocalDensity.current.density // Convert Dp to Px

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
            .padding(start = if (barColor != null) blockQuoteStyle.verticalBarWidth + blockQuoteStyle.padding else blockQuoteStyle.padding,
                top = blockQuoteStyle.padding / 2,
                end = blockQuoteStyle.padding,
                bottom = blockQuoteStyle.padding / 2)
            .then(
                if(blockQuoteStyle.backgroundColor != null && blockQuoteStyle.backgroundColor != Color.Transparent) {
                    Modifier.background(blockQuoteStyle.backgroundColor)
                } else Modifier
            )

    ) {
        Text(
            text = MarkdownRenderer.render(node.content, styleSheet.copy(textStyle = blockQuoteStyle.textStyle)),
            style = blockQuoteStyle.textStyle
        )
    }
}