package org.penakelex.obscura.rest.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.penakelex.obscura.contract.rest.requests.account.ChangeEmailRequest
import org.penakelex.obscura.contract.rest.requests.account.ChangePasswordRequest
import org.penakelex.obscura.contract.rest.requests.auth.LoginRequest
import org.penakelex.obscura.contract.rest.requests.auth.RegisterRequest
import org.penakelex.obscura.contract.rest.responses.auth.LogoutAllResponse
import org.penakelex.obscura.rest.service.AuthService
import org.penakelex.obscura.security.authenticate
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
fun Route.authRouting(authService: AuthService) {
    route("/api/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val response = authService.register(request)
            call.respond(HttpStatusCode.Created, response)
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val response = authService.login(request)
            call.respond(HttpStatusCode.OK, response)
        }

        post("/logout") {
            val session = call.authenticate()
            val response = authService.logout(session.id)
            call.respond(HttpStatusCode.OK, response)
        }

        post("/logout/all") {
            val session = call.authenticate()
            val revokedCount =
                authService.logoutAllSessions(session.userId)
            call.respond(
                HttpStatusCode.OK,
                LogoutAllResponse(revokedCount = revokedCount)
            )
        }

        get("/me") {
            val session = call.authenticate()
            val response = authService.getProfile(session.userId)
            call.respond(HttpStatusCode.OK, response)
        }

        put("/password") {
            val session = call.authenticate()
            val request = call.receive<ChangePasswordRequest>()
            val response =
                authService.changePassword(session.userId, request)
            call.respond(HttpStatusCode.OK, response)
        }

        put("/email") {
            val session = call.authenticate()
            val request = call.receive<ChangeEmailRequest>()
            val response =
                authService.changeEmail(session.userId, request)
            call.respond(HttpStatusCode.OK, response)
        }
    }
}