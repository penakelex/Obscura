package org.penakelex.obscura.di

import android.content.Context
import org.koin.core.module.Module
import org.koin.dsl.module
import org.penakelex.obscura.crypto.CryptoProvider
import org.penakelex.obscura.persistence.db.DatabaseFactory

fun androidPlatformModule(context: Context): Module = module {
    single { context }
    single { DatabaseFactory(context) }
    single { CryptoProvider(context) }
}