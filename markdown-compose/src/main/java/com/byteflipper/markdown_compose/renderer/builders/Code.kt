package com.byteflipper.markdown_compose.renderer.builders

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.withStyle
import com.byteflipper.markdown_compose.model.CodeNode
import com.byteflipper.markdown_compose.model.MarkdownStyleSheet

object Code {
    /**
     * Renders inline or block code using the styles from the stylesheet.
     * Assumes inline code if it doesn't contain newlines, otherwise applies block padding/background.
     * Note: True block code (```) rendering might be better handled by a dedicated composable in MarkdownText.
     */
    fun render(builder: AnnotatedString.Builder, node: CodeNode, styleSheet: MarkdownStyleSheet) {
        val codeStyle = styleSheet.codeBlockStyle
        val isLikelyBlock = node.code.contains('\n')

        builder.withStyle(
            codeStyle.textStyle
                .copy(background = codeStyle.backgroundColor)
                .toSpanStyle()
        ) {append(node.code)
        }
    }
}