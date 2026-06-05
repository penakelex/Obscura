package org.penakelex.obscura.presentation.screens.auth.register.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import obscura.app.shared.generated.resources.Res
import obscura.app.shared.generated.resources.device_info_optional
import obscura.app.shared.generated.resources.device_info_placeholder
import obscura.app.shared.generated.resources.have_account
import obscura.app.shared.generated.resources.login
import obscura.app.shared.generated.resources.email
import obscura.app.shared.generated.resources.email_placeholder
import obscura.app.shared.generated.resources.password
import obscura.app.shared.generated.resources.password_warning
import obscura.app.shared.generated.resources.register
import org.jetbrains.compose.resources.stringResource
import org.penakelex.obscura.presentation.components.common.ButtonVariant
import org.penakelex.obscura.presentation.components.common.ObscuraButton
import org.penakelex.obscura.presentation.components.common.ObscuraTextField
import org.penakelex.obscura.presentation.screens.auth.register.RegisterUiState
import org.penakelex.obscura.presentation.theme.ObscuraDimens
import org.penakelex.obscura.presentation.util.error.toDisplayString

@Composable
fun RegisterForm(
    state: RegisterUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onDeviceInfoChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ObscuraDimens.Padding.l),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ObscuraDimens.Padding.m),
    ) {
        ObscuraTextField(
            value = state.email,
            onValueChange = onEmailChange,
            label = stringResource(Res.string.email),
            placeholder = stringResource(Res.string.email_placeholder),
            errorMessage = state.emailError?.toDisplayString(),
            enabled = !state.isLoading,
            keyboardType = KeyboardType.Email,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        ObscuraTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            label = stringResource(Res.string.password),
            errorMessage = state.passwordError?.toDisplayString(),
            enabled = !state.isLoading,
            isPassword = true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        ObscuraTextField(
            value = state.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = stringResource(Res.string.password) + " *",
            errorMessage = state.confirmPasswordError?.toDisplayString(),
            enabled = !state.isLoading,
            isPassword = true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        ObscuraTextField(
            value = state.deviceInfo,
            onValueChange = onDeviceInfoChange,
            label = stringResource(Res.string.device_info_optional),
            placeholder = stringResource(Res.string.device_info_placeholder),
            errorMessage = state.deviceInfoError?.toDisplayString(),
            enabled = !state.isLoading,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        PasswordWarningCard()

        Spacer(Modifier.height(ObscuraDimens.Padding.s))

        ObscuraButton(
            text = stringResource(Res.string.register),
            onClick = onRegisterClick,
            variant = ButtonVariant.PRIMARY,
            enabled = state.isRegisterEnabled,
            loading = state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(ObscuraDimens.Padding.m))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(Res.string.have_account),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(
                onClick = onLoginClick,
                enabled = !state.isLoading,
            ) {
                Text(text = stringResource(Res.string.login))
            }
        }
    }
}

@Composable
private fun PasswordWarningCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(ObscuraDimens.Padding.m),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ObscuraDimens.Padding.s),
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
            )
            Text(
                text = stringResource(Res.string.password_warning),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}