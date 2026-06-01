package org.penakelex.obscura.domain.model.sync

enum class SyncResultStatus {
    SUCCESS,
    PARTIAL,
    CONFLICT_RESOLVED,
    AUTH_ERROR
}