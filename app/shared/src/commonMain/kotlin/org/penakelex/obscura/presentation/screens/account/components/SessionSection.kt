package org.penakelex.obscura.presentation.screens.account.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import obscura.app.shared.generated.resources.Res
import obscura.app.shared.generated.resources.logout
import obscura.app.shared.generated.resources.logout_all
import org.jetbrains.compose.resources.stringResource
import org.penakelex.obscura.presentation.components.common.ButtonVariant
import org.penakelex.obscura.presentation.components.common.ObscuraButton
import org.penakelex.obscura.presentation.theme.ObscuraDimens

@Composable
fun SessionSection(
    onLogoutClick: () -> Unit,
    onLogoutAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(
            ObscuraDimens.Padding.m,
        ),
    ) {
        ObscuraButton(
            text = stringResource(Res.string.logout),
            onClick = onLogoutClick,
            variant = ButtonVariant.SECONDARY,
            modifier = Modifier.fillMaxWidth(),
        )
        ObscuraButton(
            text = stringResource(Res.string.logout_all),
            onClick = onLogoutAllClick,
            variant = ButtonVariant.DESTRUCTIVE,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}