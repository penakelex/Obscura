package org.penakelex.obscura.domain.usecase.auth

import org.penakelex.obscura.data.crypto.KeyDeriver
import org.penakelex.obscura.data.crypto.toDerivedKeys
import org.penakelex.obscura.domain.exception.ValidationException
import org.penakelex.obscura.domain.repository.AuthRepository
import org.penakelex.obscura.domain.validation.InputValidator
import org.penakelex.obscura.domain.validation.ValidationError
import kotlin.io.encoding.Base64

class ChangeEmailUseCase(
    private val authRepository: AuthRepository,
    private val keyDeriver: KeyDeriver,
) {
    suspend operator fun invoke(
        currentPassword: String,
        newEmail: String,
    ) {
        val errors = buildList {
            if (currentPassword.isBlank()) {
                add(ValidationError.CurrentPasswordBlank())
            }
            addAll(InputValidator.validateEmail(newEmail))
        }

        if (errors.isNotEmpty()) {
            throw ValidationException(errors)
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

        authRepository.changeEmail(
            currentAuthHash = currentAuthHash,
            newEmail = newEmail.trim().lowercase(),
        )
    }
}