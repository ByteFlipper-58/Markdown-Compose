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
import com.byteflipper.markdown_compose.model.MarkdownStyleSheet
import com.byteflipper.markdown_compose.model.TaskListItemNode
import com.byteflipper.markdown_compose.parser.BlockParser
import com.byteflipper.markdown_compose.renderer.MarkdownRenderer
import android.util.Log

private const val TAG = "TaskListItemComposable"

/**
 * Renders a TaskListItemNode using a Checkbox and ClickableText composable.
 *
 * @param node The TaskListItemNode containing the checked state and content.
 * @param styleSheet The stylesheet defining visual appearance, including checkbox colors.
 * @param modifier Modifier for layout adjustments of the Row.
 * @param linkHandler The callback to handle link clicks within the task item text.
 */
@Composable
fun TaskListItem (
    node: TaskListItemNode,
    styleSheet: MarkdownStyleSheet,
    modifier: Modifier = Modifier,
    linkHandler: (url: String) -> Unit
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
        val disabledUncheckedBorderColor = taskStyle.disabledCheckboxContainerColor
            ?: (taskStyle.uncheckedCheckboxBorderColor ?: defaultOutline).copy(alpha = 0.38f)

        Checkbox(
            checked = node.isChecked,
            onCheckedChange = null,
            enabled = false,
            modifier = Modifier.size(24.dp),
            colors = CheckboxDefaults.colors(
                disabledCheckedColor = disabledCheckedContainerColor,
                disabledUncheckedColor = disabledUncheckedBorderColor,
                disabledIndeterminateColor = disabledCheckedContainerColor
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

        val contentString = MarkdownRenderer.render(node.content, styleSheet)

        ClickableText(
            text = contentString,
            style = LocalTextStyle.current.merge(contentTextStyle),
            onClick = { offset ->
                contentString
                    .getStringAnnotations(Link.URL_TAG, offset, offset)
                    .firstOrNull()?.let { annotation ->
                        Log.i(TAG, "Link clicked in TaskListItem: ${annotation.item}")
                        linkHandler(annotation.item)
                    }
            }
        )
    }
}