package org.penakelex.obscura.presentation.screens.notes.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.penakelex.obscura.domain.exception.ObscuraDomainException
import org.penakelex.obscura.domain.exception.ValidationException
import org.penakelex.obscura.domain.model.common.CipherType
import org.penakelex.obscura.domain.usecase.note.CreateNoteUseCase
import org.penakelex.obscura.domain.usecase.note.GetNoteUseCase
import org.penakelex.obscura.domain.usecase.note.UpdateNoteUseCase
import org.penakelex.obscura.domain.usecase.settings.GetSettingsUseCase
import org.penakelex.obscura.domain.usecase.sync.SyncNotesUseCase
import org.penakelex.obscura.presentation.navigation.Navigator
import org.penakelex.obscura.presentation.util.error.UiErrorMapper
import org.penakelex.obscura.presentation.util.event.UiEvent
import org.penakelex.obscura.presentation.util.message.UiMessage
import org.penakelex.obscura.presentation.util.message.UiMessageMapper
import kotlin.time.Duration.Companion.milliseconds

class NoteEditorViewModel(
    private val noteId: String?,
    private val createNoteUseCase: CreateNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val getNoteUseCase: GetNoteUseCase,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val syncNotesUseCase: SyncNotesUseCase,
    private val navigator: Navigator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteEditorUiState(noteId = noteId))
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events: Flow<UiEvent> = _events.receiveAsFlow()

    private var autoSaveJob: Job? = null
    private var initialContent: String = ""

    init {
        loadNoteOrDefaultCipher()
    }

    private fun loadNoteOrDefaultCipher() {
        viewModelScope.launch {
            if (noteId != null) {
                loadNote(noteId)
            } else {
                val settings = getSettingsUseCase.get()
                _uiState.update {
                    it.copy(
                        cipherType = settings.defaultCipherType,
                        isLoading = false,
                    )
                }
            }
        }
    }

    private suspend fun loadNote(id: String) {
        try {
            val note = getNoteUseCase(id)
            initialContent = note.content
            _uiState.update {
                it.copy(
                    content = note.content,
                    cipherType = note.cipherType,
                    isLoading = false,
                    hasUnsavedChanges = false,
                    isDecryptionFailed = false,
                )
            }
        } catch (_: ObscuraDomainException.DecryptionException) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isDecryptionFailed = true,
                )
            }
            _events.send(
                UiEvent.ShowSnackbar(
                    message = UiMessage.Error.DecryptionFailed,
                )
            )
        } catch (_: ObscuraDomainException.NoteNotFoundException) {
            _events.send(
                UiEvent.ShowSnackbar(
                    message = UiMessage.Error.NoteNotFound(id),
                )
            )
            navigator.navigateBack()
        } catch (e: Exception) {
            _events.send(
                UiEvent.ShowSnackbar(
                    message = UiMessage.Error.Unknown(e.message),
                )
            )
        }
    }

    fun onContentChange(newContent: String) {
        _uiState.update {
            it.copy(
                content = newContent,
                hasUnsavedChanges = newContent != initialContent,
                contentError = null,
            )
        }
        scheduleAutoSave()
    }

    fun onCipherTypeChange(cipherType: CipherType) {
        _uiState.update {
            it.copy(
                cipherType = cipherType,
                hasUnsavedChanges = true,
            )
        }
        scheduleAutoSave()
    }

    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(AUTO_SAVE_DELAY_MILLIS.milliseconds)
            val state = _uiState.value
            if (state.hasUnsavedChanges && state.content.isNotBlank()) {
                save()
            }
        }
    }

    fun save() {
        val state = _uiState.value
        if (!state.isSaveEnabled) return

        autoSaveJob?.cancel()
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val savedId = if (state.noteId != null) {
                    updateNoteUseCase(
                        id = state.noteId,
                        content = state.content,
                        cipherType = state.cipherType,
                    )
                    state.noteId
                } else {
                    createNoteUseCase(
                        content = state.content,
                        cipherType = state.cipherType,
                    )
                }
                initialContent = state.content
                _uiState.update {
                    it.copy(
                        noteId = savedId,
                        isSaving = false,
                        hasUnsavedChanges = false,
                    )
                }
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessage.Success.NoteSaved,
                    )
                )
                triggerSync()
            } catch (e: ValidationException) {
                val contentError = UiErrorMapper.mapForField(e, "content")
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        contentError = contentError,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessageMapper.map(e),
                    )
                )
            }
        }
    }

    private fun triggerSync() {
        viewModelScope.launch {
            try {
                syncNotesUseCase()
            } catch (_: Exception) {
            }
        }
    }

    fun onBackClick() {
        if (_uiState.value.hasUnsavedChanges) {
            viewModelScope.launch {
                _events.send(UiEvent.NavigateBack)
            }
        } else {
            navigator.navigateBack()
        }
    }

    fun discardChangesAndNavigateBack() {
        navigator.navigateBack()
    }

    override fun onCleared() {
        autoSaveJob?.cancel()
    }

    private companion object {
        const val AUTO_SAVE_DELAY_MILLIS = 2000L
    }
}