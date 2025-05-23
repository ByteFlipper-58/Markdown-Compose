package com.byteflipper.markdown_compose.model

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift // Для суперскрипта
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.*

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
    val thickness: Dp = 1.dp,
    /** Style of the rule: "solid", "dashed", "dotted". */
    val style: String = "solid"
)

/** Styles for [links](...). */
@Immutable
data class LinkStyle(
    val textDecoration: TextDecoration? = TextDecoration.Underline,
    val color: Color
)

/** Styles for ![images](...). */
@Immutable
data class ImageStyle(
    val modifier: Modifier = Modifier,
    val shape: Shape = RectangleShape,
    val contentScale: ContentScale = ContentScale.Fit,
    val placeholder: Painter? = null,
    val error: Painter? = null
)

/** Styles for definition lists (term/details). */
@Immutable
data class DefinitionListStyle(
    val termTextStyle: TextStyle,
    val detailsTextStyle: TextStyle,
    val detailsIndent: Dp = 16.dp, // Indentation for the details part
    val itemSpacing: Dp = 8.dp // Spacing between definition items
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
    val imageStyle: ImageStyle,
    // --- Новые стили для сносок ---
    /** Style for the inline footnote reference (e.g., `[1]`). Often uses superscript. */
    val footnoteReferenceStyle: SpanStyle,
    /** Style for the entire footnote definition block at the bottom. */
    val footnoteDefinitionStyle: TextStyle,
    /** Padding above the footnote definitions block. */
    val footnoteBlockPadding: Dp = 16.dp,
    // --- Конец новых стилей ---
    val definitionListStyle: DefinitionListStyle, // Add style for definition lists
    val paragraphPadding: PaddingValues, // Add padding for paragraphs
    val blockSpacing: Dp = 16.dp, // General spacing (might be less used now)
    val lineBreakSpacing: Dp = 8.dp // Spacing for explicit <br> or LineBreakElement
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
 * @param imageStyle Default styles for images.
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
    disabledCheckboxContainerColor: Color = checkedTaskItemTextColor,
    imageStyle: ImageStyle = ImageStyle(),
    // Add default values for definition list styles
    definitionTermFontWeight: FontWeight = FontWeight.Bold,
    definitionDetailsIndent: Dp = 16.dp,
    definitionItemSpacing: Dp = 8.dp,
    // Add default parameter for paragraph padding
    paragraphPadding: PaddingValues = PaddingValues(bottom = 8.dp) // Default bottom padding for paragraphs
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

    // --- Стиль сноски по умолчанию ---
    val defaultFootnoteReferenceStyle = SpanStyle(
        color = MaterialTheme.colorScheme.primary, // Или baseTextStyle.color
        fontSize = baseTextStyle.fontSize * 0.8, // Меньше
        baselineShift = BaselineShift.Superscript // Поднять
    )
    val defaultFootnoteDefinitionStyle = MaterialTheme.typography.bodySmall // Или baseTextStyle, но меньше

    // Default styles for definition list
    val defaultDefinitionTermStyle = baseTextStyle.copy(fontWeight = definitionTermFontWeight)
    val defaultDefinitionDetailsStyle = baseTextStyle // Inherit base style for details

    return remember(
        textStyle, boldTextStyle, italicTextStyle, strikethroughTextStyle,
        inlineCodeBackgroundColor, inlineCodeTextColor, linkColor,
        codeBlockContainerBackgroundColor, codeBlockTextAreaBackgroundColor,
        blockQuoteVerticalBarColor, blockQuoteBackgroundColor, dividerColor, tableBorderColor,
        checkedTaskItemTextColor,
        checkedCheckboxIndicatorColor, checkedCheckboxContainerColor,
        uncheckedCheckboxBorderColor, disabledCheckboxIndicatorColor, disabledCheckboxContainerColor,
        imageStyle,
        defaultFootnoteReferenceStyle,
        defaultFootnoteDefinitionStyle,
        // Add definition list styles to remember keys
        defaultDefinitionTermStyle, defaultDefinitionDetailsStyle, definitionDetailsIndent, definitionItemSpacing,
        // Add paragraph padding to remember keys
        paragraphPadding,
        // Add horizontal rule style to remember keys
        dividerColor, // Already here for color/thickness, style is implicitly remembered via HorizontalRuleStyle instance
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
            imageStyle = imageStyle,
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
                thickness = 1.dp,
                style = "solid" // Provide default style
            ),
            // --- Присваивание стилей сносок ---
            footnoteReferenceStyle = defaultFootnoteReferenceStyle,
            footnoteDefinitionStyle = defaultFootnoteDefinitionStyle.copy(
                color = baseTextStyle.color.copy(alpha = 0.8f) // Немного приглушить цвет определения
            ),
            footnoteBlockPadding = 16.dp,
            // --- Assign definition list style ---
            definitionListStyle = DefinitionListStyle(
                termTextStyle = defaultDefinitionTermStyle,
                detailsTextStyle = defaultDefinitionDetailsStyle,
                detailsIndent = definitionDetailsIndent,
                itemSpacing = definitionItemSpacing
            ),
            // --- End assignment ---
            paragraphPadding = paragraphPadding, // Assign paragraph padding
            blockSpacing = 16.dp, // Keep blockSpacing for now, might be deprecated later
            lineBreakSpacing = 8.dp
        )
    }
}
