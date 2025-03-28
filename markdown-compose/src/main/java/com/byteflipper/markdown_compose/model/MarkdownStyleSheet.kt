package com.byteflipper.markdown_compose.model

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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

/** Styles for H1-H6 headers and spacing after them. */
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

/** Styles for ordered and unordered lists. */
@Immutable
data class ListStyle(
    val bulletChars: List<String> = listOf("•", "◦", "▪"),
    val numberPrefix: (Int) -> String = { "$it. " },
    val indentPadding: Dp,
    val itemSpacing: Dp
)

/**
 * Defines styling for task list items ([x], [ ]). Includes styles for both the text
 * content and the checkbox appearance.
 */
@Immutable
data class TaskListItemStyle(
    /** Style applied to the text when the checkbox is checked (e.g., strikethrough, dim color). */
    val checkedTextStyle: SpanStyle? = null,
    /** Style applied to the text when the checkbox is unchecked (usually null to inherit default). */
    val uncheckedTextStyle: SpanStyle? = null,

    // --- Styles for the Checkbox Composable (applied via CheckboxDefaults.colors) ---
    /** The color of the checkmark symbol itself when checked. */
    val checkedCheckboxIndicatorColor: Color? = null,
    /** The background fill color of the checkbox square when checked. */
    val checkedCheckboxContainerColor: Color? = null,
    /** The border color of the checkbox square when unchecked. */
    val uncheckedCheckboxBorderColor: Color? = null,
    /** The color of the checkmark symbol when disabled (reflecting checked state). */
    val disabledCheckboxIndicatorColor: Color? = null,
    /** The background/border color of the checkbox square when disabled (can reflect checked/unchecked). */
    val disabledCheckboxContainerColor: Color? = null,
)

/** Styles for > block quotes. */
@Immutable
data class BlockQuoteStyle(
    val textStyle: TextStyle,
    val verticalBarColor: Color? = null,
    val verticalBarWidth: Dp = 4.dp,
    val padding: Dp = 8.dp,
    val backgroundColor: Color? = null
)

/** Styles for ``` code blocks ```. */
@Immutable
data class CodeBlockStyle(
    val textStyle: TextStyle,
    val modifier: Modifier = Modifier,
    val contentPadding: PaddingValues = PaddingValues(8.dp),
    val codeBackground: Color,

    // Top Language Label Bar
    val showLanguageLabel: Boolean = true,
    val languageLabelTextStyle: TextStyle,
    val languageLabelBackground: Color,
    val languageLabelPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 4.dp),

    // Bottom Info Bar
    val showInfoBar: Boolean = true,
    val infoBarTextStyle: TextStyle,
    val infoBarBackground: Color,
    val infoBarPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    val showCopyButton: Boolean = true,
    val copyIconTint: Color,
    val showLineCount: Boolean = true,
    val showCharCount: Boolean = true
)

/** Styles for | tables |. */
@Immutable
data class TableStyle(
    val cellPadding: Dp = 8.dp,
    val borderThickness: Dp = 1.dp,
    val borderColor: Color,
    val outerBorderShape: Shape? = null
)

/** Styles for --- horizontal rules. */
@Immutable
data class HorizontalRuleStyle(
    val color: Color,
    val thickness: Dp = 1.dp
)

/** Styles for [links](...). */
@Immutable
data class LinkStyle(
    val textDecoration: TextDecoration? = TextDecoration.Underline,
    val color: Color
)

/**
 * Defines the visual styling for rendering Markdown content using Jetpack Compose.
 * Provides customization options for various Markdown elements like headers, lists, code blocks, etc.
 *
 * Obtain a default theme-based instance using [defaultMarkdownStyleSheet]. Customize it using the `copy()` method.
 */
@Immutable
data class MarkdownStyleSheet(
    val textStyle: TextStyle,
    val boldTextStyle: TextStyle,
    val italicTextStyle: TextStyle,
    val strikethroughTextStyle: TextStyle,
    val headerStyle: HeaderStyle,
    val listStyle: ListStyle,
    val taskListItemStyle: TaskListItemStyle,
    val blockQuoteStyle: BlockQuoteStyle,
    val codeBlockStyle: CodeBlockStyle,
    val inlineCodeStyle: SpanStyle,
    val tableStyle: TableStyle,
    val horizontalRuleStyle: HorizontalRuleStyle,
    val linkStyle: LinkStyle,
    val blockSpacing: Dp = 16.dp,
    val lineBreakSpacing: Dp = 16.dp
)

