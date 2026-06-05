package org.penakelex.obscura.presentation.screens.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import obscura.app.shared.generated.resources.Res
import obscura.app.shared.generated.resources.account
import obscura.app.shared.generated.resources.login
import obscura.app.shared.generated.resources.logout
import obscura.app.shared.generated.resources.logout_all
import obscura.app.shared.generated.resources.register
import obscura.app.shared.generated.resources.sessions
import org.jetbrains.compose.resources.stringResource
import org.penakelex.obscura.presentation.theme.ObscuraDimens

@Composable
fun AccountLinks(
    isAuthenticated: Boolean,
    onAccountClick: () -> Unit,
    onSessionsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onLogoutAllClick: () -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (isAuthenticated) {
            AccountLinkItem(
                label = stringResource(Res.string.account),
                onClick = onAccountClick,
            )
            AccountLinkItem(
                label = stringResource(Res.string.sessions),
                onClick = onSessionsClick,
            )
            HorizontalDivider(
                modifier = Modifier.padding(
                    vertical = ObscuraDimens.Padding.s,
                ),
            )
            AccountLinkItem(
                label = stringResource(Res.string.logout),
                onClick = onLogoutClick,
                isDestructive = false,
            )
            AccountLinkItem(
                label = stringResource(Res.string.logout_all),
                onClick = onLogoutAllClick,
                isDestructive = true,
            )
        } else {
            AccountLinkItem(
                label = stringResource(Res.string.login),
                onClick = onLoginClick,
            )
            AccountLinkItem(
                label = stringResource(Res.string.register),
                onClick = onRegisterClick,
            )
        }
    }
}

@Composable
private fun AccountLinkItem(
    modifier: Modifier = Modifier,
    label: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
) {
    val textColor = if (isDestructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Text(
        text = label,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = ObscuraDimens.Padding.m,
                vertical = ObscuraDimens.Padding.m,
            ),
        style = MaterialTheme.typography.bodyLarge,
        color = textColor,
    )
}