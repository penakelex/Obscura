package org.penakelex.obscura.di

import android.content.Context
import org.koin.core.module.Module
import org.koin.dsl.module
import org.penakelex.obscura.data.crypto.CryptoProvider
import org.penakelex.obscura.data.local.db.DatabaseFactory
import org.penakelex.obscura.data.settings.SettingsStorage
import org.penakelex.obscura.data.storage.TokenStorage
import org.penakelex.obscura.presentation.util.clipboard.AndroidClipboard
import org.penakelex.obscura.presentation.util.clipboard.Clipboard

fun androidPlatformModule(context: Context): Module = module {
    single { context }
    single { DatabaseFactory(context) }
    single { CryptoProvider(context) }
    single { TokenStorage(context) }
    single { SettingsStorage(context) }
    single<Clipboard> { AndroidClipboard(context) }
}