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
    // footnotePositions removed, no longer needed here
    scrollState: ScrollState // State to perform scroll
) {
    val coroutineScope = rememberCoroutineScope() // Scope for default click handler

    MarkdownText(
        markdown = SampleMarkdown.content,
        modifier = Modifier.fillMaxWidth().padding(8.dp), // Inner padding
        scrollState = scrollState, // Pass ScrollState
        coroutineScope = coroutineScope, // Pass CoroutineScope
        // footnotePositions = footnotePositions, // Removed parameter
        onLinkClick = { url ->
            Log.d("DefaultScreen", "Link clicked: $url")
            // Handle link click if needed (default handler already opens browser)
        }
        // onFootnoteReferenceClick is omitted, using the default implementation from MarkdownText
        // which scrolls to the bottom using the provided scrollState and coroutineScope.
    )
}
