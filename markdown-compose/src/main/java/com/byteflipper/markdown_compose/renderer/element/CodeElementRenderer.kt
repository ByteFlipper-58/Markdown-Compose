package com.byteflipper.markdown_compose.renderer.element

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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.byteflipper.markdown_compose.R
import com.byteflipper.markdown_compose.model.ir.CodeElement
import com.byteflipper.markdown_compose.renderer.ComposeMarkdownRenderer

/**
 * Renders both inline code spans and fenced code blocks.
 */
internal object CodeElementRenderer {
    // Removed @Composable annotation
    fun render(
        renderer: ComposeMarkdownRenderer,
        element: CodeElement
    ): @Composable () -> Unit = {
        if (element.isBlock) {
            RenderCodeBlock(renderer, element)
        } else {
            RenderInlineCode(renderer, element)
        }
    }

    @Composable
    private fun RenderInlineCode(
        renderer: ComposeMarkdownRenderer,
        element: CodeElement
    ) {
        val annotatedString = buildAnnotatedString {
            withStyle(renderer.styleSheet.inlineCodeStyle) {
                append(element.content)
            }
        }
        Text(text = annotatedString)
    }

    @Composable
    private fun RenderCodeBlock(
        renderer: ComposeMarkdownRenderer,
        element: CodeElement
    ) {
        val codeBlockStyle = renderer.styleSheet.codeBlockStyle
        val showTopBar = codeBlockStyle.showLanguageLabel && !element.language.isNullOrBlank()
        val showBottomBar = codeBlockStyle.showInfoBar && (codeBlockStyle.showCopyButton || codeBlockStyle.showLineCount || codeBlockStyle.showCharCount)

        Column(
            modifier = Modifier // Use renderer's modifier? Or styleSheet's? Needs clarification.
                .fillMaxWidth()
                .background(codeBlockStyle.codeBackground)
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))
            // .then(codeBlockStyle.modifier) // Apply custom modifier from style
        ) {
            if (showTopBar) {
                LanguageLabel(
                    language = element.language ?: "",
                    textStyle = codeBlockStyle.languageLabelTextStyle,
                    backgroundColor = codeBlockStyle.languageLabelBackground,
                    padding = codeBlockStyle.languageLabelPadding
                )
            }
            CodeContent(
                code = element.content,
                textStyle = codeBlockStyle.textStyle,
                padding = codeBlockStyle.contentPadding,
            )
            if (showBottomBar) {
                InfoBar(
                    code = element.content,
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

    // --- Helper Composables (Copied from previous ComposeMarkdownRenderer implementation) ---

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
                .horizontalScroll(rememberScrollState())
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (showLines) Text(text = "$lineCount Lines", style = textStyle)
                if (showChars) {
                    if (showLines) Text("|", style = textStyle)
                    Text(text = "$charCount Chars", style = textStyle)
                }
            }
            if (showCopy) {
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(code))
                        Log.d("CodeElementRenderer", "Code copied to clipboard.")
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
}
