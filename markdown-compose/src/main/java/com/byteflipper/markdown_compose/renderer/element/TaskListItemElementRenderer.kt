package com.byteflipper.markdown_compose.renderer.element

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.byteflipper.markdown_compose.model.ir.TaskListItemElement
import com.byteflipper.markdown_compose.renderer.ComposeMarkdownRenderer

/**
 * Renders a task list item element with a checkbox.
 */
internal object TaskListItemElementRenderer {
    // Removed @Composable annotation
    fun render(
        renderer: ComposeMarkdownRenderer,
        element: TaskListItemElement
        // TODO: Pass nesting level for indentation
    ): @Composable () -> Unit = {
        val taskStyle = renderer.styleSheet.taskListItemStyle
        val baseTextStyle = renderer.styleSheet.textStyle
        val contentTextStyle = if (element.isChecked && taskStyle.checkedTextStyle != null) {
            baseTextStyle.merge(taskStyle.checkedTextStyle)
        } else if (!element.isChecked && taskStyle.uncheckedTextStyle != null) {
            baseTextStyle.merge(taskStyle.uncheckedTextStyle)
        } else {
            baseTextStyle
        }

        Row(
            modifier = Modifier.fillMaxWidth(), // TODO: Add indentation based on level
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val defaultOutline = MaterialTheme.colorScheme.outline
            val checkedContainerColor = taskStyle.checkedCheckboxContainerColor ?: MaterialTheme.colorScheme.primary
            val uncheckedBorderColor = taskStyle.uncheckedCheckboxBorderColor ?: defaultOutline
            val checkmarkColor = taskStyle.checkedCheckboxIndicatorColor ?: MaterialTheme.colorScheme.onPrimary

            Checkbox(
                checked = element.isChecked,
                onCheckedChange = { newCheckedState ->
                    Log.d("TaskListItemRenderer", "Checkbox clicked, new state: $newCheckedState")
                    renderer.onTaskCheckedChange?.invoke(element, newCheckedState)
                },
                enabled = true, // Assuming always enabled
                modifier = Modifier.size(24.dp),
                colors = CheckboxDefaults.colors(
                    checkedColor = checkedContainerColor,
                    uncheckedColor = uncheckedBorderColor,
                    checkmarkColor = checkmarkColor
                )
            )

            CompositionLocalProvider(LocalTextStyle provides contentTextStyle) {
                Box(modifier = Modifier.weight(1f)) { // Allow content to take remaining space
                    renderer.renderChildren(element.children)
                }
            }
        }
    }
}
