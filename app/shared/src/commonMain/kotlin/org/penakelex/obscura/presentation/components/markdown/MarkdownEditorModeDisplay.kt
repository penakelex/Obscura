package org.penakelex.obscura.presentation.components.markdown

import androidx.compose.runtime.Composable
import obscura.app.shared.generated.resources.Res
import obscura.app.shared.generated.resources.editor_mode_editor
import obscura.app.shared.generated.resources.editor_mode_preview
import obscura.app.shared.generated.resources.editor_mode_split
import org.jetbrains.compose.resources.stringResource

@Composable
fun MarkdownEditorMode.toDisplayString(): String = when (this) {
    MarkdownEditorMode.EDITOR ->
        stringResource(Res.string.editor_mode_editor)
    MarkdownEditorMode.PREVIEW ->
        stringResource(Res.string.editor_mode_preview)
    MarkdownEditorMode.SPLIT ->
        stringResource(Res.string.editor_mode_split)
}