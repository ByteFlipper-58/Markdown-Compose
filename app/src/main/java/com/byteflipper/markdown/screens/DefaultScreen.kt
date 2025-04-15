package com.byteflipper.markdown.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.byteflipper.markdown.SampleMarkdown // Import SampleMarkdown
import com.byteflipper.markdown_compose.MarkdownText
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun DefaultScreen(
    footnotePositions: MutableMap<String, Float>, // Map for MarkdownText to update
    scrollState: ScrollState // State to perform scroll
) {
    val coroutineScope = rememberCoroutineScope()
    MarkdownText(
        markdown = SampleMarkdown.content,
        modifier = Modifier.fillMaxWidth().padding(8.dp), // Inner padding
        footnotePositions = footnotePositions, // Pass map
        onLinkClick = { url ->
            Log.d("DefaultScreen", "Link clicked: $url") // Updated Log tag
            // Handle link click if needed (default handler already opens browser)
        },
        onFootnoteReferenceClick = { identifier ->
            Log.d("DefaultScreen", "Footnote reference clicked: [^$identifier]") // Updated Log tag
            // --- Debug Logging ---
            Log.d("DefaultScreen", "Current footnotePositions map: $footnotePositions") // Updated Log tag
            val position = footnotePositions[identifier]
            Log.d("DefaultScreen", "Position for '$identifier': $position") // Updated Log tag
            // --- Scroll Logic ---
            if (position != null) {
                Log.d("DefaultScreen", "Attempting scroll to position: $position for id: $identifier") // Updated Log tag
                coroutineScope.launch {
                    scrollState.animateScrollTo(position.toInt()) // Scroll to the measured Y position
                }
            } else {
                Log.w("DefaultScreen", "Position not found for footnote id: $identifier") // Updated Log tag
            }
        }
    )
}
