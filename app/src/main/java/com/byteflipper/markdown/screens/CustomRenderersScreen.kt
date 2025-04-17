package com.byteflipper.markdown.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.byteflipper.markdown.SampleMarkdown // Import SampleMarkdown
import com.byteflipper.markdown_compose.MarkdownText
import com.byteflipper.markdown_compose.model.*
import com.byteflipper.markdown_compose.model.defaultMarkdownStyleSheet // <<< ДОБАВЛЕН ИМПОРТ
import com.byteflipper.markdown_compose.renderer.MarkdownRenderer
import kotlinx.coroutines.launch

// Removed createCustomRenderers function

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun CustomRenderersScreen(
    footnotePositions: MutableMap<String, Float>,
    scrollState: ScrollState
) {
    val coroutineScope = rememberCoroutineScope()
    // Removed customRenderers definition

    MarkdownText(
        markdown = SampleMarkdown.content,
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        footnotePositions = footnotePositions,
        // renderers = customRenderers, // Removed custom renderers parameter
        // Use default stylesheet or a custom one if needed
        styleSheet = defaultMarkdownStyleSheet(),
        onLinkClick = { url ->
            Log.d("CustomRenderersScreen", "Link clicked: $url") // Updated Log tag
        },
        onFootnoteReferenceClick = { identifier ->
            Log.d("CustomRenderersScreen", "Footnote reference clicked: [^$identifier]") // Updated Log tag
            // --- Debug Logging ---
            Log.d("CustomRenderersScreen", "Current footnotePositions map: $footnotePositions") // Updated Log tag
            val position = footnotePositions[identifier]
            Log.d("CustomRenderersScreen", "Position for '$identifier': $position") // Updated Log tag
            // --- Scroll Logic ---
            if (position != null) {
                Log.d("CustomRenderersScreen", "Attempting scroll to position: $position for id: $identifier") // Updated Log tag
                coroutineScope.launch {
                    scrollState.animateScrollTo(position.toInt())
                }
            } else {
                Log.w("CustomRenderersScreen", "Position not found for footnote id: $identifier") // Updated Log tag
            }
        },
        scrollState = scrollState
    )
}
