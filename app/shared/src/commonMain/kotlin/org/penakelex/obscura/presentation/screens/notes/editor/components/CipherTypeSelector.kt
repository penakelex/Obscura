package org.penakelex.obscura.presentation.screens.notes.editor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import obscura.app.shared.generated.resources.Res
import obscura.app.shared.generated.resources.cipher_aes_gcm
import obscura.app.shared.generated.resources.cipher_xchacha20
import org.jetbrains.compose.resources.stringResource
import org.penakelex.obscura.domain.model.common.CipherType
import org.penakelex.obscura.presentation.theme.ObscuraDimens

@Composable
fun CipherTypeSelector(
    selectedCipher: CipherType,
    onCipherSelected: (CipherType) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ObscuraDimens.Padding.m),
        horizontalArrangement = Arrangement.spacedBy(
            ObscuraDimens.Padding.s
        ),
    ) {
        CipherType.entries.forEach { cipher ->
            FilterChip(
                selected = selectedCipher == cipher,
                onClick = { onCipherSelected(cipher) },
                label = {
                    Text(
                        text = when (cipher) {
                            CipherType.AES_GCM ->
                                stringResource(Res.string.cipher_aes_gcm)
                            CipherType.XCHACHA20_POLY1305 ->
                                stringResource(Res.string.cipher_xchacha20)
                        }
                    )
                },
                enabled = enabled,
            )
        }
    }
}