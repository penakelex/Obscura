package org.penakelex.obscura.data.crypto

expect class KeyDeriver(
    iterations: Int = DEFAULT_ITERATIONS,
    saltSize: Int = DEFAULT_SALT_SIZE,
    keyLengthBits: Int = DERIVED_KEY_LENGTH_BITS,
) {
    fun deriveKey(password: String, salt: ByteArray): ByteArray
    fun generateSalt(): ByteArray
}

const val DEFAULT_ITERATIONS: Int = 100_000
const val DEFAULT_SALT_SIZE: Int = 16
const val DERIVED_KEY_LENGTH_BITS: Int = 512
const val AUTH_KEY_OFFSET: Int = 0
const val MASTER_KEY_OFFSET: Int = 32
const val KEY_PART_LENGTH: Int = 32