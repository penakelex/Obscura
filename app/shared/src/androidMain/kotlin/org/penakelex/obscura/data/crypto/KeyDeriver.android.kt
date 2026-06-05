package org.penakelex.obscura.data.crypto

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

actual class KeyDeriver actual constructor(
    private val iterations: Int,
    private val saltSize: Int,
    private val keyLengthBits: Int,
) {
    actual fun deriveKey(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(
            password.toCharArray(),
            salt,
            iterations,
            keyLengthBits
        )
        return SecretKeyFactory
            .getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(spec)
            .encoded
            .also { spec.clearPassword() }
    }

    actual fun generateSalt(): ByteArray =
        ByteArray(saltSize).also { SecureRandom().nextBytes(it) }
}