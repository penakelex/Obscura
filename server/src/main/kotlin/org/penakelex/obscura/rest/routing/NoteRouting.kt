package org.penakelex.obscura.rest.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.penakelex.obscura.contract.rest.requests.sync.SyncRequest
import org.penakelex.obscura.exception.resource.NotFoundException
import org.penakelex.obscura.rest.service.NoteService
import org.penakelex.obscura.security.authenticate
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
fun Route.noteRouting(noteService: NoteService) {
    route("/api/notes") {
        get {
            val session = call.authenticate()
            val limit = call.request.queryParameters["limit"]
                ?.toIntOrNull()
                ?: NoteService.DEFAULT_PAGE_SIZE
            val offset = call.request.queryParameters["offset"]
                ?.toIntOrNull()
                ?: 0
            val includeDeleted = call.request
                .queryParameters["includeDeleted"]
                ?.toBooleanStrictOrNull()
                ?: false

            val response = noteService.listNotes(
                userId = session.userId,
                limit = limit,
                offset = offset,
                includeDeleted = includeDeleted
            )
            call.respond(HttpStatusCode.OK, response)
        }

        get("/{noteId}") {
            val session = call.authenticate()
            val noteId = call.parameters["noteId"]
                ?: throw NotFoundException.NoteNotFound("null")
            val response = noteService
                .getNoteById(session.userId, noteId)
            call.respond(HttpStatusCode.OK, response)
        }

        post("/sync") {
            val session = call.authenticate()
            val request = call.receive<SyncRequest>()
            val response = noteService.sync(session.userId, request)
            call.respond(HttpStatusCode.OK, response)
        }

        get("/delta") {
            val session = call.authenticate()
            val since = call.request.queryParameters["since"]
                ?.toLongOrNull()
                ?: 0L
            val response = noteService
                .getDelta(session.userId, since)
            call.respond(HttpStatusCode.OK, response)
        }
    }
}