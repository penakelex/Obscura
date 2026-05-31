package org.penakelex.obscura.di

import org.koin.dsl.module
import org.penakelex.obscura.config.ServerConfig
import org.penakelex.obscura.db.repository.NoteRepository
import org.penakelex.obscura.db.repository.SessionRepository
import org.penakelex.obscura.db.repository.UserRepository
import org.penakelex.obscura.jobs.NotesCleanupJob
import org.penakelex.obscura.jobs.SessionCleanupJob
import org.penakelex.obscura.rest.service.AuthService
import org.penakelex.obscura.rest.service.NoteService
import org.penakelex.obscura.security.PasswordHasher
import org.penakelex.obscura.security.ratelimit.InMemoryRateLimiter
import org.penakelex.obscura.security.ratelimit.RateLimitConfig
import kotlin.time.Duration.Companion.minutes

fun appModule(serverConfig: ServerConfig) = module {
    single { serverConfig }

    single {
        PasswordHasher(
            passwordConfig = serverConfig.security.password,
        )
    }

    single { UserRepository() }
    single {
        SessionRepository(
            sessionConfig = serverConfig.security.session,
        )
    }
    single {
        NoteRepository(
            validationConfig = serverConfig.validation,
        )
    }

    single {
        SessionCleanupJob(
            jobsConfig = serverConfig.jobs,
            sessionRepository = get(),
        )
    }
    single {
        NotesCleanupJob(
            jobsConfig = serverConfig.jobs,
            noteRepository = get(),
        )
    }

    single {
        InMemoryRateLimiter(
            RateLimitConfig(
                maxRequests = serverConfig
                    .security.rateLimit.maxRequests,
                window = serverConfig
                    .security.rateLimit.windowMinutes.minutes,
            )
        )
    }

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
            validationConfig = serverConfig.validation,
        )
    }
}