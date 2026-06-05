package org.penakelex.obscura.presentation.util.content

object NoteContentParser {
    private val HEADING_REGEX = Regex("^#{1,6}\\s+(.+)$")
    private val MARKDOWN_SYNTAX_REGEX = Regex(
        """(?x)
        \*\*([^*]+)\*\*       |  # bold
        \*([^*]+)\*           |  # italic
        __([^_]+)__           |  # bold (alt)
        _([^_]+)_             |  # italic (alt)
        ~~([^~]+)~~           |  # strikethrough
        `([^`]+)`             |  # inline code
        \[([^]]+)]\([^)]+\)  |  # links
        !\[[^]]*]\([^)]+\)   |  # images
        ^>\s?                    # blockquote marker
        """.trimIndent(),
        RegexOption.MULTILINE
    )
    private val MULTIPLE_WHITESPACE = Regex("\\s+")

    fun extractTitle(content: String): String? {
        if (content.isBlank()) {
            return null
        }

        val firstNonEmpty = content.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.isNotEmpty() }
            ?: return null

        HEADING_REGEX.matchEntire(firstNonEmpty)?.let { match ->
            return stripMarkdown(match.groupValues[1].trim())
                .takeIf { it.isNotBlank() }
        }

        return stripMarkdown(firstNonEmpty)
            .takeIf { it.isNotBlank() }
            ?.take(120)
    }

    fun extractPreview(
        content: String,
        maxLength: Int = 200,
        titleToExclude: String? = null
    ): String {
        if (content.isBlank()) return ""
        val lines = content.lines()
        val previewLines = lines
            .dropWhile { line ->
                val trimmed = line.trim()
                trimmed.isEmpty() ||
                        (titleToExclude != null &&
                                stripMarkdown(trimmed) == titleToExclude) ||
                        HEADING_REGEX.containsMatchIn(trimmed)
            }
            .take(5)
            .map { stripMarkdown(it.trim()) }
            .filter { it.isNotBlank() }
        val preview = previewLines.joinToString(" ")

        if (preview.length <= maxLength) {
            return preview
        }

        return preview.take(maxLength).trimEnd() + "…"
    }

    private fun stripMarkdown(text: String): String =
        MARKDOWN_SYNTAX_REGEX
            .replace(text) { match ->
                match.groupValues
                    .drop(1)
                    .firstOrNull { it.isNotEmpty() }
                    .orEmpty()
            }
            .replace(MULTIPLE_WHITESPACE, " ")
            .trim()
}