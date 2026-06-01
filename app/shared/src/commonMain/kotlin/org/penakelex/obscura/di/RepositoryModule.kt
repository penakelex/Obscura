package org.penakelex.obscura.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.penakelex.obscura.data.crypto.CryptoProvider
import org.penakelex.obscura.data.repository.NoteRepositoryImpl
import org.penakelex.obscura.data.local.dao.NoteDao
import org.penakelex.obscura.data.remote.http.AuthApiClient
import org.penakelex.obscura.data.repository.AuthRepositoryImpl
import org.penakelex.obscura.data.storage.TokenStorage
import org.penakelex.obscura.domain.repository.AuthRepository
import org.penakelex.obscura.domain.repository.NoteRepository

val repositoryModule: Module = module {
    single<NoteRepository> {
        NoteRepositoryImpl(
            noteDao = get<NoteDao>(),
            cryptoProvider = get<CryptoProvider>(),
        )
    }

    single<AuthRepository> {
        AuthRepositoryImpl(
            authApiClient = get<AuthApiClient>(),
            tokenStorage = get<TokenStorage>(),
        )
    }
}