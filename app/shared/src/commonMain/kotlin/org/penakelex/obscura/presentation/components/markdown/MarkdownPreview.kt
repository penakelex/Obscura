package org.penakelex.obscura.presentation.components.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import org.penakelex.obscura.presentation.theme.CodeTypography
import org.penakelex.obscura.presentation.theme.ObscuraDimens

@Composable
fun MarkdownEditor(
    content: String,
    onContentChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var mode by remember { mutableStateOf(MarkdownEditorMode.EDITOR) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ObscuraDimens.Padding.m),
            horizontalArrangement =
                Arrangement.spacedBy(ObscuraDimens.Padding.s),
        ) {
            MarkdownEditorMode.entries.forEach { editorMode ->
                FilterChip(
                    selected = mode == editorMode,
                    onClick = { mode = editorMode },
                    label = { Text(editorMode.toDisplayString()) },
                )
            }
        }
        Spacer(Modifier.height(ObscuraDimens.Padding.s))
        when (mode) {
            MarkdownEditorMode.EDITOR -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = ObscuraDimens.Padding.m)
                        .border(
                            width = ObscuraDimens.Padding.xs / 4,
                            color = MaterialTheme.colorScheme.outline,
                            shape = MaterialTheme.shapes.medium,
                        )
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.medium,
                        )
                        .padding(ObscuraDimens.Padding.m),
                ) {
                    if (content.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = CodeTypography.editor,
                            color = MaterialTheme.colorScheme
                                .onSurfaceVariant
                                .copy(alpha = 0.6f),
                        )
                    }
                    BasicTextField(
                        value = content,
                        onValueChange = onContentChange,
                        modifier = Modifier.fillMaxSize(),
                        enabled = enabled,
                        textStyle = CodeTypography.editor.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        cursorBrush = SolidColor(
                            MaterialTheme.colorScheme.primary,
                        ),
                    )
                }
            }
            MarkdownEditorMode.PREVIEW -> {
                MarkdownRenderer(
                    content = content,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(ObscuraDimens.Padding.m),
                )
            }
            MarkdownEditorMode.SPLIT -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .padding(
                                start = ObscuraDimens.Padding.m,
                                end = ObscuraDimens.Padding.xs,
                            )
                            .border(
                                width = ObscuraDimens.Padding.xs / 4,
                                color = MaterialTheme.colorScheme.outline,
                                shape = MaterialTheme.shapes.medium,
                            )
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.medium,
                            )
                            .padding(ObscuraDimens.Padding.m),
                    ) {
                        if (content.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = CodeTypography.editor,
                                color = MaterialTheme.colorScheme
                                    .onSurfaceVariant
                                    .copy(alpha = 0.6f),
                            )
                        }
                        BasicTextField(
                            value = content,
                            onValueChange = onContentChange,
                            modifier = Modifier.fillMaxSize(),
                            enabled = enabled,
                            textStyle = CodeTypography.editor.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                            cursorBrush = SolidColor(
                                MaterialTheme.colorScheme.primary,
                            ),
                        )
                    }
                    MarkdownRenderer(
                        content = content,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(
                                start = ObscuraDimens.Padding.xs,
                                end = ObscuraDimens.Padding.m,
                            ),
                    )
                }
            }
        }
    }
}