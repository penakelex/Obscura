package org.penakelex.obscura.presentation.screens.auth.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import obscura.app.shared.generated.resources.Res
import obscura.app.shared.generated.resources.app_name
import obscura.app.shared.generated.resources.register
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.penakelex.obscura.presentation.components.common.ObscuraSnackbarHostState
import org.penakelex.obscura.presentation.components.common.ObscuraTopBar
import org.penakelex.obscura.presentation.screens.auth.register.components.RegisterForm
import org.penakelex.obscura.presentation.theme.ObscuraDimens
import org.penakelex.obscura.presentation.util.event.UiEvent
import org.penakelex.obscura.presentation.util.event.toDisplayLabel

@Composable
fun RegisterScreen(
    snackbarHostState: ObscuraSnackbarHostState,
    viewModel: RegisterViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.action?.toDisplayLabel(),
                    )
                }
                is UiEvent.Navigate, UiEvent.NavigateBack -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            ObscuraTopBar(
                title = stringResource(Res.string.register),
                onBackClick = viewModel::onBackClick,
            )
        }
    ) { padding ->
        RegisterScreenContent(
            state = uiState,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
            onDeviceInfoChange = viewModel::onDeviceInfoChange,
            onRegisterClick = viewModel::register,
            onLoginClick = viewModel::onLoginClick,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
private fun RegisterScreenContent(
    state: RegisterUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onDeviceInfoChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(ObscuraDimens.Padding.l),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(ObscuraDimens.Padding.xxl))

            RegisterForm(
                state = state,
                onEmailChange = onEmailChange,
                onPasswordChange = onPasswordChange,
                onConfirmPasswordChange = onConfirmPasswordChange,
                onDeviceInfoChange = onDeviceInfoChange,
                onRegisterClick = onRegisterClick,
                onLoginClick = onLoginClick,
            )
        }
    }
}