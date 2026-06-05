package org.penakelex.obscura.presentation.screens.notes.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import obscura.app.shared.generated.resources.Res
import obscura.app.shared.generated.resources.cancel
import obscura.app.shared.generated.resources.discard_changes
import obscura.app.shared.generated.resources.discard_changes_confirm
import obscura.app.shared.generated.resources.error_decryption_failed
import obscura.app.shared.generated.resources.note_content_placeholder
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.penakelex.obscura.domain.model.common.CipherType
import org.penakelex.obscura.presentation.components.common.ErrorScreen
import org.penakelex.obscura.presentation.components.common.FullScreenLoading
import org.penakelex.obscura.presentation.components.common.ObscuraSnackbarHostState
import org.penakelex.obscura.presentation.components.dialogs.ConfirmDialog
import org.penakelex.obscura.presentation.components.markdown.MarkdownEditor
import org.penakelex.obscura.presentation.screens.notes.editor.components.CipherTypeSelector
import org.penakelex.obscura.presentation.screens.notes.editor.components.NoteEditorTopBar
import org.penakelex.obscura.presentation.theme.ObscuraDimens
import org.penakelex.obscura.presentation.util.error.toDisplayString
import org.penakelex.obscura.presentation.util.event.SnackbarAction
import org.penakelex.obscura.presentation.util.event.UiEvent
import org.penakelex.obscura.presentation.util.event.toDisplayLabel

@Composable
fun NoteEditorScreen(
    noteId: String?,
    snackbarHostState: ObscuraSnackbarHostState,
    viewModel: NoteEditorViewModel = koinViewModel {
        parametersOf(noteId)
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val showDiscardDialog = remember { mutableStateOf(false) }
    val discardTitle = stringResource(Res.string.discard_changes)
    val discardMessage =
        stringResource(Res.string.discard_changes_confirm)
    val discardText = stringResource(Res.string.discard_changes)
    val cancelText = stringResource(Res.string.cancel)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    val actionLabel = event.action?.toDisplayLabel()
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = actionLabel,
                    )

                    if (result == SnackbarResult.ActionPerformed) {
                        when (event.action) {
                            SnackbarAction.Retry -> viewModel.save()
                            SnackbarAction.Undo,
                            SnackbarAction.OpenDetails,
                            null -> {
                            }
                        }
                    }
                }

                is UiEvent.NavigateBack -> {
                    if (state.hasUnsavedChanges) {
                        showDiscardDialog.value = true
                    } else {
                        viewModel.discardChangesAndNavigateBack()
                    }
                }

                is UiEvent.Navigate -> {
                }
            }
        }
    }

    if (showDiscardDialog.value) {
        ConfirmDialog(
            title = discardTitle,
            message = discardMessage,
            confirmText = discardText,
            dismissText = cancelText,
            onConfirm = {
                showDiscardDialog.value = false
                viewModel.discardChangesAndNavigateBack()
            },
            onDismiss = { showDiscardDialog.value = false },
            isDestructive = true,
        )
    }

    when {
        state.isLoading -> FullScreenLoading()
        state.isDecryptionFailed -> ErrorScreen(
            message = stringResource(Res.string.error_decryption_failed),
            onRetry = null,
        )

        else -> NoteEditorContent(
            state = state,
            onContentChange = viewModel::onContentChange,
            onCipherTypeChange = viewModel::onCipherTypeChange,
            onBackClick = viewModel::onBackClick,
            onSaveClick = viewModel::save,
        )
    }
}

@Composable
private fun NoteEditorContent(
    state: NoteEditorUiState,
    onContentChange: (String) -> Unit,
    onCipherTypeChange: (CipherType) -> Unit,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            NoteEditorTopBar(
                isEditMode = state.isEditMode,
                isSaving = state.isSaving,
                isSaveEnabled = state.isSaveEnabled,
                onBackClick = onBackClick,
                onSaveClick = onSaveClick,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            CipherTypeSelector(
                selectedCipher = state.cipherType,
                onCipherSelected = onCipherTypeChange,
                enabled = !state.isSaving,
                modifier = Modifier
                    .padding(vertical = ObscuraDimens.Padding.s),
            )

            MarkdownEditor(
                content = state.content,
                onContentChange = onContentChange,
                placeholder =
                    stringResource(Res.string.note_content_placeholder),
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxSize(),
            )

            val errorMessage = state.contentError?.toDisplayString()
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(ObscuraDimens.Padding.m),
                )
            }
        }
    }
}