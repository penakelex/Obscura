package org.penakelex.obscura.domain.usecase.auth

import co.touchlab.kermit.Logger
import org.penakelex.obscura.contract.rest.common.auth.KeysetData
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

class RegisterUseCase(
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
        val errors = buildList {
            addAll(InputValidator.validateEmail(email))
            addAll(InputValidator.validatePassword(password))
            if (deviceInfo != null) {
                addAll(InputValidator.validateDeviceInfo(deviceInfo))
            }
        }
        if (errors.isNotEmpty()) {
            throw ValidationException(errors)
        }

        val normalizedEmail = email.trim().lowercase()

        val guestNotes = migrateGuestNotesUseCase.decryptGuestNotes()
        if (guestNotes.isNotEmpty()) {
            logger.d { "Found ${guestNotes.size} guest note(s) to migrate" }
        }

        val salt = keyDeriver.generateSalt()
        val derived = keyDeriver.deriveKey(password, salt).toDerivedKeys()

        val encryptedKeysetJson = try {
            cryptoProvider.initialize(derived.masterKey, null)
        } catch (_: CryptoException) {
            throw AuthException.KeysetDecryptionFailed()
        }

        val keysetData = KeysetData(
            salt = Base64.encode(salt),
            encryptedKeyset = encryptedKeysetJson,
        )

        try {
            authRepository.register(
                email = normalizedEmail,
                authHash = Base64.encode(derived.authKey),
                deviceInfo = deviceInfo?.takeIf { it.isNotBlank() },
                keyset = keysetData,
            )
        } catch (e: Exception) {
            cryptoProvider.reset()
            throw e
        }

        accountKeyStorage.saveMasterKey(derived.masterKey)

        if (guestNotes.isNotEmpty()) {
            migrateGuestNotesUseCase.reEncryptAndSave(guestNotes)
        }

        logger.i { "User registered: $normalizedEmail" }
    }

    private companion object {
        const val LOG_TAG = "RegisterUseCase"
    }
}