/**
 * Creates a default [MarkdownStyleSheet] based on the current MaterialTheme values.
 * This function provides sensible defaults derived from the active color scheme and typography.
 * You can override specific defaults by passing arguments or modify the returned sheet using `copy()`.
 *
 * @param textStyle Base text style (defaults to MaterialTheme.typography.bodyMedium).
 * @param boldTextStyle Style for bold text.
 * @param italicTextStyle Style for italic text.
 * @param strikethroughTextStyle Style for strikethrough text.
 * @param linkColor Default color for links.
 * @param codeBlockContainerBackgroundColor Background for code block top/bottom bars.
 * @param codeBlockTextAreaBackgroundColor Background for the main code text area.
 * @param inlineCodeBackgroundColor Background color for `inline code` spans.
 * @param inlineCodeTextColor Text color for `inline code` spans.
 * @param blockQuoteVerticalBarColor Color for the vertical bar on block quotes.
 * @param blockQuoteBackgroundColor Optional background color for block quotes.
 * @param dividerColor Color for horizontal rules (`---`).
 * @param tableBorderColor Color for table cell borders.
 * @param checkedTaskItemTextColor Color for the *text* of checked task list items. Defaults to a neutral outline color.
 * @param checkedCheckboxIndicatorColor Color for the checkmark when checked. Defaults to onPrimary.
 * @param checkedCheckboxContainerColor Color for the checkbox background when checked. Defaults to primary.
 * @param uncheckedCheckboxBorderColor Color for the checkbox border when unchecked. Defaults to outline.
 * @param disabledCheckboxIndicatorColor Color for the checkmark when disabled (as task checkboxes are). Defaults to `checkedTaskItemTextColor`.
 * @param disabledCheckboxContainerColor Color for the checkbox background/border when disabled. Defaults to `checkedTaskItemTextColor`.
 * @return A remembered [MarkdownStyleSheet] instance with default values.
 */
