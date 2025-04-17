package com.byteflipper.markdown_compose.renderer.element

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import com.byteflipper.markdown_compose.model.ir.HorizontalRuleElement
import com.byteflipper.markdown_compose.renderer.ComposeMarkdownRenderer

/**
 * Renders a horizontal rule element.
 */
internal object HorizontalRuleElementRenderer {
    // Removed @Composable annotation
    fun render(
        renderer: ComposeMarkdownRenderer,
        element: HorizontalRuleElement // Parameter is unused but kept for consistency
    ): @Composable () -> Unit = {
        val hrStyle = renderer.styleSheet.horizontalRuleStyle
        val color = hrStyle.color
        val thickness = hrStyle.thickness
        val style = hrStyle.style

        val pathEffect = when (style) {
            "dashed" -> PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            "dotted" -> PathEffect.dashPathEffect(floatArrayOf(2f, 5f), 0f)
            else -> null // Solid
        }
        val thicknessPx = with(LocalDensity.current) { thickness.toPx() }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(thickness)
                .padding(vertical = renderer.styleSheet.blockSpacing / 2) // Use block spacing from main styleSheet
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
