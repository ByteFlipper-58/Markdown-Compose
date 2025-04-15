package com.byteflipper.markdown_compose.model.ir

/**
 * Represents an image that is also a hyperlink (e.g., [![alt text](image-url)](link-url)) in the Markdown IR.
 *
 * @property imageUrl The source URL of the image.
 * @property altText The alternative text description for the image.
 * @property linkUrl The destination URL when the image is clicked.
 */
data class ImageLinkElement(
    val imageUrl: String,
    val altText: String,
    val linkUrl: String
) : MarkdownElement
