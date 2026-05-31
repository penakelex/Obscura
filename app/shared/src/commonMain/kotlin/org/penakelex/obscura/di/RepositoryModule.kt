package org.penakelex.obscura.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.penakelex.obscura.crypto.CryptoProvider
import org.penakelex.obscura.data.repository.NoteRepository
import org.penakelex.obscura.persistence.dao.NoteDao

val repositoryModule: Module = module {
    single {
        NoteRepository(
            noteDao = get<NoteDao>(),
            cryptoProvider = get<CryptoProvider>(),
        )
    }
}