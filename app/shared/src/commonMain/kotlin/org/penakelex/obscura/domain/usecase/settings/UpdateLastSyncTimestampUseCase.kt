package org.penakelex.obscura.domain.usecase.settings

import org.penakelex.obscura.domain.exception.ValidationException
import org.penakelex.obscura.domain.gateway.SettingsGateway
import org.penakelex.obscura.domain.validation.ValidationError

class UpdateLastSyncTimestampUseCase(
    private val settingsGateway: SettingsGateway,
) {
    suspend operator fun invoke(timestamp: Long) {
        if (timestamp < 0L) {
            throw ValidationException(ValidationError.InvalidTimestamp())
        }

        settingsGateway.setLastSyncTimestamp(timestamp)
    }
}