package org.penakelex.obscura.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.penakelex.obscura.domain.repository.AuthRepository
import org.penakelex.obscura.presentation.screens.account.AccountViewModel
import org.penakelex.obscura.presentation.screens.auth.login.LoginViewModel
import org.penakelex.obscura.presentation.screens.auth.register.RegisterViewModel
import org.penakelex.obscura.presentation.screens.notes.editor.NoteEditorViewModel
import org.penakelex.obscura.presentation.screens.notes.list.NotesListViewModel
import org.penakelex.obscura.presentation.screens.sessions.SessionsViewModel
import org.penakelex.obscura.presentation.screens.settings.SettingsViewModel

val presentationModule: Module = module {
    viewModel {
        LoginViewModel(
            loginUseCase = get(),
            navigator = get(),
        )
    }

    viewModel {
        RegisterViewModel(
            registerUseCase = get(),
            navigator = get()
        )
    }

    viewModel {
        NotesListViewModel(
            observeNotesUseCase = get(),
            deleteNoteUseCase = get(),
            syncNotesUseCase = get(),
            syncNotesRestUseCase = get(),
            getSettingsUseCase = get(),
            authRepository = get<AuthRepository>(),
            navigator = get(),
        )
    }

    viewModel { (noteId: String?) ->
        NoteEditorViewModel(
            noteId = noteId,
            createNoteUseCase = get(),
            updateNoteUseCase = get(),
            getNoteUseCase = get(),
            getSettingsUseCase = get(),
            syncNotesUseCase = get(),
            navigator = get(),
        )
    }

    viewModel {
        SettingsViewModel(
            getSettingsUseCase = get(),
            setThemeModeUseCase = get(),
            setDefaultCipherTypeUseCase = get(),
            toggleAutoSyncUseCase = get(),
            syncNotesRestUseCase = get(),
            logoutUseCase = get(),
            logoutAllUseCase = get(),
            observeSession = get(),
            navigator = get(),
        )
    }

    viewModel {
        AccountViewModel(
            getProfileUseCase = get(),
            changePasswordUseCase = get(),
            changeEmailUseCase = get(),
            deleteAccountUseCase = get(),
            logoutUseCase = get(),
            logoutAllUseCase = get(),
            checkUnsyncedNotesUseCase = get(),
        )
    }

    viewModel {
        SessionsViewModel(
            listSessionsUseCase = get(),
            revokeSessionUseCase = get(),
        )
    }
}