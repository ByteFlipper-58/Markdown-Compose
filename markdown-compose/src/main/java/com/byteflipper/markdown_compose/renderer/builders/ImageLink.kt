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
import com.byteflipper.markdown_compose.model.ImageNode
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

    // Запоминаем URL и alt
    val rememberedImageUrl = remember(node.imageUrl) { node.imageUrl }
    val rememberedAltText = remember(node.altText) { node.altText }
    val tempImageNode = remember(rememberedAltText, rememberedImageUrl) {
        com.byteflipper.markdown_compose.model.ImageNode(rememberedAltText, rememberedImageUrl)
    }

    Box(
        modifier = modifier.clickable( // Применяем clickable к Box
            interactionSource = interactionSource,
            indication = null,
            onClick = {
                Log.d("ImageLinkComposable", "Box Clicked: Открываем URI: $rememberedLinkUrl")
                try {
                    uriHandler.openUri(rememberedLinkUrl)
                } catch (e: Exception) {
                    Log.e("ImageLinkComposable", "Не удалось открыть URI: $rememberedLinkUrl", e)
                }
            }
        )
    ) {
        // В ImageComposable передаем модификатор Modifier (пустой по умолчанию)
        // или только тот, что пришел из MarkdownText, но *без* clickable
        ImageComposable(
            node = tempImageNode,
            styleSheet = styleSheet,
            // modifier = Modifier // <--- Передаем пустой, если не нужны стили из MarkdownText
            // Либо передаем modifier, но УБЕДИТЕСЬ, что он не содержит clickable здесь
            modifier = Modifier // ПРОСТО ДЛЯ ТЕСТА! Стили imageStyle будут применены внутри ImageComposable
        )
    }
}