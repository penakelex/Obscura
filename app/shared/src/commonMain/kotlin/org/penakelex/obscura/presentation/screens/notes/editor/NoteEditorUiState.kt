package org.penakelex.obscura.presentation.screens.notes.editor

import org.penakelex.obscura.domain.model.common.CipherType
import org.penakelex.obscura.presentation.util.error.UiError

data class NoteEditorUiState(
    val noteId: String? = null,
    val content: String = "",
    val cipherType: CipherType = CipherType.DEFAULT,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val contentError: UiError? = null,
    val isDecryptionFailed: Boolean = false,
) {
    val isEditMode: Boolean get() = noteId != null
    val isSaveEnabled: Boolean
        get() = !isLoading &&
                !isSaving &&
                content.isNotBlank() &&
                contentError == null &&
                !isDecryptionFailed
    val canNavigateBack: Boolean
        get() = !isLoading && !isSaving && !hasUnsavedChanges
}