package org.penakelex.obscura.presentation.screens.account.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import obscura.app.shared.generated.resources.Res
import obscura.app.shared.generated.resources.change_email
import obscura.app.shared.generated.resources.change_password
import org.jetbrains.compose.resources.stringResource
import org.penakelex.obscura.presentation.components.common.ButtonVariant
import org.penakelex.obscura.presentation.components.common.ObscuraButton
import org.penakelex.obscura.presentation.theme.ObscuraDimens

@Composable
fun SecuritySection(
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