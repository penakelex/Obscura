package org.penakelex.obscura.data.crypto

import org.penakelex.obscura.domain.model.common.CipherType

const val CRYPTO_LOG_TAG = "CryptoProvider"

expect class CryptoProvider {
    fun encrypt(
        plaintext: ByteArray,
        cipherType: CipherType
    ): ByteArray

    fun decrypt(
        ciphertext: ByteArray,
        cipherType: CipherType
    ): ByteArray
}