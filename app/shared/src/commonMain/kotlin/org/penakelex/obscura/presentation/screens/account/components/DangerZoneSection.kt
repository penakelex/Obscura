package org.penakelex.obscura.presentation.screens.account.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import obscura.app.shared.generated.resources.Res
import obscura.app.shared.generated.resources.delete_account
import org.jetbrains.compose.resources.stringResource
import org.penakelex.obscura.presentation.components.common.ButtonVariant
import org.penakelex.obscura.presentation.components.common.ObscuraButton
import org.penakelex.obscura.presentation.theme.ObscuraDimens

@Composable
fun DangerZoneSection(
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