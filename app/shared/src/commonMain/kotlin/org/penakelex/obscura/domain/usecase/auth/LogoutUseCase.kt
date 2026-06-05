package org.penakelex.obscura.domain.usecase.auth

import co.touchlab.kermit.Logger
import org.penakelex.obscura.data.crypto.GuestCryptoManager
import org.penakelex.obscura.data.storage.AccountKeyStorage
import org.penakelex.obscura.domain.repository.AuthRepository

class LogoutUseCase(
    private val authRepository: AuthRepository,
    private val guestCryptoManager: GuestCryptoManager,
    private val accountKeyStorage: AccountKeyStorage,
) {
    private val logger = Logger.withTag(LOG_TAG)

    suspend operator fun invoke() {
        authRepository.logout()
        accountKeyStorage.clear()
        guestCryptoManager.initializeGuestMode()
        logger.i { "Logged out, guest mode re-initialized" }
    }

    private companion object {
        const val LOG_TAG = "LogoutUseCase"
    }
}