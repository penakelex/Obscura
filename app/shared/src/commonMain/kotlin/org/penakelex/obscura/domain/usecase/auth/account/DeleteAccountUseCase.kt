package org.penakelex.obscura.domain.usecase.auth.account

import org.penakelex.obscura.data.crypto.KeyDeriver
import org.penakelex.obscura.data.crypto.toDerivedKeys
import org.penakelex.obscura.data.storage.AccountKeyStorage
import org.penakelex.obscura.domain.exception.ValidationException
import org.penakelex.obscura.domain.repository.AuthRepository
import org.penakelex.obscura.domain.validation.ValidationError
import kotlin.io.encoding.Base64

class DeleteAccountUseCase(
    private val authRepository: AuthRepository,
    private val keyDeriver: KeyDeriver,
    private val accountKeyStorage: AccountKeyStorage,
) {
    suspend operator fun invoke(currentPassword: String) {
        if (currentPassword.isBlank()) {
            throw ValidationException(
                ValidationError.CurrentPasswordBlank()
            )
        }

        val keyset = authRepository.getCurrentKeyset()
            ?: throw IllegalStateException(
                "No keyset available — user must be logged in"
            )

        val salt = Base64.decode(keyset.salt)
        val derived = keyDeriver
            .deriveKey(currentPassword, salt)
            .toDerivedKeys()
        val currentAuthHash = Base64.encode(derived.authKey)

        authRepository.deleteAccount(currentAuthHash)
        accountKeyStorage.clear()
    }
}