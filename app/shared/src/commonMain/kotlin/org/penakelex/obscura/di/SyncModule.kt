package org.penakelex.obscura.di

import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.penakelex.obscura.data.remote.grpc.SyncApiClient
import org.penakelex.obscura.data.remote.http.NotesApiClient
import org.penakelex.obscura.data.sync.NotesGatewayImpl
import org.penakelex.obscura.data.sync.SyncGatewayImpl
import org.penakelex.obscura.domain.gateway.NotesGateway
import org.penakelex.obscura.domain.gateway.SettingsGateway
import org.penakelex.obscura.domain.gateway.SyncGateway
import org.penakelex.obscura.domain.repository.AuthRepository
import org.penakelex.obscura.domain.repository.NoteRepository
import org.penakelex.obscura.domain.usecase.sync.SyncNotesRestUseCase
import org.penakelex.obscura.domain.usecase.sync.SyncNotesUseCase

val syncModule: Module = module {
    single {
        SyncGatewayImpl(syncApiClient = get<SyncApiClient>())
    } bind SyncGateway::class

    single {
        NotesGatewayImpl(notesApiClient = get<NotesApiClient>())
    } bind NotesGateway::class

    factory {
        SyncNotesUseCase(
            noteRepository = get<NoteRepository>(),
            syncGateway = get<SyncGateway>(),
            authRepository = get<AuthRepository>(),
        )
    }

    factory {
        SyncNotesRestUseCase(
            noteRepository = get<NoteRepository>(),
            authRepository = get<AuthRepository>(),
            settingsGateway = get<SettingsGateway>(),
            notesGateway = get<NotesGateway>(),
        )
    }
}