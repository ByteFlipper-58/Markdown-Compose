package com.byteflipper.markdown_compose.renderer.builders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.byteflipper.markdown_compose.model.* // Import all models for Link, FootnoteInfo etc.
import com.byteflipper.markdown_compose.parser.BlockParser
import com.byteflipper.markdown_compose.renderer.FOOTNOTE_REF_TAG // Import tag
import com.byteflipper.markdown_compose.renderer.MarkdownRenderer
import android.util.Log

private const val TAG = "TaskListItemComposable"

/**
 * Renders a TaskListItemNode using a Checkbox and ClickableText composable.
 *
 * @param node The TaskListItemNode containing the checked state and content.
 * @param styleSheet The stylesheet defining visual appearance, including checkbox colors.
 * @param modifier Modifier for layout adjustments of the Row.
 * @param footnoteReferenceMap Map for resolving footnote display indices.
 * @param linkHandler The callback to handle link clicks within the task item text.
 * @param onFootnoteReferenceClick Callback for footnote reference clicks.
 * @param onCheckedChange Callback invoked when the user clicks the checkbox, providing the node and the *intended* new checked state.
 */
@Composable
fun TaskListItem (
    node: TaskListItemNode,
    styleSheet: MarkdownStyleSheet,
    modifier: Modifier = Modifier,
    footnoteReferenceMap: Map<String, Int>?, // Added
    linkHandler: (url: String) -> Unit,
    onFootnoteReferenceClick: ((identifier: String) -> Unit)?, // Added
    onCheckedChange: (node: TaskListItemNode, isChecked: Boolean) -> Unit // Added
) {
    val logicalIndentLevel = node.indentLevel / BlockParser.INPUT_SPACES_PER_LEVEL
    val indentPadding = styleSheet.listStyle.indentPadding * logicalIndentLevel
    Log.d(TAG, "Rendering TaskListItem: padding=${indentPadding.value}dp, checked=${node.isChecked}")

    Row(
        modifier = modifier.padding(start = indentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val taskStyle = styleSheet.taskListItemStyle
        val defaultOutline = MaterialTheme.colorScheme.outline

        // --- Checkbox styling (remains the same) ---
        val disabledCheckedContainerColor = taskStyle.disabledCheckboxContainerColor
            ?: (taskStyle.checkedCheckboxContainerColor ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.38f)
        // Use enabled colors if checkbox is interactive
        val checkedContainerColor = taskStyle.checkedCheckboxContainerColor ?: MaterialTheme.colorScheme.primary
        val uncheckedBorderColor = taskStyle.uncheckedCheckboxBorderColor ?: defaultOutline
        val checkmarkColor = taskStyle.checkedCheckboxIndicatorColor ?: MaterialTheme.colorScheme.onPrimary

        Checkbox(
            checked = node.isChecked,
            onCheckedChange = { newCheckedState ->
                Log.d(TAG, "Checkbox clicked for task: '${node.content.firstOrNull()?.toString()?.take(20)}...', new state: $newCheckedState")
                onCheckedChange(node, newCheckedState) // Notify caller about the change attempt
            },
            enabled = true, // Make checkbox interactive
            modifier = Modifier.size(24.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = checkedContainerColor,
                uncheckedColor = uncheckedBorderColor, // Border color when unchecked
                checkmarkColor = checkmarkColor
                // Use default disabled colors if needed, but it's enabled now
            )
        )

        val baseTextStyle = styleSheet.textStyle
        val contentTextStyle = if (node.isChecked && taskStyle.checkedTextStyle != null) {
            baseTextStyle.merge(taskStyle.checkedTextStyle)
        } else if (!node.isChecked && taskStyle.uncheckedTextStyle != null) {
            baseTextStyle.merge(taskStyle.uncheckedTextStyle)
        } else {
            baseTextStyle
        }

        // Render content with footnote map
        val contentString = MarkdownRenderer.render(node.content, styleSheet, footnoteReferenceMap)

        ClickableText(
            text = contentString,
            style = LocalTextStyle.current.merge(contentTextStyle),
            onClick = { offset ->
                contentString
                    .getStringAnnotations(Link.URL_TAG, offset, offset)
                    .firstOrNull()?.let { annotation ->
                        Log.i(TAG, "Link clicked in TaskListItem: ${annotation.item}")
                        linkHandler(annotation.item)
                        return@ClickableText // Handled link
                    }
                // Handle footnote clicks
                contentString
                    .getStringAnnotations(FOOTNOTE_REF_TAG, offset, offset)
                    .firstOrNull()?.let { annotation ->
                        Log.i(TAG, "Footnote ref [^${annotation.item}] clicked in TaskListItem")
                        onFootnoteReferenceClick?.invoke(annotation.item)
                    }
            }
        )
    }
}
