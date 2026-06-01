package org.penakelex.obscura.domain.usecase.settings

import org.penakelex.obscura.domain.gateway.SettingsGateway
import org.penakelex.obscura.domain.model.common.CipherType

class SetDefaultCipherTypeUseCase(
    private val settingsGateway: SettingsGateway,
) {
    suspend operator fun invoke(cipherType: CipherType) {
        settingsGateway.setDefaultCipherType(cipherType)
    }
}