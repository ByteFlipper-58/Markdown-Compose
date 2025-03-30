package com.byteflipper.markdown_compose.renderer.builders

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import com.byteflipper.markdown_compose.model.ImageNode
import com.byteflipper.markdown_compose.model.MarkdownStyleSheet

@Composable
fun ImageComposable(
    node: ImageNode,
    styleSheet: MarkdownStyleSheet,
    modifier: Modifier = Modifier // Получает модификатор (в тесте выше - пустой)
) {
    val imageStyle = styleSheet.imageStyle
    // Важно: Используем полученный modifier!
    val finalModifier = modifier             // Модификатор от вызывающей стороны (Box его не передал в тесте)
        .then(imageStyle.modifier) // Применяем padding/размер из стиля
        .clip(imageStyle.shape)    // Применяем обрезку

    Log.d("ImageComposable", "[Тест с Box] Применяем к AsyncImage: url='${node.url}', finalModifier='$finalModifier'")

    AsyncImage(
        model = node.url,
        contentDescription = node.altText,
        modifier = finalModifier,
        contentScale = imageStyle.contentScale,
        placeholder = imageStyle.placeholder,
        error = imageStyle.error
    )
}