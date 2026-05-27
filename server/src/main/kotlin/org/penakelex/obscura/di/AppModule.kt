package org.penakelex.obscura.di

import org.koin.dsl.module
import org.penakelex.obscura.config.ServerConfig
import org.penakelex.obscura.db.repository.NoteRepository
import org.penakelex.obscura.db.repository.SessionRepository
import org.penakelex.obscura.db.repository.UserRepository
import org.penakelex.obscura.jobs.SessionCleanupJob
import org.penakelex.obscura.rest.service.AuthService
import org.penakelex.obscura.rest.service.NoteService
import org.penakelex.obscura.security.PasswordHasher

fun appModule(serverConfig: ServerConfig) = module {
    single { serverConfig }

    single { PasswordHasher(serverConfig.security.password) }

    single { UserRepository() }
    single { SessionRepository(serverConfig.security.session) }
    single { NoteRepository(serverConfig.security) }

    single { SessionCleanupJob(serverConfig.jobs, get()) }

    single {
        AuthService(
            userRepository = get(),
            sessionRepository = get(),
            noteRepository = get(),
            passwordHasher = get(),
            validationConfig = serverConfig.validation,
        )
    }

    single {
        NoteService(
            noteRepository = get(),
        )
    }
}