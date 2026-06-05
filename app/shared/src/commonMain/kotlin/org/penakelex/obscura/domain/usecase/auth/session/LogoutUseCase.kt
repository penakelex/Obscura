package org.penakelex.obscura.domain.usecase.auth.session

import co.touchlab.kermit.Logger
import org.penakelex.obscura.data.crypto.GuestCryptoManager
import org.penakelex.obscura.data.storage.AccountKeyStorage
import org.penakelex.obscura.domain.repository.AuthRepository
import org.penakelex.obscura.domain.repository.NoteRepository

class LogoutUseCase(
    private val authRepository: AuthRepository,
    private val guestCryptoManager: GuestCryptoManager,
    private val accountKeyStorage: AccountKeyStorage,
    private val noteRepository: NoteRepository,
) {
    private val logger = Logger.withTag(LOG_TAG)

    suspend operator fun invoke() {
        authRepository.logout()
        accountKeyStorage.clear()
        noteRepository.clearAll()
        guestCryptoManager.initializeGuestMode()
        logger.i {
            "Logged out, local notes cleared, guest mode re-initialized"
        }
    }

    private companion object {
        const val LOG_TAG = "LogoutUseCase"
    }
}