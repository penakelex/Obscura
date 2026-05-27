package org.penakelex.obscura.db.model

import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class Session @OptIn(ExperimentalUuidApi::class) constructor(
    val id: Uuid,
    val userId: Uuid,
    val expiresAt: Instant,
    val isActive: Boolean,
)