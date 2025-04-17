package com.byteflipper.markdown_compose.renderer.element

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.byteflipper.markdown_compose.model.ir.ImageElement
import com.byteflipper.markdown_compose.model.ir.ImageLinkElement
import com.byteflipper.markdown_compose.renderer.ComposeMarkdownRenderer

/**
 * Renders an image that acts as a hyperlink.
 */
internal object ImageLinkElementRenderer {
    // Removed @Composable annotation
    fun render(
        renderer: ComposeMarkdownRenderer,
        element: ImageLinkElement
    ): @Composable () -> Unit = {
        val uriHandler = LocalUriHandler.current
        val interactionSource = remember { MutableInteractionSource() }
        // Create the inner ImageElement to delegate rendering
        val imageElement = ImageElement(url = element.imageUrl, altText = element.altText)

        Box(
            modifier = Modifier // Apply outer modifier if needed
                .clickable(
                    interactionSource = interactionSource,
                    indication = null, // No visual indication for clicks on images
                    onClick = {
                        Log.d("ImageLinkElementRenderer", "ImageLink clicked: Opening URI: ${element.linkUrl}")
                        try {
                            uriHandler.openUri(element.linkUrl)
                        } catch (e: Exception) {
                            Log.e("ImageLinkElementRenderer", "Failed to open URI: ${element.linkUrl}", e)
                        }
                    }
                )
        ) {
            // Delegate the actual image rendering to ImageElementRenderer
            // Need to access the render function of ImageElementRenderer
            // This assumes ImageElementRenderer is accessible or we call renderer.renderImage
            renderer.renderImage(imageElement)()
        }
    }
}
