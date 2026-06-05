package org.penakelex.obscura.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.penakelex.obscura.data.crypto.CryptoProvider
import org.penakelex.obscura.domain.repository.AuthRepository
import org.penakelex.obscura.domain.repository.NoteRepository
import org.penakelex.obscura.domain.usecase.auth.session.AccountBootstrapUseCase
import org.penakelex.obscura.domain.usecase.auth.account.ChangeEmailUseCase
import org.penakelex.obscura.domain.usecase.auth.account.ChangePasswordUseCase
import org.penakelex.obscura.domain.usecase.auth.account.DeleteAccountUseCase
import org.penakelex.obscura.domain.usecase.auth.account.GetProfileUseCase
import org.penakelex.obscura.domain.usecase.auth.session.ListSessionsUseCase
import org.penakelex.obscura.domain.usecase.auth.authentication.LoginUseCase
import org.penakelex.obscura.domain.usecase.auth.session.LogoutAllUseCase
import org.penakelex.obscura.domain.usecase.auth.session.LogoutUseCase
import org.penakelex.obscura.domain.usecase.auth.authentication.MigrateGuestNotesUseCase
import org.penakelex.obscura.domain.usecase.auth.session.ObserveSessionUseCase
import org.penakelex.obscura.domain.usecase.auth.authentication.RegisterUseCase
import org.penakelex.obscura.domain.usecase.auth.session.RevokeSessionUseCase
import org.penakelex.obscura.domain.usecase.note.CheckUnsyncedNotesUseCase
import org.penakelex.obscura.domain.usecase.note.CreateNoteUseCase
import org.penakelex.obscura.domain.usecase.note.DeleteNoteUseCase
import org.penakelex.obscura.domain.usecase.note.GetNoteUseCase
import org.penakelex.obscura.domain.usecase.note.ObserveNotesUseCase
import org.penakelex.obscura.domain.usecase.note.UpdateNoteUseCase

val useCaseModule: Module = module {
    factory {
        AccountBootstrapUseCase(
            tokenStorage = get(),
            accountKeyStorage = get(),
            cryptoProvider = get(),
        )
    }

    factory {
        MigrateGuestNotesUseCase(
            noteDao = get(),
            cryptoProvider = get<CryptoProvider>(),
            guestCryptoManager = get(),
        )
    }

    factory {
        LoginUseCase(
            authRepository = get<AuthRepository>(),
            cryptoProvider = get<CryptoProvider>(),
            keyDeriver = get(),
            migrateGuestNotesUseCase = get(),
            accountKeyStorage = get(),
        )
    }
    factory {
        RegisterUseCase(
            authRepository = get<AuthRepository>(),
            cryptoProvider = get<CryptoProvider>(),
            keyDeriver = get(),
            migrateGuestNotesUseCase = get(),
            accountKeyStorage = get(),
        )
    }
    factory {
        LogoutUseCase(
            authRepository = get<AuthRepository>(),
            guestCryptoManager = get(),
            accountKeyStorage = get(),
            noteRepository = get<NoteRepository>(),
        )
    }
    factory {
        LogoutAllUseCase(
            authRepository = get<AuthRepository>(),
            guestCryptoManager = get(),
            accountKeyStorage = get(),
            noteRepository = get<NoteRepository>(),
        )
    }
    factory {
        ObserveSessionUseCase(authRepository = get<AuthRepository>())
    }
    factory {
        GetProfileUseCase(authRepository = get<AuthRepository>())
    }
    factory {
        ChangePasswordUseCase(
            authRepository = get<AuthRepository>(),
            cryptoProvider = get<CryptoProvider>(),
            keyDeriver = get(),
            accountKeyStorage = get(),
        )
    }
    factory {
        ChangeEmailUseCase(
            authRepository = get<AuthRepository>(),
            keyDeriver = get(),
        )
    }
    factory {
        DeleteAccountUseCase(
            authRepository = get<AuthRepository>(),
            keyDeriver = get(),
            accountKeyStorage = get(),
        )
    }
    factory {
        ListSessionsUseCase(authRepository = get<AuthRepository>())
    }
    factory {
        RevokeSessionUseCase(authRepository = get<AuthRepository>())
    }

    factory {
        CreateNoteUseCase(noteRepository = get<NoteRepository>())
    }
    factory {
        UpdateNoteUseCase(noteRepository = get<NoteRepository>())
    }
    factory {
        DeleteNoteUseCase(noteRepository = get<NoteRepository>())
    }
    factory {
        GetNoteUseCase(noteRepository = get<NoteRepository>())
    }
    factory {
        ObserveNotesUseCase(noteRepository = get<NoteRepository>())
    }
    factory {
        CheckUnsyncedNotesUseCase(
            noteRepository = get<NoteRepository>(),
        )
    }
}