package org.penakelex.obscura.domain.usecase.settings

import org.penakelex.obscura.domain.gateway.SettingsGateway
import org.penakelex.obscura.domain.model.settings.ThemeMode

class SetThemeModeUseCase(
    private val settingsGateway: SettingsGateway,
) {
    suspend operator fun invoke(mode: ThemeMode) {
        settingsGateway.setThemeMode(mode)
    }
}