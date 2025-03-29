package com.byteflipper.markdown_compose.parser

import android.util.Log
import com.byteflipper.markdown_compose.model.ListItemNode
import com.byteflipper.markdown_compose.model.MarkdownNode
import com.byteflipper.markdown_compose.model.TaskListItemNode

private const val TAG = "ListParser"

/**
 * Parses individual list item lines (unordered, ordered, task lists).
 */
internal object ListParser {

    // Regex order: Task list must be checked *before* general unordered list.
    private val taskListRegex = Regex("""^(\s*)- \[( |x|X)]\s+(.*)""")
    private val unorderedListRegex = Regex("""^(\s*)(?:[-*+•])\s+(.*)""") // Non-capturing group for bullet
    private val orderedListRegex = Regex("""^(\s*)(\d+)\.\s+(.*)""")

    const val INPUT_SPACES_PER_LEVEL = BlockParser.INPUT_SPACES_PER_LEVEL // Keep consistent

    /**
     * Checks if a line could be any type of list item.
     */
    fun isStartOfListItem(line: String): Boolean {
        return taskListRegex.matches(line) || unorderedListRegex.matches(line) || orderedListRegex.matches(line)
    }

    /**
     * Parses a single line as a list item (task, ordered, or unordered).
     *
     * @param line The line containing the list item.
     * @return The parsed `ListItemNode` or `TaskListItemNode`, or null if parsing fails.
     */
    fun parseListItem(line: String): MarkdownNode? {

        // 1. Check for Task List Item
        taskListRegex.matchEntire(line)?.let { match ->
            val (indentation, checkedChar, contentStr) = match.destructured
            val indentLevel = indentation.length // Use raw space count
            val isChecked = checkedChar.equals("x", ignoreCase = true)
            Log.d(TAG, "Detected Task List Item (checked: $isChecked, indentSpaces: $indentLevel)")
            val inlineNodes = InlineParser.parseInline(contentStr.trim()) // Use InlineParser
            return TaskListItemNode(inlineNodes, indentLevel, isChecked)
        }

        // 2. Check for Unordered List Item (if not a task list)
        unorderedListRegex.matchEntire(line)?.let { match ->
            val (indentation, contentStr) = match.destructured
            val indentLevel = indentation.length // Use raw space count
            Log.d(TAG, "Detected Unordered List Item (indentSpaces: $indentLevel)")
            val inlineNodes = InlineParser.parseInline(contentStr.trim()) // Use InlineParser
            return ListItemNode(inlineNodes, indentLevel, isOrdered = false)
        }

        // 3. Check for Ordered List Item
        orderedListRegex.matchEntire(line)?.let { match ->
            val (indentation, orderStr, contentStr) = match.destructured
            val indentLevel = indentation.length // Use raw space count
            val order = orderStr.toIntOrNull()
            Log.d(TAG, "Detected Ordered List Item (order: $order, indentSpaces: $indentLevel)")
            if (order != null) {
                val inlineNodes = InlineParser.parseInline(contentStr.trim()) // Use InlineParser
                return ListItemNode(inlineNodes, indentLevel, isOrdered = true, order = order)
            } else {
                Log.w(TAG, "Failed to parse order number for potential OL item. Line: $line")
                // Fallthrough to return null
            }
        }

        // If none matched
        Log.w(TAG, "Line did not match any known list item format: \"$line\"")
        return null
    }
}