package org.penakelex.obscura.data.crypto

data class DerivedKeys(
    val authKey: ByteArray,
    val masterKey: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DerivedKeys) return false
        return authKey.contentEquals(other.authKey) &&
                masterKey.contentEquals(other.masterKey)
    }

    override fun hashCode(): Int =
        31 * authKey.contentHashCode() + masterKey.contentHashCode()
}

fun ByteArray.toDerivedKeys(): DerivedKeys = DerivedKeys(
    authKey = copyOfRange(
        AUTH_KEY_OFFSET,
        AUTH_KEY_OFFSET + KEY_PART_LENGTH
    ),
    masterKey = copyOfRange(
        MASTER_KEY_OFFSET,
        MASTER_KEY_OFFSET + KEY_PART_LENGTH
    ),
)