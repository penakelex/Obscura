package org.penakelex.obscura.domain.usecase.auth.session

import co.touchlab.kermit.Logger
import org.penakelex.obscura.data.crypto.GuestCryptoManager
import org.penakelex.obscura.data.storage.AccountKeyStorage
import org.penakelex.obscura.domain.repository.AuthRepository
import org.penakelex.obscura.domain.repository.NoteRepository

class LogoutAllUseCase(
    private val authRepository: AuthRepository,
    private val guestCryptoManager: GuestCryptoManager,
    private val accountKeyStorage: AccountKeyStorage,
    private val noteRepository: NoteRepository,
) {
    private val logger = Logger.withTag(LOG_TAG)

    suspend operator fun invoke(): Int {
        val revokedCount = authRepository.logoutAll()
        accountKeyStorage.clear()
        noteRepository.clearAll()
        guestCryptoManager.initializeGuestMode()
        logger.i {
            "Logged out from all devices ($revokedCount revoked), " +
                    "local notes cleared, guest mode re-initialized"
        }
        return revokedCount
    }

    private companion object {
        const val LOG_TAG = "LogoutAllUseCase"
    }
}