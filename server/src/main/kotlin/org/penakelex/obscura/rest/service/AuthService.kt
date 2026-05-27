package org.penakelex.obscura.rest.service

import org.penakelex.obscura.config.ServerConfig
import org.penakelex.obscura.contract.rest.requests.auth.LoginRequest
import org.penakelex.obscura.contract.rest.requests.auth.RegisterRequest
import org.penakelex.obscura.contract.rest.responses.auth.LoginResponse
import org.penakelex.obscura.contract.rest.responses.auth.ProfileResponse
import org.penakelex.obscura.contract.rest.responses.common.SuccessResponse
import org.penakelex.obscura.db.repository.SessionRepository
import org.penakelex.obscura.db.repository.UserRepository
import org.penakelex.obscura.exception.auth.AuthException
import org.penakelex.obscura.exception.resource.NotFoundException
import org.penakelex.obscura.exception.validation.ValidationException
import org.penakelex.obscura.contract.ErrorCodes
import org.penakelex.obscura.contract.rest.requests.account.ChangeEmailRequest
import org.penakelex.obscura.contract.rest.requests.account.ChangePasswordRequest
import org.penakelex.obscura.contract.rest.responses.common.FieldError
import org.penakelex.obscura.exception.account.AccountException
import org.penakelex.obscura.security.PasswordHasher
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class AuthService(
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository
) {
    suspend fun register(request: RegisterRequest): SuccessResponse {
        validateRegisterInput(request.email, request.password)

        val hash = PasswordHasher.hash(request.password)
        val userId = userRepository.create(request.email, hash)
            ?: throw AuthException.EmailAlreadyRegistered(request.email)

        return SuccessResponse("User registered with id $userId")
    }

    suspend fun login(request: LoginRequest): LoginResponse {
        validateLoginInput(
            request.email,
            request.password,
            request.deviceInfo
        )

        val user = userRepository.findByEmail(request.email)
            ?: throw AuthException.InvalidCredentials()

        if (!PasswordHasher.verify(
                request.password,
                user.passwordHash
            )
        ) {
            throw AuthException.InvalidCredentials()
        }

        val sessionResult =
            sessionRepository.create(user.id, request.deviceInfo)
        return LoginResponse(
            token = sessionResult.rawToken,
            expiresAt = sessionResult.expiresAt.toString(),
            userId = user.id.toString()
        )
    }

    suspend fun logout(sessionId: Uuid): SuccessResponse {
        sessionRepository.revoke(sessionId)
        return SuccessResponse("Session revoked")
    }

    suspend fun logoutAllSessions(userId: Uuid): Int {
        return sessionRepository.revokeAllByUserId(userId)
    }

    suspend fun getProfile(userId: Uuid): ProfileResponse {
        val user = userRepository.findById(userId)
            ?: throw NotFoundException.UserNotFound(userId.toString())
        return ProfileResponse(
            userId = userId.toString(),
            email = user.email
        )
    }

    suspend fun changePassword(
        userId: Uuid,
        request: ChangePasswordRequest
    ): SuccessResponse {
        validatePassword(request.newPassword)

        val user = userRepository.findById(userId)
            ?: throw NotFoundException.UserNotFound(userId.toString())

        if (!PasswordHasher.verify(
                request.currentPassword,
                user.passwordHash
            )
        ) {
            throw AccountException.InvalidCurrentPassword()
        }

        if (request.newPassword == request.currentPassword) {
            throw AccountException.PasswordSameAsCurrent()
        }

        val newHash = PasswordHasher.hash(request.newPassword)
        userRepository.updatePassword(userId, newHash)

        sessionRepository.revokeAllByUserId(userId)

        return SuccessResponse("Password changed successfully")
    }

    suspend fun changeEmail(
        userId: Uuid,
        request: ChangeEmailRequest
    ): SuccessResponse {
        validateEmail(request.newEmail)

        val user = userRepository.findById(userId)
            ?: throw NotFoundException.UserNotFound(userId.toString())

        if (!PasswordHasher.verify(
                request.currentPassword,
                user.passwordHash
            )
        ) {
            throw AccountException.InvalidCurrentPassword()
        }

        if (request.newEmail.equals(user.email, ignoreCase = true)) {
            return SuccessResponse("Email unchanged")
        }

        val existingUser =
            userRepository.findByEmail(request.newEmail)
        if (existingUser != null) {
            throw AccountException.NewEmailAlreadyTaken(request.newEmail)
        }

        userRepository.updateEmail(userId, request.newEmail)

        return SuccessResponse("Email changed successfully")
    }

    private fun validateRegisterInput(
        email: String,
        password: String
    ) {
        val errors = mutableListOf<FieldError>()
        val validation = ServerConfig.validation

        when {
            email.isBlank() -> errors += FieldError(
                "email",
                ErrorCodes.Validation.EMAIL_BLANK,
                "Email is required"
            )

            email.length > validation.emailMaxLength ->
                errors += FieldError(
                    "email",
                    ErrorCodes.Validation.EMAIL_TOO_LONG,
                    "Max ${validation.emailMaxLength} characters"
                )

            !EMAIL_REGEX.matches(email) -> errors += FieldError(
                "email",
                ErrorCodes.Validation.INVALID_EMAIL_FORMAT,
                "Invalid format"
            )
        }

        when {
            password.length < validation.passwordMinLength ->
                errors += FieldError(
                    "password",
                    ErrorCodes.Validation.PASSWORD_TOO_SHORT,
                    "Min ${validation.passwordMinLength} characters"
                )

            password.length > validation.passwordMaxLength ->
                errors += FieldError(
                    "password",
                    ErrorCodes.Validation.PASSWORD_TOO_LONG,
                    "Max ${validation.passwordMaxLength} characters"
                )
        }

        if (errors.isNotEmpty()) {
            throw ValidationException.MultipleFields(errors)
        }
    }

    private fun validateLoginInput(
        email: String,
        password: String,
        deviceInfo: String?
    ) {
        val errors = mutableListOf<FieldError>()
        val validation = ServerConfig.validation

        if (email.isBlank()) {
            errors += FieldError(
                "email",
                ErrorCodes.Validation.EMAIL_BLANK,
                "Email is required"
            )
        } else if (!EMAIL_REGEX.matches(email)) {
            errors += FieldError(
                "email",
                ErrorCodes.Validation.INVALID_EMAIL_FORMAT,
                "Invalid format"
            )
        }

        if (password.length < validation.passwordMinLength) {
            errors += FieldError(
                "password",
                ErrorCodes.Validation.PASSWORD_TOO_SHORT,
                "Min ${validation.passwordMinLength} characters"
            )
        }

        if (deviceInfo != null && deviceInfo.length >
            validation.deviceInfoMaxLength
        ) {
            errors += FieldError(
                "deviceInfo",
                ErrorCodes.Validation.DEVICE_INFO_TOO_LONG,
                "Max ${validation.deviceInfoMaxLength} characters"
            )
        }

        if (errors.isNotEmpty()) {
            throw ValidationException.MultipleFields(errors)
        }
    }

    private fun validateEmail(email: String) {
        val validation = ServerConfig.validation

        when {
            email.isBlank() -> throw ValidationException.EmailBlank()
            email.length > validation.emailMaxLength ->
                throw ValidationException.EmailTooLong(
                    validation.emailMaxLength
                )

            !EMAIL_REGEX.matches(email) ->
                throw ValidationException.InvalidEmailFormat(email)
        }
    }

    private fun validatePassword(password: String) {
        val validation = ServerConfig.validation

        when {
            password.length < validation.passwordMinLength ->
                throw ValidationException.PasswordTooShort(
                    validation.passwordMinLength
                )

            password.length > validation.passwordMaxLength ->
                throw ValidationException.PasswordTooLong(
                    validation.passwordMaxLength
                )
        }
    }

    companion object {
        private val EMAIL_REGEX = Regex(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )
    }
}