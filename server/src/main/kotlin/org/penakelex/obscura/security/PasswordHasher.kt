package org.penakelex.obscura.security

import com.password4j.*
import com.password4j.types.Argon2
import com.password4j.types.Hmac
import org.penakelex.obscura.config.ServerConfig
import org.slf4j.LoggerFactory

object PasswordHasher {
    private val logger =
        LoggerFactory.getLogger(PasswordHasher::class.java)

    private val hashingFunction: HashingFunction by lazy {
        val algorithm =
            ServerConfig.security.password.algorithm.uppercase()
                .trim()
        val parameters = ServerConfig.security.password.hashParameters

        logger.info(
            "Initializing PasswordHasher with algorithm: {}",
            algorithm
        )

        when (algorithm) {
            "ARGON2ID", "ARGON2_ID" -> Argon2Function.getInstance(
                parameters.memory,
                parameters.iterations,
                parameters.parallelism,
                parameters.outputLength,
                Argon2.ID
            )

            "ARGON2I", "ARGON2_I" -> Argon2Function.getInstance(
                parameters.memory,
                parameters.iterations,
                parameters.parallelism,
                parameters.outputLength,
                Argon2.I
            )

            "ARGON2D", "ARGON2_D" -> Argon2Function.getInstance(
                parameters.memory,
                parameters.iterations,
                parameters.parallelism,
                parameters.outputLength,
                Argon2.D
            )

            "BCRYPT" -> BcryptFunction.getInstance(parameters.logRounds)

            "SCRYPT" -> ScryptFunction.getInstance(
                parameters.workFactor,
                parameters.resources,
                parameters.parallelism,
                parameters.outputLength
            )

            "PBKDF2" -> PBKDF2Function.getInstance(
                Hmac.valueOf(parameters.hmacAlgorithm.uppercase().trim()),
                parameters.iterations,
                parameters.outputLength
            )

            else -> throw IllegalArgumentException(
                "Unsupported password hashing algorithm: $algorithm"
            )
        }
    }

    fun hash(password: String): String =
        Password.hash(password).with(hashingFunction).result

    fun verify(password: String, hash: String): Boolean =
        Password.check(password, hash).with(hashingFunction)
}