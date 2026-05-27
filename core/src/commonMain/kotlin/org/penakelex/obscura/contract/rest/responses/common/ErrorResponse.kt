package org.penakelex.obscura.contract.rest.responses.common

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val error: String,
    val code: String? = null,
    val traceId: String? = null,
    val timestamp: Long? = null,
    val details: List<FieldError>? = null,
    val retryAfterSeconds: Long? = null
)