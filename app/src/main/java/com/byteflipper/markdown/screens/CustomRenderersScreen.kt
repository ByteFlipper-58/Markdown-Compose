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
import com.byteflipper.markdown_compose.renderer.MarkdownRenderer
import kotlinx.coroutines.launch

// --- Custom Renderers Example ---
@Composable
private fun createCustomRenderers(): MarkdownRenderers {
    val defaultRenderers = defaultMarkdownRenderers()
    val defaultStyleSheet = defaultMarkdownStyleSheet() // Need styles for default parts

    return defaultRenderers.copy(
        // Example: Add an emoji before H1 headers
        renderHeader = { node, styleSheet, modifier -> {
            val defaultStyle = when (node.level) {
                1 -> styleSheet.headerStyle.h1
                2 -> styleSheet.headerStyle.h2
                3 -> styleSheet.headerStyle.h3
                4 -> styleSheet.headerStyle.h4
                5 -> styleSheet.headerStyle.h5
                else -> styleSheet.headerStyle.h6
            }
            val prefix = if (node.level == 1) "🚀 " else ""
            val content = MarkdownRenderer.render(node.content, styleSheet, null) // Render content
            Text(
                text = buildAnnotatedString {
                    append(prefix)
                    append(content)
                },
                style = defaultStyle,
                modifier = modifier.padding(bottom = styleSheet.headerStyle.bottomPadding)
            )
        }},

        // Example: Customize BlockQuote with an icon and different background
        renderBlockQuote = { node, styleSheet, modifier, footnoteMap, linkHandler, footnoteClickHandler -> {
            val content = MarkdownRenderer.render(node.content, styleSheet, footnoteMap)
            val quoteColor = MaterialTheme.colorScheme.primary
            val backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

            Row(
                modifier = modifier
                    .drawBehind { // Draw the vertical bar
                        val barWidthPx = styleSheet.blockQuoteStyle.verticalBarWidth.toPx()
                        drawLine(
                            color = quoteColor,
                            start = Offset(barWidthPx / 2f, 0f),
                            end = Offset(barWidthPx / 2f, size.height),
                            strokeWidth = barWidthPx,
                            cap = StrokeCap.Square
                        )
                    }
                    .background(backgroundColor)
                    .padding(
                        start = styleSheet.blockQuoteStyle.verticalBarWidth + styleSheet.blockQuoteStyle.padding, // Padding after bar
                        top = styleSheet.blockQuoteStyle.padding,
                        end = styleSheet.blockQuoteStyle.padding,
                        bottom = styleSheet.blockQuoteStyle.padding
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Quote",
                    tint = quoteColor,
                    modifier = Modifier.size(18.dp).padding(end = 8.dp).align(Alignment.Top)
                )
                // Use ClickableText for the content to handle links/footnotes inside quote
                ClickableText(
                    text = content,
                    style = styleSheet.textStyle.merge(LocalTextStyle.current), // Use base text style
                    onClick = { offset ->
                        content.getStringAnnotations(com.byteflipper.markdown_compose.renderer.builders.Link.URL_TAG, offset, offset).firstOrNull()
                            ?.let { linkHandler(it.item); return@ClickableText }
                        content.getStringAnnotations(com.byteflipper.markdown_compose.renderer.FOOTNOTE_REF_TAG, offset, offset).firstOrNull()
                            ?.let { footnoteClickHandler?.invoke(it.item) }
                    }
                )
            }
        }}
        // Keep other renderers as default
    )
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun CustomRenderersScreen(
    footnotePositions: MutableMap<String, Float>,
    scrollState: ScrollState
) {
    val coroutineScope = rememberCoroutineScope()
    val customRenderers = createCustomRenderers() // Get the custom renderers

    MarkdownText(
        markdown = SampleMarkdown.content,
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        footnotePositions = footnotePositions,
        renderers = customRenderers, // Pass the custom renderers
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
        }
    )
}
