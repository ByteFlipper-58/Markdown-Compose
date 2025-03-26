package com.byteflipper.markdown_compose.model

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class HeaderStyle(
    val h1: TextStyle,
    val h2: TextStyle,
    val h3: TextStyle,
    val h4: TextStyle,
    val h5: TextStyle,
    val h6: TextStyle,
    val bottomPadding: Dp
)

@Immutable
data class ListStyle(
    val bulletChars: List<String> = listOf("•", "◦", "▪"),
    val numberPrefix: (Int) -> String = { "$it. " },
    val indentPadding: Dp,
    val itemSpacing: Dp
)

@Immutable
data class BlockQuoteStyle(
    val textStyle: TextStyle,
    val verticalBarColor: Color? = null,
    val verticalBarWidth: Dp = 4.dp,
    val padding: Dp = 8.dp,
    val backgroundColor: Color? = null
)

@Immutable
data class CodeBlockStyle(
    val textStyle: TextStyle,
    val padding: Dp,
    val backgroundColor: Color
)

@Immutable
data class TableStyle(
    val cellPadding: Dp = 8.dp,
    val borderThickness: Dp = 1.dp,
    val borderColor: Color,
    val outerBorderShape: Shape? = null
)

@Immutable
data class HorizontalRuleStyle(
    val color: Color,
    val thickness: Dp = 1.dp
)

@Immutable
data class LinkStyle(
    val textDecoration: TextDecoration? = TextDecoration.Underline,
    val color: Color
)


/**
 * Defines the visual styling for rendering Markdown content.
 *
 * Provides customization options for various Markdown elements.
 * A default implementation based on MaterialTheme can be obtained using `defaultMarkdownStyleSheet()`.
 */
@Immutable
data class MarkdownStyleSheet(
    val textStyle: TextStyle,
    val boldTextStyle: TextStyle,
    val italicTextStyle: TextStyle,
    val strikethroughTextStyle: TextStyle,
    val headerStyle: HeaderStyle,
    val listStyle: ListStyle,
    val blockQuoteStyle: BlockQuoteStyle,
    val codeBlockStyle: CodeBlockStyle,
    val tableStyle: TableStyle,
    val horizontalRuleStyle: HorizontalRuleStyle,
    val linkStyle: LinkStyle,
    val blockSpacing: Dp = 16.dp,
    val lineBreakSpacing: Dp = 16.dp
)

/**
 * Creates a default [MarkdownStyleSheet] based on the current MaterialTheme.
 *
 * @param textStyle Default text style, defaults to `MaterialTheme.typography.bodyMedium`.
 * @param boldTextStyle Default style for bold text, derived from `textStyle`.
 * @param italicTextStyle Default style for italic text, derived from `textStyle`.
 * @param strikethroughTextStyle Default style for strikethrough text, derived from `textStyle`.
 * @param linkColor Color for links, defaults to `MaterialTheme.colorScheme.primary`.
 * @param codeBackgroundColor Background color for code blocks, defaults to a semi-transparent gray.
 * @param blockQuoteVerticalBarColor Color for the vertical bar in block quotes, defaults to a contrasting color.
 * @param blockQuoteBackgroundColor Background color for block quotes, defaults to transparent.
 * @param dividerColor Color for horizontal rules, defaults to `MaterialTheme.colorScheme.outline`.
 * @param tableBorderColor Color for table borders, defaults to `MaterialTheme.colorScheme.outline`.
 */
@Composable
fun defaultMarkdownStyleSheet(
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    boldTextStyle: TextStyle = textStyle.copy(fontWeight = FontWeight.Bold),
    italicTextStyle: TextStyle = textStyle.copy(fontStyle = FontStyle.Italic),
    strikethroughTextStyle: TextStyle = textStyle.copy(textDecoration = TextDecoration.LineThrough),
    linkColor: Color = MaterialTheme.colorScheme.primary,
    codeBackgroundColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
    blockQuoteVerticalBarColor: Color = MaterialTheme.colorScheme.outline,
    blockQuoteBackgroundColor: Color = Color.Transparent,
    dividerColor: Color = MaterialTheme.colorScheme.outline,
    tableBorderColor: Color = MaterialTheme.colorScheme.outline
): MarkdownStyleSheet {
    val resolvedTextColor = textStyle.color.takeOrElse { MaterialTheme.colorScheme.onSurface }
    val baseTextStyle = textStyle.copy(color = resolvedTextColor)

    return remember(
        baseTextStyle,
        boldTextStyle,
        italicTextStyle,
        strikethroughTextStyle,
        linkColor,
        codeBackgroundColor,
        blockQuoteVerticalBarColor,
        blockQuoteBackgroundColor,
        dividerColor,
        tableBorderColor
    ) {
        MarkdownStyleSheet(
            textStyle = baseTextStyle,
            boldTextStyle = boldTextStyle.merge(TextStyle(color = baseTextStyle.color)),
            italicTextStyle = italicTextStyle.merge(TextStyle(color = baseTextStyle.color, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Cursive)),
            strikethroughTextStyle = strikethroughTextStyle.merge(TextStyle(color = baseTextStyle.color)),
            headerStyle = HeaderStyle(
                h1 = baseTextStyle.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                h2 = baseTextStyle.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                h3 = baseTextStyle.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                h4 = baseTextStyle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                h5 = baseTextStyle.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                h6 = baseTextStyle.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                bottomPadding = 8.dp
            ),
            listStyle = ListStyle(
                indentPadding = 8.dp,
                itemSpacing = 4.dp
            ),
            blockQuoteStyle = BlockQuoteStyle(
                textStyle = baseTextStyle.copy(fontStyle = FontStyle.Italic), // Default italic for quotes
                verticalBarColor = blockQuoteVerticalBarColor,
                verticalBarWidth = 4.dp,
                padding = 8.dp,
                backgroundColor = blockQuoteBackgroundColor
            ),
            codeBlockStyle = CodeBlockStyle(
                textStyle = baseTextStyle.merge(TextStyle(fontFamily = FontFamily.Monospace)),
                padding = 8.dp,
                backgroundColor = codeBackgroundColor
            ),
            tableStyle = TableStyle(
                cellPadding = 8.dp,
                borderThickness = 1.dp,
                borderColor = tableBorderColor,
                outerBorderShape = null
            ),
            horizontalRuleStyle = HorizontalRuleStyle(
                color = dividerColor,
                thickness = 1.dp
            ),
            linkStyle = LinkStyle(
                textDecoration = TextDecoration.Underline,
                color = linkColor
            ),
            blockSpacing = 16.dp,
            lineBreakSpacing = 16.dp
        )
    }
}