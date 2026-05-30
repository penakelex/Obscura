package org.penakelex.obscura.crypto

enum class CipherType(val id: Int) {
    AES_GCM(id = 1),
    XCHACHA20_POLY1305(id = 2);

    companion object {
        val DEFAULT: CipherType = AES_GCM

        fun fromIdOrFallback(id: Int): CipherType =
            entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}