package com.byteflipper.markdown_compose.renderer.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

/**
 * Object responsible for rendering horizontal rules (dividers) in Markdown.
 */
object HorizontalRule {

    /**
     * Renders a horizontal rule (divider) using Jetpack Compose's Canvas API.
     *
     * @param color The color of the horizontal rule.
     * @param thickness The thickness of the rule.
     * @param modifier Modifier for layout adjustments.
     * @param style The style of the rule: "solid", "dashed", or "dotted" (optional).
     */
    @Composable
    fun Render(
        color: Color,
        thickness: Dp,
        modifier: Modifier = Modifier,
        style: String = "solid"
    ) {
        val pathEffect = when (style) {
            "dashed" -> PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            "dotted" -> PathEffect.dashPathEffect(floatArrayOf(2f, 5f), 0f)
            else -> null
        }
        val thicknessPx = with(LocalDensity.current) { thickness.toPx() }

        Canvas(
            modifier = modifier
                .fillMaxWidth()
                .height(thickness)
        ) {
            drawLine(
                color = color,
                start = Offset(0f, size.height / 2f),
                end = Offset(size.width, size.height / 2f),
                strokeWidth = thicknessPx,
                pathEffect = pathEffect,
                cap = StrokeCap.Butt
            )
        }
    }
}