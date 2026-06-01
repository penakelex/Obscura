package org.penakelex.obscura.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.penakelex.obscura.domain.repository.AuthRepository
import org.penakelex.obscura.domain.repository.NoteRepository
import org.penakelex.obscura.domain.usecase.auth.*
import org.penakelex.obscura.domain.usecase.note.*

val useCaseModule: Module = module {
    factory { LoginUseCase(authRepository = get<AuthRepository>()) }
    factory {
        RegisterUseCase(authRepository = get<AuthRepository>())
    }
    factory { LogoutUseCase(authRepository = get<AuthRepository>()) }
    factory {
        LogoutAllUseCase(authRepository = get<AuthRepository>())
    }
    factory {
        ObserveSessionUseCase(authRepository = get<AuthRepository>())
    }
    factory {
        GetProfileUseCase(authRepository = get<AuthRepository>())
    }
    factory {
        ChangePasswordUseCase(authRepository = get<AuthRepository>())
    }
    factory {
        ChangeEmailUseCase(authRepository = get<AuthRepository>())
    }
    factory {
        DeleteAccountUseCase(authRepository = get<AuthRepository>())
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
}