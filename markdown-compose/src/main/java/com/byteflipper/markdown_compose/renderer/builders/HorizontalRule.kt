package com.byteflipper.markdown_compose.renderer.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.byteflipper.markdown_compose.model.HorizontalRuleNode

object HorizontalRule {

    /**
     * Renders a horizontal rule (divider) on a Canvas.
     *
     * @param color The color of the horizontal rule
     * @param style The style of the rule: "solid", "dashed", or "dotted"
     * @param thickness The thickness of the rule in dp
     */
    @Composable
    fun Render(
        color: Color,
        style: String = "solid",
        thickness: Float = 1f
    ) {
        val pathEffect = when (style) {
            "dashed" -> PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            "dotted" -> PathEffect.dashPathEffect(floatArrayOf(2f, 5f), 0f)
            else -> null // solid line
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(thickness.dp)
                .padding(vertical = 8.dp)
        ) {
            // For a solid line without path effect
            if (pathEffect == null) {
                drawLine(
                    color = color,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = thickness * density,
                    cap = StrokeCap.Round
                )
            } else {
                // For dashed or dotted lines with path effect
                drawLine(
                    color = color,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = thickness * density,
                    pathEffect = pathEffect,
                    cap = StrokeCap.Round
                )
            }
        }
    }

    /**
     * Alternative implementation that can render different styles of horizontal rules
     * based on the Markdown syntax used (---, ***, ___)
     */
    @Composable
    fun RenderByType(
        node: HorizontalRuleNode,
        color: Color,
        markdownSource: String? = null
    ) {
        // Determine style based on the Markdown source
        val style = when {
            markdownSource?.contains("---") == true -> "solid"
            markdownSource?.contains("***") == true -> "dashed"
            markdownSource?.contains("___") == true -> "dotted"
            else -> "solid" // Default
        }

        Render(color = color, style = style)
    }
}