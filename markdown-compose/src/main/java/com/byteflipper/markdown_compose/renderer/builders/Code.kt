package com.byteflipper.markdown_compose.renderer.builders

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.withStyle
import com.byteflipper.markdown_compose.model.CodeNode
import com.byteflipper.markdown_compose.model.MarkdownStyleSheet
import android.util.Log

private const val TAG = "CodeRenderer"

object Code {
    /**
     * Renders ONLY inline code (` `) using the styles from the stylesheet.
     * Block code (``` ```) is handled by CodeBlockComposable.
     */
    fun render(builder: AnnotatedString.Builder, node: CodeNode, styleSheet: MarkdownStyleSheet) {
        if (!node.isBlock) {
            builder.withStyle(styleSheet.inlineCodeStyle) {
                append(node.code)
            }
        } else {
            Log.w(TAG, "Block code node encountered in inline rendering path: '${node.code.take(20)}...'")
        }
    }
}