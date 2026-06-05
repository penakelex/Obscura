package org.penakelex.obscura.presentation.screens.notes.list

import org.penakelex.obscura.domain.model.note.Note
import org.penakelex.obscura.presentation.util.content.NoteContentParser

data class NotesListUiState(
    val notes: List<Note> = emptyList(),
    val corruptedNoteIds: Set<String> = emptySet(),
    val isCorruptedBannerVisible: Boolean = true,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isSyncing: Boolean = false,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val selectedNoteIds: Set<String> = emptySet(),
    val isSelectionMode: Boolean = false,
    val isAutoSyncEnabled: Boolean = true,
    val lastSyncTimestamp: Long = 0L,
) {
    val filteredNotes: List<Note> by lazy {
        val source = notes.filter { it.id !in corruptedNoteIds }
        if (searchQuery.isBlank()) {
            source
        } else {
            val query = searchQuery.trim().lowercase()
            source.filter { note ->
                val title = NoteContentParser
                    .extractTitle(note.content)
                    ?.lowercase()
                    .orEmpty()
                val preview = NoteContentParser
                    .extractPreview(note.content)
                    .lowercase()
                title.contains(query) || preview.contains(query)
            }
        }
    }

    val hasNotes: Boolean get() = notes.isNotEmpty()
    val hasSearchResults: Boolean get() = filteredNotes.isNotEmpty()
    val hasCorruptedNotes: Boolean get() =
        corruptedNoteIds.isNotEmpty() && isCorruptedBannerVisible
    val selectedCount: Int get() = selectedNoteIds.size
    val isAnySelected: Boolean get() = selectedNoteIds.isNotEmpty()
    val isAllSelected: Boolean
        get() = filteredNotes.isNotEmpty() &&
                filteredNotes.all { it.id in selectedNoteIds }
}