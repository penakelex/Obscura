package org.penakelex.obscura.domain.usecase.auth.account

import co.touchlab.kermit.Logger
import org.penakelex.obscura.contract.rest.common.auth.KeysetData
import org.penakelex.obscura.data.crypto.CryptoProvider
import org.penakelex.obscura.data.crypto.KeyDeriver
import org.penakelex.obscura.data.storage.AccountKeyStorage
import org.penakelex.obscura.domain.exception.ValidationException
import org.penakelex.obscura.domain.repository.AuthRepository
import org.penakelex.obscura.domain.validation.InputValidator
import org.penakelex.obscura.domain.validation.ValidationError
import kotlin.io.encoding.Base64

class ChangePasswordUseCase(
    private val authRepository: AuthRepository,
    private val cryptoProvider: CryptoProvider,
    private val keyDeriver: KeyDeriver,
    private val accountKeyStorage: AccountKeyStorage,
) {
    private val logger = Logger.withTag(LOG_TAG)

    suspend operator fun invoke(
        currentPassword: String,
        newPassword: String
    ) {
        val errors = buildList {
            if (currentPassword.isBlank()) {
                add(ValidationError.CurrentPasswordBlank())
            }
            addAll(InputValidator.validatePassword(newPassword))
            if (currentPassword.isNotBlank() &&
                newPassword.isNotBlank() &&
                currentPassword == newPassword
            ) {
                add(ValidationError.PasswordsMatch())
            }
        }
        if (errors.isNotEmpty()) {
            throw ValidationException(errors)
        }

        val currentKeyset = authRepository.getCurrentKeyset()
            ?: throw IllegalStateException("No keyset available")

        val salt = Base64.decode(currentKeyset.salt)

        val currentDerived = keyDeriver.deriveKey(currentPassword, salt)
        val currentAuthKey = currentDerived.copyOfRange(0, 32)
        val currentMasterKey = currentDerived.copyOfRange(32, 64)

        val newDerived = keyDeriver.deriveKey(newPassword, salt)
        val newAuthKey = newDerived.copyOfRange(0, 32)
        val newMasterKey = newDerived.copyOfRange(32, 64)

        val newEncryptedKeyset = cryptoProvider.reEncryptKeyset(
            currentEncryptedKeyset = currentKeyset.encryptedKeyset,
            currentMasterKey = currentMasterKey,
            newMasterKey = newMasterKey
        )

        val newKeysetData = KeysetData(
            salt = currentKeyset.salt,
            encryptedKeyset = newEncryptedKeyset
        )

        authRepository.changePassword(
            currentAuthHash = Base64.encode(currentAuthKey),
            newAuthHash = Base64.encode(newAuthKey),
            newKeyset = newKeysetData
        )

        accountKeyStorage.saveMasterKey(newMasterKey)
        logger.i { "Password changed successfully" }
    }

    private companion object {
        const val LOG_TAG = "ChangePasswordUseCase"
    }
}