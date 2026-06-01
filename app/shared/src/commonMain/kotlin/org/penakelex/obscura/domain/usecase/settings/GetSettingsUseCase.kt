package org.penakelex.obscura.domain.usecase.settings

import kotlinx.coroutines.flow.Flow
import org.penakelex.obscura.domain.gateway.SettingsGateway
import org.penakelex.obscura.domain.model.settings.AppSettings

class GetSettingsUseCase(
    private val settingsGateway: SettingsGateway,
) {
    fun observe(): Flow<AppSettings> = settingsGateway.observe()

    suspend fun get(): AppSettings = settingsGateway.get()
}