package com.byteflipper.markdown_compose.renderer.builders

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.byteflipper.markdown_compose.model.ImageLinkNode
import com.byteflipper.markdown_compose.model.MarkdownStyleSheet

@Composable
fun ImageLinkComposable(
    node: ImageLinkNode,
    styleSheet: MarkdownStyleSheet,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val interactionSource = remember { MutableInteractionSource() }
    val rememberedLinkUrl = remember(node.linkUrl) { node.linkUrl }

    // Create the temporary ImageNode for the inner composable
    // Using remember here is fine, though direct usage might be slightly simpler
    val rememberedImageUrl = remember(node.imageUrl) { node.imageUrl }
    val rememberedAltText = remember(node.altText) { node.altText }
    val tempImageNode = remember(rememberedAltText, rememberedImageUrl) {
        com.byteflipper.markdown_compose.model.ImageNode(rememberedAltText, rememberedImageUrl)
    }

    Box(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    Log.d("ImageLinkComposable", "Box Clicked: Opening URI: $rememberedLinkUrl")
                    try {
                        uriHandler.openUri(rememberedLinkUrl)
                    } catch (e: Exception) {
                        Log.e("ImageLinkComposable", "Failed to open URI: $rememberedLinkUrl", e)
                    }
                }
            )
    ) {
        // Call ImageComposable, but pass a default Modifier.
        // ImageComposable will then apply styleSheet.imageStyle.modifier internally.
        ImageComposable(
            node = tempImageNode,
            styleSheet = styleSheet,
            modifier = Modifier
        )
    }
}