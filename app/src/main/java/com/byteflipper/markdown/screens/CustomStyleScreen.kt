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
    val defaults = defaultMarkdownStyleSheet()
    // --- Create customStyleSheet ---
    val customStyleSheet = defaults.copy(
        textStyle = defaults.textStyle.copy(fontSize = 15.sp, lineHeight = 22.sp),
        headerStyle = defaults.headerStyle.copy(
            h1 = defaults.headerStyle.h1.copy(color = MaterialTheme.colorScheme.tertiary),
            h2 = defaults.headerStyle.h2.copy(color = MaterialTheme.colorScheme.secondary),
            bottomPadding = 12.dp
        ),
        listStyle = defaults.listStyle.copy(
            indentPadding = 12.dp,
            bulletChars = listOf("* ", "+ ", "- "),
            itemSpacing = 6.dp
        ),
        taskListItemStyle = defaults.taskListItemStyle.copy(
            checkedTextStyle = SpanStyle(
                textDecoration = TextDecoration.LineThrough,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        ),
        tableStyle = defaults.tableStyle.copy(
            borderColor = MaterialTheme.colorScheme.primary,
            borderThickness = 2.dp,
            cellPadding = 10.dp,
            outerBorderShape = RoundedCornerShape(8.dp)
        ),
        horizontalRuleStyle = defaults.horizontalRuleStyle.copy(
            color = MaterialTheme.colorScheme.error,
            thickness = 2.dp
        ),
        blockQuoteStyle = defaults.blockQuoteStyle.copy(
            backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            verticalBarColor = MaterialTheme.colorScheme.primary,
            verticalBarWidth = 6.dp,
            padding = 12.dp
        ),
        codeBlockStyle = defaults.codeBlockStyle.copy(
            modifier = Modifier.clip(RoundedCornerShape(8.dp)),
            textStyle = defaults.codeBlockStyle.textStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            codeBackground = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            showLanguageLabel = true,
            languageLabelTextStyle = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary),
            languageLabelBackground = MaterialTheme.colorScheme.surfaceVariant,
            languageLabelPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            showInfoBar = true,
            infoBarTextStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            infoBarBackground = MaterialTheme.colorScheme.surfaceVariant,
            infoBarPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            showCopyButton = true,
            copyIconTint = MaterialTheme.colorScheme.primary,
            showLineCount = true,
            showCharCount = true
        ),
        inlineCodeStyle = defaults.inlineCodeStyle.copy(
            background = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
        ),
        linkStyle = defaults.linkStyle.copy(
            color = MaterialTheme.colorScheme.secondary,
            textDecoration = TextDecoration.Underline
        ),
        strikethroughTextStyle = defaults.strikethroughTextStyle.copy(
            color = MaterialTheme.colorScheme.error
        ),
        boldTextStyle = defaults.boldTextStyle.copy(
            color = MaterialTheme.colorScheme.primary
        ),
        italicTextStyle = defaults.italicTextStyle.copy(
            color = MaterialTheme.colorScheme.secondary
        ),
        // --- Custom Footnote Styles ---
        footnoteReferenceStyle = defaults.footnoteReferenceStyle.copy(
            color = MaterialTheme.colorScheme.secondary,
            baselineShift = BaselineShift.None, // Keep it on baseline
            textDecoration = TextDecoration.Underline // Underline the ref number
        ),
        footnoteDefinitionStyle = defaults.footnoteDefinitionStyle.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        ),
        footnoteBlockPadding = 24.dp,
        // --- Spacing ---
        blockSpacing = 20.dp,
        lineBreakSpacing = 10.dp
    )

    MarkdownText(
        markdown = SampleMarkdown.content,
        styleSheet = customStyleSheet,
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
        }
    )
}
