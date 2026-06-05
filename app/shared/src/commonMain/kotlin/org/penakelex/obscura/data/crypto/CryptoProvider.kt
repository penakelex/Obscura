package org.penakelex.obscura.data.crypto

import org.penakelex.obscura.domain.model.common.CipherType

const val CRYPTO_LOG_TAG = "CryptoProvider"

expect class CryptoProvider {
    val isInitialized: Boolean

    fun initialize(
        masterKey: ByteArray,
        encryptedKeysetJson: String?
    ): String

    fun encrypt(
        plaintext: ByteArray,
        cipherType: CipherType
    ): ByteArray

    fun decrypt(
        ciphertext: ByteArray,
        cipherType: CipherType
    ): ByteArray

    fun reEncryptKeyset(
        currentEncryptedKeyset: String,
        currentMasterKey: ByteArray,
        newMasterKey: ByteArray
    ): String

    fun reset()
}