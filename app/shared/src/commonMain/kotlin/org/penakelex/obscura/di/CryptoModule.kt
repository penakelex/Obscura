package org.penakelex.obscura.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.penakelex.obscura.data.crypto.CryptoProvider
import org.penakelex.obscura.data.crypto.GuestCryptoManager
import org.penakelex.obscura.data.storage.GuestKeyStorage

val cryptoModule: Module = module {
    single {
        GuestCryptoManager(
            cryptoProvider = get<CryptoProvider>(),
            guestKeyStorage = get<GuestKeyStorage>(),
        )
    }
}