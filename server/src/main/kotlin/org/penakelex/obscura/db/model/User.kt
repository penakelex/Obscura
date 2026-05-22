package org.penakelex.obscura.db.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class User @OptIn(ExperimentalUuidApi::class) constructor(
    val id: Uuid,
    val email: String,
    val passwordHash: String
)