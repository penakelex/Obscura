package org.penakelex.obscura.presentation.components.markdown

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.ReferenceLinkHandler
import org.penakelex.obscura.presentation.theme.LocalObscuraColors

@Composable
fun MarkdownRenderer(
    content: String,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val customColors = LocalObscuraColors.current

    val markdownColors = markdownColor(
        text = colors.onSurface,
        codeBackground = customColors.codeBackground,
        inlineCodeBackground =
            customColors.codeBackground.copy(alpha = 0.6f),
        dividerColor = colors.outlineVariant,
        tableBackground = colors.surface,
    )

    val markdownTypography = markdownTypography(
        code = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily.Monospace,
            color = customColors.codeText,
        ),
        inlineCode = MaterialTheme.typography.bodyLarge.copy(
            fontFamily = FontFamily.Monospace,
            color = customColors.codeText,
        ),
        textLink = TextLinkStyles(
            style = SpanStyle(
                color = customColors.linkColor,
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.Underline,
            ),
        ),
    )

    Markdown(
        content = content,
        colors = markdownColors,
        typography = markdownTypography,
        modifier = modifier.fillMaxWidth(),
        referenceLinkHandler = object : ReferenceLinkHandler {
            private val references = mutableMapOf<String, String>()

            override fun find(label: String): String =
                references[label].orEmpty()

            override fun store(label: String, destination: String?) {
                if (destination != null) {
                    references[label] = destination
                }
            }
        },
    )
}