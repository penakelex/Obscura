package org.penakelex.obscura.domain.usecase.auth.authentication

import co.touchlab.kermit.Logger
import org.penakelex.obscura.data.crypto.CryptoException
import org.penakelex.obscura.data.crypto.CryptoProvider
import org.penakelex.obscura.data.crypto.KeyDeriver
import org.penakelex.obscura.data.crypto.toDerivedKeys
import org.penakelex.obscura.data.storage.AccountKeyStorage
import org.penakelex.obscura.domain.exception.AuthException
import org.penakelex.obscura.domain.exception.ValidationException
import org.penakelex.obscura.domain.repository.AuthRepository
import org.penakelex.obscura.domain.validation.InputValidator
import kotlin.io.encoding.Base64

class LoginUseCase(
    private val authRepository: AuthRepository,
    private val cryptoProvider: CryptoProvider,
    private val keyDeriver: KeyDeriver,
    private val migrateGuestNotesUseCase: MigrateGuestNotesUseCase,
    private val accountKeyStorage: AccountKeyStorage,
) {
    private val logger = Logger.withTag(LOG_TAG)

    suspend operator fun invoke(
        email: String,
        password: String,
        deviceInfo: String? = null,
    ) {
        val errors = InputValidator.validateEmail(email) +
                InputValidator.validatePassword(password)
        if (errors.isNotEmpty()) {
            throw ValidationException(errors)
        }

        val normalizedEmail = email.trim().lowercase()

        val guestNotes = migrateGuestNotesUseCase.decryptGuestNotes()
        if (guestNotes.isNotEmpty()) {
            logger.d { "Found ${guestNotes.size} guest note(s) to migrate" }
        }

        val saltBase64 = try {
            authRepository.getChallenge(normalizedEmail)
        } catch (e: Exception) {
            throw e
        }
        val salt = Base64.decode(saltBase64)

        val derived = keyDeriver.deriveKey(password, salt).toDerivedKeys()

        val keyset = try {
            authRepository.login(
                email = normalizedEmail,
                authHash = Base64.encode(derived.authKey),
                deviceInfo = deviceInfo,
            )
        } catch (e: Exception) {
            throw e
        }

        try {
            cryptoProvider.initialize(
                derived.masterKey,
                keyset.encryptedKeyset
            )
        } catch (_: CryptoException.KeysetDecryptionFailed) {
            authRepository.logout()
            throw AuthException.KeysetDecryptionFailed()
        } catch (_: CryptoException) {
            authRepository.logout()
            throw AuthException.KeysetDecryptionFailed()
        }

        accountKeyStorage.saveMasterKey(derived.masterKey)

        if (guestNotes.isNotEmpty()) {
            migrateGuestNotesUseCase.reEncryptAndSave(guestNotes)
        }

        logger.i { "User logged in: $normalizedEmail" }
    }

    private companion object {
        const val LOG_TAG = "LoginUseCase"
    }
}