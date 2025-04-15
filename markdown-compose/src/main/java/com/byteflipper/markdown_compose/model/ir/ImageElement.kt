package com.byteflipper.markdown_compose.model.ir

/**
 * Represents an image element (e.g., ![alt text](url)) in the Markdown IR.
 *
 * @property url The source URL of the image.
 * @property altText The alternative text description for the image.
 */
data class ImageElement(
    val url: String,
    val altText: String
) : MarkdownElement
