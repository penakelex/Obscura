package org.penakelex.obscura.domain.usecase.settings

import org.penakelex.obscura.domain.gateway.SettingsGateway

class ToggleDarkModeUseCase(
    private val settingsGateway: SettingsGateway,
) {
    suspend operator fun invoke(enabled: Boolean) {
        settingsGateway.setDarkMode(enabled)
    }
}