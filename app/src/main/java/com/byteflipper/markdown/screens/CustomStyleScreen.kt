package com.byteflipper.markdown.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byteflipper.markdown.SampleMarkdown // Import SampleMarkdown
import com.byteflipper.markdown_compose.MarkdownText
import com.byteflipper.markdown_compose.model.defaultMarkdownStyleSheet
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun CustomStyleScreen(
    footnotePositions: MutableMap<String, Float>,
    scrollState: ScrollState
) {
    val coroutineScope = rememberCoroutineScope()
    // Removed customStyleSheet definition

    MarkdownText(
        markdown = SampleMarkdown.content,
        // styleSheet = customStyleSheet, // Removed custom stylesheet
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        footnotePositions = footnotePositions, // Pass map
        onLinkClick = { url ->
            Log.d("CustomStyleScreen", "Link clicked: $url") // Updated Log tag
        },
        onFootnoteReferenceClick = { identifier ->
            Log.d("CustomStyleScreen", "Footnote reference clicked: [^$identifier]") // Updated Log tag
            // --- Debug Logging ---
            Log.d("CustomStyleScreen", "Current footnotePositions map: $footnotePositions") // Updated Log tag
            val position = footnotePositions[identifier]
            Log.d("CustomStyleScreen", "Position for '$identifier': $position") // Updated Log tag
            // --- Scroll Logic ---
            if (position != null) {
                Log.d("CustomStyleScreen", "Attempting scroll to position: $position for id: $identifier") // Updated Log tag
                coroutineScope.launch {
                    scrollState.animateScrollTo(position.toInt())
                }
            } else {
                Log.w("CustomStyleScreen", "Position not found for footnote id: $identifier") // Updated Log tag
            }
        },
        scrollState = scrollState
    )
}
