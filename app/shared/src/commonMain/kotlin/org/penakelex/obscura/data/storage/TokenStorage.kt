package org.penakelex.obscura.data.storage

import kotlinx.coroutines.flow.StateFlow
import org.penakelex.obscura.domain.model.auth.SessionData

expect class TokenStorage {
    val sessionFlow: StateFlow<SessionData?>

    suspend fun save(session: SessionData)
    suspend fun clear()
}