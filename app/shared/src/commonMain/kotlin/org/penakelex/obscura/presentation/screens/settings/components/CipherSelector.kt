package org.penakelex.obscura.presentation.screens.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import obscura.app.shared.generated.resources.Res
import obscura.app.shared.generated.resources.cipher_aes_gcm
import obscura.app.shared.generated.resources.cipher_xchacha20
import org.jetbrains.compose.resources.stringResource
import org.penakelex.obscura.domain.model.common.CipherType
import org.penakelex.obscura.presentation.theme.ObscuraDimens

@Composable
fun CipherSelector(
    selectedCipher: CipherType,
    onCipherSelected: (CipherType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(
            ObscuraDimens.Padding.xs,
        ),
    ) {
        CipherType.entries.forEach { cipher ->
            CipherOption(
                cipher = cipher,
                isSelected = cipher == selectedCipher,
                onClick = { onCipherSelected(cipher) },
            )
        }
    }
}

@Composable
private fun CipherOption(
    cipher: CipherType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = when (cipher) {
        CipherType.AES_GCM -> stringResource(Res.string.cipher_aes_gcm)
        CipherType.XCHACHA20_POLY1305 ->
            stringResource(Res.string.cipher_xchacha20)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = ObscuraDimens.Padding.m,
                vertical = ObscuraDimens.Padding.s,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(
            ObscuraDimens.Padding.m,
        ),
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}