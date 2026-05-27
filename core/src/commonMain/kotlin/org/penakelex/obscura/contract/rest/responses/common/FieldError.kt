package org.penakelex.obscura.contract.rest.responses.common

import kotlinx.serialization.Serializable

@Serializable
data class FieldError(
    val field: String,
    val code: String,
    val message: String
)