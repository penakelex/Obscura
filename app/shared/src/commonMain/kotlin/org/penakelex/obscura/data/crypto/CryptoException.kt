package org.penakelex.obscura.data.crypto

sealed class CryptoException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    class NotInitialized : CryptoException(
        "CryptoProvider is not initialized. Call initialize() first."
    )

    class EncryptionFailed(cause: Throwable) : CryptoException(
        "Encryption failed: ${cause.message}", cause
    )

    class DecryptionFailed(cause: Throwable) : CryptoException(
        "Decryption failed: ${cause.message}", cause
    )

    class KeysetDecryptionFailed(cause: Throwable) : CryptoException(
        "Failed to decrypt keyset — wrong master password?", cause
    )

    class KeysetEncodingFailed(cause: Throwable) : CryptoException(
        "Failed to decode keyset data: ${cause.message}", cause
    )
}