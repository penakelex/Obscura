package org.penakelex.obscura.crypto

class CryptoException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)