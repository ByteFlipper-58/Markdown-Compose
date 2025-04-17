package com.byteflipper.markdown_compose.renderer.element

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import com.byteflipper.markdown_compose.model.ir.ImageElement
import com.byteflipper.markdown_compose.renderer.ComposeMarkdownRenderer

/**
 * Renders an image element using Coil's AsyncImage.
 */
internal object ImageElementRenderer {
    // Removed @Composable annotation
    fun render(
        renderer: ComposeMarkdownRenderer,
        element: ImageElement
    ): @Composable () -> Unit = {
        val imageStyle = renderer.styleSheet.imageStyle
        val finalModifier = Modifier // Start with empty, apply style modifier later
            .then(imageStyle.modifier)
            .clip(imageStyle.shape)

        Log.d("ImageElementRenderer", "Rendering Image: url='${element.url}', modifier='$finalModifier'")

        AsyncImage(
            model = element.url,
            contentDescription = element.altText,
            modifier = finalModifier,
            contentScale = imageStyle.contentScale,
            placeholder = imageStyle.placeholder,
            error = imageStyle.error
        )
    }
}
