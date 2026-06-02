package org.penakelex.obscura.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.penakelex.obscura.data.crypto.CryptoProvider
import org.penakelex.obscura.data.local.db.DatabaseFactory
import org.penakelex.obscura.data.settings.SettingsStorage
import org.penakelex.obscura.data.storage.TokenStorage
import org.penakelex.obscura.presentation.util.clipboard.Clipboard
import org.penakelex.obscura.presentation.util.clipboard.DesktopClipboard

val desktopPlatformModule: Module = module {
    single { DatabaseFactory() }
    single { CryptoProvider() }
    single { TokenStorage() }
    single { SettingsStorage() }
    single<Clipboard> { DesktopClipboard() }
}