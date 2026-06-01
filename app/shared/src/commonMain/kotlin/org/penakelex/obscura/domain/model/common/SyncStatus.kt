package org.penakelex.obscura.domain.model.common

enum class SyncStatus(val code: Int) {
    SYNCED(0),
    PENDING(1),
    CONFLICT(2);

    companion object {
        fun fromCode(code: Int): SyncStatus =
            entries.firstOrNull { it.code == code } ?: SYNCED
    }
}