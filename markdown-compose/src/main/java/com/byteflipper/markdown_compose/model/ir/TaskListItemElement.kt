package com.byteflipper.markdown_compose.model.ir

/**
 * Represents a task list item (e.g., - [x] Task) in the Markdown IR.
 * Task lists are typically rendered within regular lists.
 *
 * @property children The elements contained within the task list item.
 * @property isChecked True if the task is marked as completed.
 */
data class TaskListItemElement(
    val children: List<MarkdownElement>,
    val isChecked: Boolean
) : MarkdownElement // Note: This might be nested within a ListItemElement or ListElement depending on parsing logic
