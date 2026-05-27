package org.penakelex.obscura.contract.rest.responses.auth

import kotlinx.serialization.Serializable

@Serializable
data class SessionsListResponse(
    val sessions: List<SessionInfo>,
    val totalCount: Int
)