package org.penakelex.obscura.presentation.screens.notes.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import obscura.app.shared.generated.resources.Res
import obscura.app.shared.generated.resources.cancel
import obscura.app.shared.generated.resources.create_note
import obscura.app.shared.generated.resources.delete
import obscura.app.shared.generated.resources.delete_note
import obscura.app.shared.generated.resources.delete_note_confirm
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.penakelex.obscura.presentation.components.common.FullScreenLoading
import org.penakelex.obscura.presentation.components.common.ObscuraSnackbarHostState
import org.penakelex.obscura.presentation.components.dialogs.ConfirmDialog
import org.penakelex.obscura.presentation.screens.notes.list.components.NotesListContent
import org.penakelex.obscura.presentation.screens.notes.list.components.NotesListTopBar
import org.penakelex.obscura.presentation.util.event.UiEvent

@Composable
fun NotesListScreen(
    snackbarHostState: ObscuraSnackbarHostState,
    viewModel: NotesListViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val deleteConfirmTitle = stringResource(Res.string.delete_note)
    val deleteConfirmMessage = stringResource(
        Res.string.delete_note_confirm,
    )
    val confirmText = stringResource(Res.string.delete)
    val cancelText = stringResource(Res.string.cancel)

    val showDeleteConfirmDialog = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                    )
                }

                is UiEvent.Navigate, UiEvent.NavigateBack -> {
                }
            }
        }
    }

    if (showDeleteConfirmDialog.value) {
        ConfirmDialog(
            title = deleteConfirmTitle,
            message = deleteConfirmMessage,
            confirmText = confirmText,
            dismissText = cancelText,
            onConfirm = {
                viewModel.onDeleteSelectedConfirmed()
                showDeleteConfirmDialog.value = false
            },
            onDismiss = { showDeleteConfirmDialog.value = false },
            isDestructive = true,
        )
    }

    NotesListScreenContent(
        state = state,
        onSearchToggle = viewModel::onToggleSearch,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSyncClick = viewModel::onManualSync,
        onSettingsClick = viewModel::onSettingsClick,
        onAccountClick = viewModel::onAccountClick,
        onSessionsClick = viewModel::onSessionsClick,
        onSelectAll = viewModel::onSelectAll,
        onDeselectAll = viewModel::onDeselectAll,
        onDeleteSelected = { showDeleteConfirmDialog.value = true },
        onCancelSelection = viewModel::onCancelSelectionMode,
        onNoteClick = viewModel::onNoteClick,
        onNoteLongClick = viewModel::onNoteLongClick,
        onRefresh = viewModel::onRefresh,
        onCreateNote = viewModel::onCreateNoteClick,
        onSwipeDelete = viewModel::onDeleteNoteSwipe,
        onToggleNoteSelection = viewModel::onToggleNoteSelection,
        onDismissCorruptedBanner = viewModel::onDismissCorruptedBanner,
    )
}

@Composable
private fun NotesListScreenContent(
    state: NotesListUiState,
    onSearchToggle: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSyncClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAccountClick: () -> Unit,
    onSessionsClick: () -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onDeleteSelected: () -> Unit,
    onCancelSelection: () -> Unit,
    onNoteClick: (String) -> Unit,
    onNoteLongClick: (String) -> Unit,
    onRefresh: () -> Unit,
    onCreateNote: () -> Unit,
    onSwipeDelete: (String) -> Unit,
    onToggleNoteSelection: (String) -> Unit,
    onDismissCorruptedBanner: () -> Unit,
) {
    Scaffold(
        topBar = {
            NotesListTopBar(
                state = state,
                onSearchToggle = onSearchToggle,
                onSearchQueryChange = onSearchQueryChange,
                onSyncClick = onSyncClick,
                onSettingsClick = onSettingsClick,
                onAccountClick = onAccountClick,
                onSessionsClick = onSessionsClick,
                onSelectAll = onSelectAll,
                onDeselectAll = onDeselectAll,
                onDeleteSelected = onDeleteSelected,
                onCancelSelection = onCancelSelection,
            )
        },
        floatingActionButton = {
            if (!state.isSelectionMode && !state.isLoading) {
                FloatingActionButton(
                    onClick = onCreateNote,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(
                            Res.string.create_note,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.isLoading -> FullScreenLoading()
                else -> NotesListContent(
                    state = state,
                    onNoteClick = onNoteClick,
                    onNoteLongClick = onNoteLongClick,
                    onRefresh = onRefresh,
                    onSwipeDelete = onSwipeDelete,
                    onToggleNoteSelection = onToggleNoteSelection,
                    onDismissCorruptedBanner =
                        onDismissCorruptedBanner,
                )
            }
        }
    }
}