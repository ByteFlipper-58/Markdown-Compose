package com.byteflipper.markdown_compose.renderer.element

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import com.byteflipper.markdown_compose.model.ir.BlockQuoteElement
import com.byteflipper.markdown_compose.renderer.ComposeMarkdownRenderer

/**
 * Renders a block quote element with optional vertical bar and background.
 */
internal object BlockQuoteElementRenderer {
    // Removed @Composable annotation
    fun render(
        renderer: ComposeMarkdownRenderer,
        element: BlockQuoteElement
    ): @Composable () -> Unit = {
        val blockQuoteStyle = renderer.styleSheet.blockQuoteStyle
        val barColor = blockQuoteStyle.verticalBarColor
        val barWidthPx = blockQuoteStyle.verticalBarWidth.value * LocalDensity.current.density

        // TODO: Handle ClickableText for links/footnotes within block quotes if needed.

        Box(
            modifier = Modifier
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
                    top = blockQuoteStyle.padding / 2,
                    end = blockQuoteStyle.padding,
                    bottom = blockQuoteStyle.padding / 2
                )
                .then(
                    if (blockQuoteStyle.backgroundColor != null && blockQuoteStyle.backgroundColor != Color.Transparent) {
                        Modifier.background(blockQuoteStyle.backgroundColor)
                    } else Modifier
                )
                .padding(blockQuoteStyle.padding)
        ) {
            CompositionLocalProvider(LocalTextStyle provides blockQuoteStyle.textStyle) {
                Column { // Use Column to stack children vertically
                    renderer.renderChildren(element.children)
                }
            }
        }
    }
}
