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
import org.penakelex.obscura.contract.rest.requests.account.DeleteAccountRequest
import org.penakelex.obscura.contract.rest.responses.auth.SessionInfo
import org.penakelex.obscura.contract.rest.responses.auth.SessionsListResponse
import org.penakelex.obscura.contract.rest.responses.common.FieldError
import org.penakelex.obscura.db.repository.NoteRepository
import org.penakelex.obscura.exception.account.AccountException
import org.penakelex.obscura.security.PasswordHasher
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class AuthService(
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val noteRepository: NoteRepository,
    private val passwordHasher: PasswordHasher,
    private val validationConfig: ServerConfig.Validation
) {
    suspend fun register(request: RegisterRequest): SuccessResponse {
        validateRegisterInput(request.email, request.password)

        val hash = passwordHasher.hash(request.password)
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

        if (!passwordHasher.verify(
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
            expiresAt = sessionResult.expiresAt.toEpochMilliseconds(),
            userId = user.id.toString()
        )
    }

    suspend fun listSessions(
        userId: Uuid,
        currentSessionId: Uuid
    ): SessionsListResponse {
        val sessions = sessionRepository.findAllActiveByUserId(userId)
            .filter { it.expiresAt > Clock.System.now() }
            .map { session ->
                SessionInfo(
                    id = session.id.toString(),
                    deviceInfo = session.deviceInfo,
                    createdAt = session.createdAt.toEpochMilliseconds(),
                    expiresAt = session.expiresAt.toEpochMilliseconds(),
                    isCurrent = session.id == currentSessionId
                )
            }
            .sortedByDescending { it.createdAt }

        return SessionsListResponse(
            sessions = sessions,
            totalCount = sessions.size
        )
    }

    suspend fun revokeSessionById(
        userId: Uuid,
        sessionId: Uuid,
        currentSessionId: Uuid
    ): SuccessResponse {
        if (sessionId == currentSessionId) {
            throw ValidationException.CannotRevokeCurrentSession()
        }

        val session = sessionRepository
            .findActiveByIdAndUser(sessionId, userId)
            ?: throw NotFoundException.SessionNotFound(sessionId.toString())

        sessionRepository.revoke(session.id)
        return SuccessResponse("Session revoked")
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

        if (!passwordHasher.verify(
                request.currentPassword,
                user.passwordHash
            )
        ) {
            throw AccountException.InvalidCurrentPassword()
        }

        if (request.newPassword == request.currentPassword) {
            throw AccountException.PasswordSameAsCurrent()
        }

        val newHash = passwordHasher.hash(request.newPassword)
        userRepository.updatePassword(userId, newHash)

        sessionRepository.revokeAllByUserId(userId)

        return SuccessResponse("Password changed successfully")
    }

    suspend fun deleteAccount(
        userId: Uuid,
        request: DeleteAccountRequest
    ): SuccessResponse {
        val user = userRepository.findById(userId)
            ?: throw NotFoundException.UserNotFound(userId.toString())

        if (!passwordHasher.verify(
                request.currentPassword,
                user.passwordHash
            )
        ) {
            throw AccountException.InvalidCurrentPassword()
        }

        sessionRepository.deleteAllByUserId(userId)
        noteRepository.deleteAllByUserId(userId)
        userRepository.delete(userId)

        return SuccessResponse("Account and all associated data deleted")
    }

    suspend fun changeEmail(
        userId: Uuid,
        request: ChangeEmailRequest
    ): SuccessResponse {
        validateEmail(request.newEmail)

        val user = userRepository.findById(userId)
            ?: throw NotFoundException.UserNotFound(userId.toString())

        if (!passwordHasher.verify(
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

        when {
            email.isBlank() -> errors += FieldError(
                "email",
                ErrorCodes.Validation.EMAIL_BLANK,
                "Email is required"
            )

            email.length > validationConfig.emailMaxLength ->
                errors += FieldError(
                    "email",
                    ErrorCodes.Validation.EMAIL_TOO_LONG,
                    "Max ${validationConfig.emailMaxLength} characters"
                )

            !EMAIL_REGEX.matches(email) -> errors += FieldError(
                "email",
                ErrorCodes.Validation.INVALID_EMAIL_FORMAT,
                "Invalid format"
            )
        }

        when {
            password.length < validationConfig.passwordMinLength ->
                errors += FieldError(
                    "password",
                    ErrorCodes.Validation.PASSWORD_TOO_SHORT,
                    "Min ${validationConfig.passwordMinLength} characters"
                )

            password.length > validationConfig.passwordMaxLength ->
                errors += FieldError(
                    "password",
                    ErrorCodes.Validation.PASSWORD_TOO_LONG,
                    "Max ${validationConfig.passwordMaxLength} characters"
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

        if (password.length < validationConfig.passwordMinLength) {
            errors += FieldError(
                "password",
                ErrorCodes.Validation.PASSWORD_TOO_SHORT,
                "Min ${validationConfig.passwordMinLength} characters"
            )
        }

        if (deviceInfo != null && deviceInfo.length >
            validationConfig.deviceInfoMaxLength
        ) {
            errors += FieldError(
                "deviceInfo",
                ErrorCodes.Validation.DEVICE_INFO_TOO_LONG,
                "Max ${validationConfig.deviceInfoMaxLength} characters"
            )
        }

        if (errors.isNotEmpty()) {
            throw ValidationException.MultipleFields(errors)
        }
    }

    private fun validateEmail(email: String) {
        when {
            email.isBlank() -> throw ValidationException.EmailBlank()
            email.length > validationConfig.emailMaxLength ->
                throw ValidationException.EmailTooLong(
                    validationConfig.emailMaxLength
                )

            !EMAIL_REGEX.matches(email) ->
                throw ValidationException.InvalidEmailFormat(email)
        }
    }

    private fun validatePassword(password: String) {
        when {
            password.length < validationConfig.passwordMinLength ->
                throw ValidationException.PasswordTooShort(
                    validationConfig.passwordMinLength
                )

            password.length > validationConfig.passwordMaxLength ->
                throw ValidationException.PasswordTooLong(
                    validationConfig.passwordMaxLength
                )
        }
    }

    companion object {
        private val EMAIL_REGEX = Regex(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )
    }
}