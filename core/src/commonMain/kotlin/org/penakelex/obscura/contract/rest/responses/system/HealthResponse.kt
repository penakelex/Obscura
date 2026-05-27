package org.penakelex.obscura.contract.rest.responses.system

import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
    val status: String,
    val uptimeSeconds: Long
)