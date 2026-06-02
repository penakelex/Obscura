package org.penakelex.obscura.di

import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.penakelex.obscura.data.settings.SettingsGatewayImpl
import org.penakelex.obscura.data.settings.SettingsStorage
import org.penakelex.obscura.domain.gateway.SettingsGateway
import org.penakelex.obscura.domain.usecase.settings.*

val settingsModule: Module = module {
    single {
        SettingsGatewayImpl(storage = get<SettingsStorage>())
    } bind SettingsGateway::class

    factory { GetSettingsUseCase(settingsGateway = get()) }
    factory { SetDefaultCipherTypeUseCase(settingsGateway = get()) }
    factory { ToggleAutoSyncUseCase(settingsGateway = get()) }
    factory {
        UpdateLastSyncTimestampUseCase(settingsGateway = get())
    }
    factory { SetThemeModeUseCase(settingsGateway = get()) }
}