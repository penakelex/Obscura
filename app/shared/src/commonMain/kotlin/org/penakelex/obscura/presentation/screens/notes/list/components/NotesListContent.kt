package org.penakelex.obscura.presentation.screens.notes.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import obscura.app.shared.generated.resources.Res
import obscura.app.shared.generated.resources.create_first_note
import obscura.app.shared.generated.resources.no_notes
import obscura.app.shared.generated.resources.no_search_results
import obscura.app.shared.generated.resources.search_no_results_message
import org.jetbrains.compose.resources.stringResource
import org.penakelex.obscura.presentation.components.common.EmptyStateView
import org.penakelex.obscura.presentation.screens.notes.list.NotesListUiState
import org.penakelex.obscura.presentation.theme.ObscuraDimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListContent(
    state: NotesListUiState,
    onNoteClick: (String) -> Unit,
    onNoteLongClick: (String) -> Unit,
    onRefresh: () -> Unit,
    onSwipeDelete: (String) -> Unit,
    onToggleNoteSelection: (String) -> Unit,
    onDismissCorruptedBanner: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = ObscuraDimens.Padding.m,
                end = ObscuraDimens.Padding.m,
                top = ObscuraDimens.Padding.s,
                bottom = ObscuraDimens.Padding.xxl,
            ),
            verticalArrangement = Arrangement.spacedBy(
                ObscuraDimens.Padding.s,
            ),
        ) {
            if (state.hasCorruptedNotes) {
                item(key = "corrupted-banner") {
                    CorruptedNotesBanner(
                        count = state.corruptedNoteIds.size,
                        onDismiss = onDismissCorruptedBanner,
                    )
                }
            }

            if (state.filteredNotes.isEmpty()) {
                item(key = "empty-state") {
                    NotesListEmptyState(state = state)
                }
            } else {
                items(
                    items = state.filteredNotes,
                    key = { it.id },
                ) { note ->
                    SwipeToDeleteNoteCard(
                        note = note,
                        isSelected = note.id in state.selectedNoteIds,
                        isSelectionMode = state.isSelectionMode,
                        onClick = { onNoteClick(note.id) },
                        onLongClick = { onNoteLongClick(note.id) },
                        onDelete = { onSwipeDelete(note.id) },
                        onToggleSelection = {
                            onToggleNoteSelection(
                                note.id
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun NotesListEmptyState(
    state: NotesListUiState,
    modifier: Modifier = Modifier,
) {
    when {
        state.searchQuery.isNotBlank() && !state.hasSearchResults ->
            EmptyStateView(
                title = stringResource(Res.string.no_search_results),
                message = stringResource(
                    Res.string.search_no_results_message,
                ),
                icon = Icons.Default.EditNote,
                modifier = modifier.padding(top = 64.dp),
            )

        !state.hasNotes -> EmptyStateView(
            title = stringResource(Res.string.no_notes),
            message = stringResource(Res.string.create_first_note),
            icon = Icons.Default.EditNote,
            modifier = modifier.padding(top = 64.dp),
        )
    }
}