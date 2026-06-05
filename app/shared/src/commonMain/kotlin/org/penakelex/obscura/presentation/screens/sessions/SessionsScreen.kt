package org.penakelex.obscura.presentation.screens.sessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
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
import obscura.app.shared.generated.resources.revoke
import obscura.app.shared.generated.resources.revoke_session_confirm
import obscura.app.shared.generated.resources.sessions
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.penakelex.obscura.presentation.components.common.FullScreenLoading
import org.penakelex.obscura.presentation.components.common.ObscuraSnackbarHostState
import org.penakelex.obscura.presentation.components.common.ObscuraTopBar
import org.penakelex.obscura.presentation.components.dialogs.ConfirmDialog
import org.penakelex.obscura.presentation.screens.sessions.components.SessionCard
import org.penakelex.obscura.presentation.theme.ObscuraDimens
import org.penakelex.obscura.presentation.util.event.UiEvent
import org.penakelex.obscura.presentation.util.event.toDisplayLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsScreen(
    snackbarHostState: ObscuraSnackbarHostState,
    onBackClick: () -> Unit,
    viewModel: SessionsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val showRevokeDialog = remember { mutableStateOf(false) }
    val revokeTitle = stringResource(Res.string.revoke)
    val revokeMessage = stringResource(Res.string.revoke_session_confirm)
    val confirmText = stringResource(Res.string.revoke)
    val cancelText = stringResource(Res.string.cancel)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.action?.toDisplayLabel(),
                    )
                }
                is UiEvent.Navigate, UiEvent.NavigateBack -> {
                }
            }
        }
    }

    if (showRevokeDialog.value) {
        ConfirmDialog(
            title = revokeTitle,
            message = revokeMessage,
            confirmText = confirmText,
            dismissText = cancelText,
            onConfirm = {
                showRevokeDialog.value = false
                viewModel.onRevokeConfirmed()
            },
            onDismiss = {
                showRevokeDialog.value = false
                viewModel.onRevokeDismissed()
            },
            isDestructive = true,
        )
    }

    when {
        state.isLoading -> FullScreenLoading()
        else -> SessionsScreenContent(
            state = state,
            onBackClick = onBackClick,
            onRefresh = viewModel::onRefresh,
            onRevokeSessionClick = { sessionId ->
                viewModel.onRevokeSessionClick(sessionId)
                showRevokeDialog.value = true
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionsScreenContent(
    state: SessionsUiState,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    onRevokeSessionClick: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            ObscuraTopBar(
                title = stringResource(Res.string.sessions),
                onBackClick = onBackClick,
            )
        },
    ) { padding ->
        androidx.compose.material3.pulltorefresh.PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(ObscuraDimens.Padding.m),
                verticalArrangement = Arrangement.spacedBy(
                    ObscuraDimens.Padding.s,
                ),
            ) {
                items(
                    items = state.sessions,
                    key = { it.id },
                ) { session ->
                    SessionCard(
                        session = session,
                        onRevokeClick = {
                            onRevokeSessionClick(session.id)
                        },
                    )
                }
            }
        }
    }
}