@Composable
fun defaultMarkdownStyleSheet(
    // --- Base & Inline Text Styles ---
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    boldTextStyle: TextStyle = textStyle.copy(fontWeight = FontWeight.Bold),
    italicTextStyle: TextStyle = textStyle.copy(fontStyle = FontStyle.Italic, fontFamily = FontFamily.Cursive),
    strikethroughTextStyle: TextStyle = textStyle.copy(textDecoration = TextDecoration.LineThrough),
    inlineCodeBackgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    inlineCodeTextColor: Color = MaterialTheme.colorScheme.onSurface,
    linkColor: Color = MaterialTheme.colorScheme.primary,
    // --- Block Element Colors ---
    codeBlockContainerBackgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant, // Bars
    codeBlockTextAreaBackgroundColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), // Text area BG
    blockQuoteVerticalBarColor: Color = MaterialTheme.colorScheme.outline,
    blockQuoteBackgroundColor: Color = Color.Transparent,
    dividerColor: Color = MaterialTheme.colorScheme.outline,
    tableBorderColor: Color = MaterialTheme.colorScheme.outline,
    // --- Task Item Styles (Text & Checkbox) ---
    checkedTaskItemTextColor: Color = MaterialTheme.colorScheme.outline,
    checkedCheckboxIndicatorColor: Color = MaterialTheme.colorScheme.onPrimary,
    checkedCheckboxContainerColor: Color = MaterialTheme.colorScheme.primary,
    uncheckedCheckboxBorderColor: Color = MaterialTheme.colorScheme.outline,
    disabledCheckboxIndicatorColor: Color = checkedTaskItemTextColor,
    disabledCheckboxContainerColor: Color = checkedTaskItemTextColor
): MarkdownStyleSheet {
    val resolvedTextColor = textStyle.color.takeOrElse { MaterialTheme.colorScheme.onSurface }
    val baseTextStyle = textStyle.copy(color = resolvedTextColor)

    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val codeTextStyle = baseTextStyle.copy(fontFamily = FontFamily.Monospace)
    val defaultLabelSmallStyle = MaterialTheme.typography.labelSmall
    val languageLabelTextStyleResolved = defaultLabelSmallStyle.copy(color = onSurfaceVariantColor)
    val infoBarTextStyleResolved = defaultLabelSmallStyle.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

    val resolvedInlineCodeStyle = SpanStyle(
        fontFamily = FontFamily.Monospace,
        background = inlineCodeBackgroundColor,
        color = inlineCodeTextColor,
        fontSize = baseTextStyle.fontSize * 0.9
    )

    val defaultCheckedTaskItemTextStyle = SpanStyle(
        textDecoration = TextDecoration.LineThrough,
        color = checkedTaskItemTextColor
    )

    return remember(
        textStyle, boldTextStyle, italicTextStyle, strikethroughTextStyle,
        inlineCodeBackgroundColor, inlineCodeTextColor, linkColor,
        codeBlockContainerBackgroundColor, codeBlockTextAreaBackgroundColor,
        blockQuoteVerticalBarColor, blockQuoteBackgroundColor, dividerColor, tableBorderColor,
        checkedTaskItemTextColor,
        checkedCheckboxIndicatorColor, checkedCheckboxContainerColor,
        uncheckedCheckboxBorderColor, disabledCheckboxIndicatorColor, disabledCheckboxContainerColor,
        baseTextStyle, onSurfaceVariantColor, codeTextStyle, defaultLabelSmallStyle,
        languageLabelTextStyleResolved, infoBarTextStyleResolved,
        resolvedInlineCodeStyle, defaultCheckedTaskItemTextStyle
    ) {
        MarkdownStyleSheet(
            textStyle = baseTextStyle,
            boldTextStyle = boldTextStyle.merge(TextStyle(color = baseTextStyle.color)),
            italicTextStyle = italicTextStyle.merge(TextStyle(color = baseTextStyle.color)),
            strikethroughTextStyle = strikethroughTextStyle.merge(TextStyle(color = baseTextStyle.color)),
            inlineCodeStyle = resolvedInlineCodeStyle,
            linkStyle = LinkStyle(
                textDecoration = TextDecoration.Underline,
                color = linkColor
            ),
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
            taskListItemStyle = TaskListItemStyle(
                checkedTextStyle = defaultCheckedTaskItemTextStyle,
                uncheckedTextStyle = null,
                checkedCheckboxIndicatorColor = checkedCheckboxIndicatorColor,
                checkedCheckboxContainerColor = checkedCheckboxContainerColor,
                uncheckedCheckboxBorderColor = uncheckedCheckboxBorderColor,
                disabledCheckboxIndicatorColor = disabledCheckboxIndicatorColor,
                disabledCheckboxContainerColor = disabledCheckboxContainerColor
            ),
            blockQuoteStyle = BlockQuoteStyle(
                textStyle = baseTextStyle.copy(fontStyle = FontStyle.Italic),
                verticalBarColor = blockQuoteVerticalBarColor,
                verticalBarWidth = 4.dp,
                padding = 8.dp,
                backgroundColor = blockQuoteBackgroundColor
            ),
            codeBlockStyle = CodeBlockStyle(
                textStyle = codeTextStyle,
                modifier = Modifier,
                contentPadding = PaddingValues(8.dp),
                codeBackground = codeBlockTextAreaBackgroundColor,
                showLanguageLabel = true,
                languageLabelTextStyle = languageLabelTextStyleResolved,
                languageLabelBackground = codeBlockContainerBackgroundColor,
                languageLabelPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                showInfoBar = true,
                infoBarTextStyle = infoBarTextStyleResolved,
                infoBarBackground = codeBlockContainerBackgroundColor,
                infoBarPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                showCopyButton = true,
                copyIconTint = onSurfaceVariantColor,
                showLineCount = true,
                showCharCount = true
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
            blockSpacing = 16.dp,
            lineBreakSpacing = 8.dp
        )
    }
}