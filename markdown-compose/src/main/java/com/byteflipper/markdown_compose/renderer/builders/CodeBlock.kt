package com.byteflipper.markdown_compose.renderer.builders

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.byteflipper.markdown_compose.R
import com.byteflipper.markdown_compose.model.CodeNode
import com.byteflipper.markdown_compose.model.MarkdownStyleSheet
import com.byteflipper.markdown_compose.renderer.MarkdownRenderer

private const val TAG = "CodeBlockRenderer"

/**
 * Renders a CodeNode representing a code block (``` ```) with optional language label,
 * copy button, and info bar, based on the provided MarkdownStyleSheet.
 */
@Composable
fun CodeBlockComposable(
    node: CodeNode,
    styleSheet: MarkdownStyleSheet,
    modifier: Modifier = Modifier
) {
    if (!node.isBlock) {
        Log.w(TAG, "CodeBlockComposable called with an inline CodeNode. This should not happen.")
        Text(
            text = MarkdownRenderer.render(listOf(node), styleSheet),
            modifier = modifier,
            style = styleSheet.textStyle
        )
        return
    }

    val codeBlockStyle = styleSheet.codeBlockStyle
    val showTopBar = codeBlockStyle.showLanguageLabel && !node.language.isNullOrBlank()
    val showBottomBar = codeBlockStyle.showInfoBar && (codeBlockStyle.showCopyButton || codeBlockStyle.showLineCount || codeBlockStyle.showCharCount)

    // Outer container styling (e.g., rounded corners) should be applied via codeBlockStyle.modifier
    // Or applied here wrapping the Column if modifier is complex. Let's use Column background for simplicity for now.
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(codeBlockStyle.codeBackground)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f)))
    ) {
        // 1. Top Bar: Language Label
        if (showTopBar) {
            LanguageLabel(
                language = node.language ?: "",
                textStyle = codeBlockStyle.languageLabelTextStyle,
                backgroundColor = codeBlockStyle.languageLabelBackground,
                padding = codeBlockStyle.languageLabelPadding
            )
        }

        // 2. Code Content Area
        CodeContent(
            code = node.code,
            textStyle = codeBlockStyle.textStyle,
            padding = codeBlockStyle.contentPadding,
        )

        // 3. Bottom Bar: Info (Copy, Lines, Chars)
        if (showBottomBar) {
            InfoBar(
                code = node.code,
                textStyle = codeBlockStyle.infoBarTextStyle,
                backgroundColor = codeBlockStyle.infoBarBackground,
                padding = codeBlockStyle.infoBarPadding,
                showCopy = codeBlockStyle.showCopyButton,
                showLines = codeBlockStyle.showLineCount,
                showChars = codeBlockStyle.showCharCount,
                iconTint = codeBlockStyle.copyIconTint
            )
        }
    }
}


@Composable
private fun LanguageLabel(
    language: String,
    textStyle: TextStyle,
    backgroundColor: Color,
    padding: PaddingValues
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(padding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = language.uppercase(), style = textStyle)
    }
}


@Composable
private fun CodeContent(
    code: String,
    textStyle: TextStyle,
    padding: PaddingValues,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(padding)
    ) {
        Text(
            text = code,
            style = textStyle,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@Composable
private fun InfoBar(
    code: String,
    textStyle: TextStyle,
    backgroundColor: Color,
    padding: PaddingValues,
    showCopy: Boolean,
    showLines: Boolean,
    showChars: Boolean,
    iconTint: Color
) {
    val clipboardManager = LocalClipboardManager.current
    var lineCount by remember(code) { mutableStateOf(0) }
    var charCount by remember(code) { mutableStateOf(0) }

    // Calculate counts - LaunchedEffect avoids recalculation on every recomposition
    LaunchedEffect(code) {
        lineCount = code.lines().count { it.isNotEmpty() || code.endsWith('\n') }
        if (code.isEmpty()) lineCount = 0
        charCount = code.length
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(padding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left side: Info (Lines/Chars)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (showLines) {
                Text(text = "$lineCount Lines", style = textStyle)
            }
            if (showChars) {
                if (showLines) Text("|", style = textStyle)
                Text(text = "$charCount Chars", style = textStyle)
            }
        }

        if (showCopy) {
            IconButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(code))
                    Log.d(TAG, "Code copied to clipboard.")
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.content_copy_24px),
                    contentDescription = "Copy code",
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}