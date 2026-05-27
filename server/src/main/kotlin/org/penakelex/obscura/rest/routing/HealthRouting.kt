package org.penakelex.obscura.rest.routing

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.json.Json
import org.penakelex.obscura.contract.rest.responses.system.HealthResponse
import org.penakelex.obscura.db.DatabaseManager

fun Route.healthRouting() {
    get("/health") {
        val healthy = DatabaseManager.isHealthy()
        call.respondText(
            text = Json.encodeToString(
                HealthResponse(
                    status = if (healthy) "ok" else "unhealthy",
                    uptimeSeconds = DatabaseManager.uptimeSeconds()
                )
            ),
            contentType = ContentType.Application.Json,
            status = if (healthy) HttpStatusCode.OK
            else HttpStatusCode.ServiceUnavailable
        )
    }
}