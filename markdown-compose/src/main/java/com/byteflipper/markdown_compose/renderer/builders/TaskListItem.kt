package com.byteflipper.markdown_compose.renderer.builders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.byteflipper.markdown_compose.model.MarkdownStyleSheet
import com.byteflipper.markdown_compose.model.TaskListItemNode
import com.byteflipper.markdown_compose.parser.BlockParser
import com.byteflipper.markdown_compose.renderer.MarkdownRenderer
import android.util.Log
import androidx.compose.material3.MaterialTheme

private const val TAG = "TaskListItemComposable"

/**
 * Renders a TaskListItemNode using a Checkbox and Text composable.
 *
 * @param node The TaskListItemNode containing the checked state and content.
 * @param styleSheet The stylesheet defining visual appearance, including checkbox colors.
 * @param modifier Modifier for layout adjustments of the Row.
 */
@Composable
fun TaskListItem (
    node: TaskListItemNode,
    styleSheet: MarkdownStyleSheet,
    modifier: Modifier = Modifier
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

        // Determine the final colors to be used for the *disabled* state
        // Use style color if defined, fallback to default outline/primary, then apply dimming (alpha)
        val disabledCheckedContainerColor = taskStyle.disabledCheckboxContainerColor
            ?: (taskStyle.checkedCheckboxContainerColor ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.38f)
        val disabledUncheckedBorderColor = taskStyle.disabledCheckboxContainerColor // Often same color for container/border in disabled state
            ?: (taskStyle.uncheckedCheckboxBorderColor ?: defaultOutline).copy(alpha = 0.38f)
        // NOTE: The disabled checkmark indicator color cannot be directly set in M3 1.3.1 via `colors`.
        // It will likely be derived internally.

        Checkbox(
            checked = node.isChecked,
            onCheckedChange = null,
            enabled = false,
            modifier = Modifier.size(24.dp),
            colors = CheckboxDefaults.colors(
                // --- Configure the available DISABLED state colors ---
                disabledCheckedColor = disabledCheckedContainerColor,
                disabledUncheckedColor = disabledUncheckedBorderColor,
                disabledIndeterminateColor = disabledCheckedContainerColor
                // The parameter `disabledCheckmarkColor` is NOT available here in M3 v1.3.1
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

        Text(
            text = MarkdownRenderer.render(node.content, styleSheet),
            style = LocalTextStyle.current.merge(contentTextStyle)
        )
    }
}