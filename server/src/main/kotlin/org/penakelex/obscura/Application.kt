package org.penakelex.obscura

import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.ktor.ext.inject
import org.penakelex.obscura.config.ServerConfig
import org.penakelex.obscura.db.DatabaseManager
import org.penakelex.obscura.grpc.GrpcServerManager
import org.penakelex.obscura.jobs.NotesCleanupJob
import org.penakelex.obscura.jobs.SessionCleanupJob
import org.penakelex.obscura.plugins.configureDependencyInjection
import org.penakelex.obscura.plugins.configureErrorHandling
import org.penakelex.obscura.plugins.configureMonitoring
import org.penakelex.obscura.plugins.configureRateLimiting
import org.penakelex.obscura.plugins.configureSerialization
import org.penakelex.obscura.rest.routing.authRouting
import org.penakelex.obscura.rest.routing.healthRouting
import org.penakelex.obscura.rest.routing.noteRouting
import org.penakelex.obscura.rest.service.AuthService
import org.penakelex.obscura.rest.service.NoteService
import kotlin.time.Duration.Companion.seconds

fun main() {
    dotenv().entries().forEach { entry ->
        if (System.getProperty(entry.key) == null) {
            System.setProperty(entry.key, entry.value)
        }
    }

    val config = ServerConfig()

    DatabaseManager.init(config.database)

    val server = embeddedServer(
        Netty,
        port = config.network.port,
        host = config.network.host,
        module = { module(config) }
    )

    Runtime.getRuntime().addShutdownHook(Thread {
        server.stop(
            gracePeriodMillis = 5.seconds.inWholeMilliseconds,
            timeoutMillis = 10.seconds.inWholeMilliseconds,
        )
        DatabaseManager.close()
    })

    server.start(wait = true)
}

fun Application.module(serverConfig: ServerConfig) {
    configureDependencyInjection(serverConfig)
    configureMonitoring()
    configureSerialization()
    configureErrorHandling()
    configureRateLimiting()

    val cleanupJob by inject<SessionCleanupJob>()
    val notesCleanupJob by inject<NotesCleanupJob>()
    val jobScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    cleanupJob.start(jobScope)
    notesCleanupJob.start(jobScope)

    val grpcManager by inject<GrpcServerManager>()

    monitor.subscribe(ApplicationStarted) {
        grpcManager.start()
    }

    monitor.subscribe(ApplicationStopped) {
        grpcManager.stop()
        jobScope.cancel()
    }

    val authService by inject<AuthService>()
    val noteService by inject<NoteService>()

    routing {
        healthRouting()
        authRouting(authService)
        noteRouting(noteService)
    }
}