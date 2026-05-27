package org.penakelex.obscura.exception

import io.ktor.http.HttpStatusCode

abstract class ObscuraException(
    val errorCode: String,
    val httpStatus: HttpStatusCode,
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)