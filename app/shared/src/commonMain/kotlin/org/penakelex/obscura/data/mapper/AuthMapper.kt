package org.penakelex.obscura.data.mapper

import org.penakelex.obscura.contract.rest.responses.auth.SessionResponse
import org.penakelex.obscura.contract.rest.responses.auth.ProfileResponse
import org.penakelex.obscura.contract.rest.responses.auth.SessionInfo as RestSessionInfo
import org.penakelex.obscura.domain.model.auth.SessionData
import org.penakelex.obscura.domain.model.auth.SessionInfo
import org.penakelex.obscura.domain.model.auth.UserProfile

object AuthMapper {
    fun SessionResponse.toSessionData(): SessionData = SessionData(
        token = token,
        userId = userId,
        expiresAt = expiresAt,
        encryptedKeyset = keyset?.encryptedKeyset,
        salt = keyset?.salt,
    )

    fun ProfileResponse.toDomain(): UserProfile = UserProfile(
        userId = userId,
        email = email,
    )

    fun RestSessionInfo.toDomain(): SessionInfo = SessionInfo(
        id = id,
        deviceInfo = deviceInfo,
        createdAt = createdAt,
        expiresAt = expiresAt,
        isCurrent = isCurrent,
    )

    fun List<RestSessionInfo>.toDomainList(): List<SessionInfo> =
        map { it.toDomain() }
}