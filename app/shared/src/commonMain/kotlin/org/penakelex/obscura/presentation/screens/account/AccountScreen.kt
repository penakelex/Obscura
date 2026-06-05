package org.penakelex.obscura.presentation.screens.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import obscura.app.shared.generated.resources.Res
import obscura.app.shared.generated.resources.account
import obscura.app.shared.generated.resources.change_email
import obscura.app.shared.generated.resources.change_password
import obscura.app.shared.generated.resources.current_email
import obscura.app.shared.generated.resources.delete_account
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.penakelex.obscura.presentation.components.common.ButtonVariant
import org.penakelex.obscura.presentation.components.common.FullScreenLoading
import org.penakelex.obscura.presentation.components.common.ObscuraButton
import org.penakelex.obscura.presentation.components.common.ObscuraSnackbarHostState
import org.penakelex.obscura.presentation.components.common.ObscuraTopBar
import org.penakelex.obscura.presentation.screens.account.components.ChangeEmailDialog
import org.penakelex.obscura.presentation.screens.account.components.ChangePasswordDialog
import org.penakelex.obscura.presentation.screens.account.components.DeleteAccountDialog
import org.penakelex.obscura.presentation.theme.ObscuraDimens
import org.penakelex.obscura.presentation.util.event.UiEvent
import org.penakelex.obscura.presentation.util.event.toDisplayLabel

@Composable
fun AccountScreen(
    snackbarHostState: ObscuraSnackbarHostState,
    onBackClick: () -> Unit,
    viewModel: AccountViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    val actionLabel = event.action?.toDisplayLabel()
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = actionLabel,
                    )
                }
                is UiEvent.Navigate, UiEvent.NavigateBack -> {
                }
            }
        }
    }

    if (state.isPasswordDialogVisible) {
        ChangePasswordDialog(
            isOperationInProgress = state.isOperationInProgress,
            onConfirm = viewModel::changePassword,
            onDismiss = viewModel::onPasswordDialogDismiss,
        )
    }

    if (state.isEmailDialogVisible) {
        ChangeEmailDialog(
            isOperationInProgress = state.isOperationInProgress,
            onConfirm = viewModel::changeEmail,
            onDismiss = viewModel::onEmailDialogDismiss,
        )
    }

    if (state.isDeleteDialogVisible) {
        DeleteAccountDialog(
            isOperationInProgress = state.isOperationInProgress,
            onConfirm = viewModel::deleteAccount,
            onDismiss = viewModel::onDeleteDialogDismiss,
        )
    }

    when {
        state.isLoading -> FullScreenLoading()
        else -> AccountScreenContent(
            state = state,
            onBackClick = onBackClick,
            onChangePasswordClick = viewModel::onChangePasswordClick,
            onChangeEmailClick = viewModel::onChangeEmailClick,
            onDeleteAccountClick = viewModel::onDeleteAccountClick,
        )
    }
}

@Composable
private fun AccountScreenContent(
    state: AccountUiState,
    onBackClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onChangeEmailClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            ObscuraTopBar(
                title = stringResource(Res.string.account),
                onBackClick = onBackClick,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(ObscuraDimens.Padding.m),
            verticalArrangement = Arrangement.spacedBy(
                ObscuraDimens.Padding.l,
            ),
        ) {
            ProfileSection(email = state.email)

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            SecuritySection(
                onChangePasswordClick = onChangePasswordClick,
                onChangeEmailClick = onChangeEmailClick,
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            DangerZoneSection(
                onDeleteAccountClick = onDeleteAccountClick,
            )
        }
    }
}

@Composable
private fun ProfileSection(
    email: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(
            ObscuraDimens.Padding.s,
        ),
    ) {
        Text(
            text = stringResource(Res.string.current_email),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = email,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SecuritySection(
    onChangePasswordClick: () -> Unit,
    onChangeEmailClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(
            ObscuraDimens.Padding.m,
        ),
    ) {
        ObscuraButton(
            text = stringResource(Res.string.change_password),
            onClick = onChangePasswordClick,
            variant = ButtonVariant.SECONDARY,
            modifier = Modifier.fillMaxWidth(),
        )

        ObscuraButton(
            text = stringResource(Res.string.change_email),
            onClick = onChangeEmailClick,
            variant = ButtonVariant.SECONDARY,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DangerZoneSection(
    onDeleteAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(
            ObscuraDimens.Padding.s,
        ),
    ) {
        ObscuraButton(
            text = stringResource(Res.string.delete_account),
            onClick = onDeleteAccountClick,
            variant = ButtonVariant.DESTRUCTIVE,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}