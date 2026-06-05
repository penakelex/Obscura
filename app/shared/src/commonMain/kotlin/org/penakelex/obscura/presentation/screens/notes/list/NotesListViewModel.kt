package org.penakelex.obscura.presentation.screens.notes.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.penakelex.obscura.domain.exception.SyncException
import org.penakelex.obscura.domain.model.note.Note
import org.penakelex.obscura.domain.repository.AuthRepository
import org.penakelex.obscura.domain.usecase.note.DeleteNoteUseCase
import org.penakelex.obscura.domain.usecase.note.ObserveNotesUseCase
import org.penakelex.obscura.domain.usecase.settings.GetSettingsUseCase
import org.penakelex.obscura.domain.usecase.sync.SyncNotesRestUseCase
import org.penakelex.obscura.domain.usecase.sync.SyncNotesUseCase
import org.penakelex.obscura.presentation.navigation.NavRoute
import org.penakelex.obscura.presentation.navigation.Navigator
import org.penakelex.obscura.presentation.util.event.SnackbarAction
import org.penakelex.obscura.presentation.util.event.UiEvent
import org.penakelex.obscura.presentation.util.message.UiMessage
import org.penakelex.obscura.presentation.util.message.UiMessageMapper

class NotesListViewModel(
    private val observeNotesUseCase: ObserveNotesUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val syncNotesUseCase: SyncNotesUseCase,
    private val syncNotesRestUseCase: SyncNotesRestUseCase,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val authRepository: AuthRepository,
    private val navigator: Navigator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesListUiState())
    val uiState: StateFlow<NotesListUiState> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events: Flow<UiEvent> = _events.receiveAsFlow()

    private var syncJob: Job? = null
    private var undoJob: Job? = null

    init {
        observeNotes()
        observeSettings()
        triggerInitialSync()
    }

    private fun observeNotes() {
        observeNotesUseCase()
            .onEach { result ->
                _uiState.update { state ->
                    val newCorruptedIds =
                        result.corruptedNoteIds.toSet()
                    val showBanner =
                        if (newCorruptedIds != state.corruptedNoteIds) {
                            true
                        } else {
                            state.isCorruptedBannerVisible
                        }

                    state.copy(
                        notes = result.notes
                            .sortedByDescending(Note::updatedAt),
                        corruptedNoteIds = newCorruptedIds,
                        isCorruptedBannerVisible = showBanner,
                        isLoading = false,
                    ).clearInvalidSelections()
                }
                if (result.corruptedNoteIds.isNotEmpty()) {
                    _events.send(
                        UiEvent.ShowSnackbar(
                            message = UiMessage.Warning.CorruptedNotesSkipped(
                                count = result.corruptedNoteIds.size,
                            ),
                        )
                    )
                }
            }
            .catch { e ->
                _uiState.update { it.copy(isLoading = false) }
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessageMapper.map(e),
                    )
                )
            }
            .launchIn(viewModelScope)
    }

    private fun observeSettings() {
        getSettingsUseCase.observe()
            .onEach { settings ->
                _uiState.update {
                    it.copy(
                        isAutoSyncEnabled = settings.isAutoSyncEnabled,
                        lastSyncTimestamp = settings.lastSyncTimestamp,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun triggerInitialSync() {
        viewModelScope.launch {
            val settings = getSettingsUseCase.get()
            if (!settings.isAutoSyncEnabled) return@launch
            if (!authRepository.isLoggedIn()) return@launch

            try {
                syncNotesUseCase()
            } catch (_: Exception) {
            }
        }
    }

    fun onRefresh() {
        viewModelScope.launch {
            if (!authRepository.isLoggedIn()) {
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessage.Warning.SyncRequiresAuth,
                    )
                )
                return@launch
            }

            _uiState.update { it.copy(isRefreshing = true) }
            try {
                performManualSync()
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun onManualSync() {
        viewModelScope.launch {
            if (!authRepository.isLoggedIn()) {
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessage.Warning.SyncRequiresAuth,
                    )
                )
                return@launch
            }
            performManualSync()
        }
    }

    private fun performManualSync() {
        syncJob?.cancel()
        syncJob = viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            try {
                val success = syncNotesRestUseCase()
                if (success) {
                    _events.send(
                        UiEvent.ShowSnackbar(
                            message = UiMessage.Success.SyncSuccessful,
                        )
                    )
                }
            } catch (_: SyncException.Unauthenticated) {
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessage.Error.SyncUnauthenticated,
                    )
                )
            } catch (e: SyncException) {
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessageMapper.map(e),
                    )
                )
            } finally {
                _uiState.update { it.copy(isSyncing = false) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onToggleSearch() {
        _uiState.update { state ->
            if (state.isSearchActive) {
                state.copy(
                    isSearchActive = false,
                    searchQuery = "",
                )
            } else {
                state.copy(isSearchActive = true)
            }
        }
    }

    fun onNoteClick(noteId: String) {
        val state = _uiState.value
        if (state.isSelectionMode) {
            toggleNoteSelection(noteId)
        } else {
            navigator.navigate(NavRoute.Main.NoteEditor(noteId = noteId))
        }
    }

    fun onCreateNoteClick() {
        navigator.navigate(NavRoute.Main.NoteEditor(noteId = null))
    }

    fun onNoteLongClick(noteId: String) {
        if (_uiState.value.isSelectionMode) return
        _uiState.update {
            it.copy(
                isSelectionMode = true,
                selectedNoteIds = setOf(noteId),
            )
        }
    }

    fun onToggleNoteSelection(noteId: String) {
        toggleNoteSelection(noteId)
    }

    private fun toggleNoteSelection(noteId: String) {
        _uiState.update { state ->
            val newSelection =
                state.selectedNoteIds.toMutableSet().apply {
                    if (contains(noteId)) remove(noteId) else add(
                        noteId
                    )
                }
            state.copy(
                selectedNoteIds = newSelection,
                isSelectionMode = newSelection.isNotEmpty(),
            )
        }
    }

    fun onSelectAll() {
        _uiState.update { state ->
            state.copy(
                selectedNoteIds = state.filteredNotes.map { it.id }
                    .toSet(),
            )
        }
    }

    fun onDeselectAll() {
        _uiState.update {
            it.copy(
                selectedNoteIds = emptySet(),
                isSelectionMode = false,
            )
        }
    }

    fun onCancelSelectionMode() = onDeselectAll()

    fun onDeleteSelectedConfirmed() {
        val ids = _uiState.value.selectedNoteIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            deleteNotes(ids, showUndo = false)
        }
    }

    fun onDeleteNoteSwipe(noteId: String) {
        viewModelScope.launch {
            deleteNotes(listOf(noteId), showUndo = true)
        }
    }

    private suspend fun deleteNotes(
        ids: List<String>,
        showUndo: Boolean,
    ) {
        try {
            ids.forEach { deleteNoteUseCase(it) }
            _uiState.update {
                it.copy(
                    selectedNoteIds = it.selectedNoteIds - ids.toSet(),
                    isSelectionMode = (it.selectedNoteIds - ids.toSet()).isNotEmpty(),
                )
            }
            _events.send(
                UiEvent.ShowSnackbar(
                    message = UiMessage.Success.NotesDeleted(ids.size),
                    action = if (showUndo && ids.size == 1) {
                        SnackbarAction.Undo
                    } else {
                        null
                    },
                )
            )
            triggerSync()
        } catch (e: Exception) {
            _events.send(
                UiEvent.ShowSnackbar(
                    message = UiMessageMapper.map(e),
                )
            )
        }
    }

    private fun triggerSync() {
        viewModelScope.launch {
            if (!authRepository.isLoggedIn()) return@launch
            try {
                syncNotesUseCase()
            } catch (_: Exception) {
            }
        }
    }

    fun onSettingsClick() {
        navigator.navigate(NavRoute.Main.Settings)
    }

    fun onAccountClick() {
        if (!authRepository.isLoggedIn()) {
            navigator.navigate(NavRoute.Auth.Login)
            return
        }
        navigator.navigate(NavRoute.Main.Account)
    }

    fun onSessionsClick() {
        if (!authRepository.isLoggedIn()) {
            navigator.navigate(NavRoute.Auth.Login)
            return
        }
        navigator.navigate(NavRoute.Main.Sessions)
    }

    fun onDismissCorruptedBanner() {
        _uiState.update { it.copy(isCorruptedBannerVisible = false) }
    }

    private fun NotesListUiState.clearInvalidSelections(): NotesListUiState {
        if (selectedNoteIds.isEmpty()) return this
        val validIds = notes.map { it.id }.toSet()
        val cleaned = selectedNoteIds.intersect(validIds)
        return copy(
            selectedNoteIds = cleaned,
            isSelectionMode = cleaned.isNotEmpty(),
        )
    }

    override fun onCleared() {
        syncJob?.cancel()
        undoJob?.cancel()
    }
}