package org.penakelex.obscura.domain.usecase.auth.session

import co.touchlab.kermit.Logger
import org.penakelex.obscura.data.crypto.CryptoException
import org.penakelex.obscura.data.crypto.CryptoProvider
import org.penakelex.obscura.data.storage.AccountKeyStorage
import org.penakelex.obscura.data.storage.TokenStorage

class AccountBootstrapUseCase(
    private val tokenStorage: TokenStorage,
    private val accountKeyStorage: AccountKeyStorage,
    private val cryptoProvider: CryptoProvider,
) {
    private val logger = Logger.withTag(LOG_TAG)

    suspend operator fun invoke(): Boolean {
        if (cryptoProvider.isInitialized) {
            logger.d {
                "CryptoProvider already initialized, skipping bootstrap"
            }
            return true
        }

        val session = tokenStorage.sessionFlow.value

        if (session == null) {
            logger.d { "No active session, bootstrap skipped" }
            return false
        }

        val encryptedKeyset = session.encryptedKeyset

        if (encryptedKeyset == null) {
            logger.w { "Session exists but keyset is missing" }
            return false
        }

        val masterKey = accountKeyStorage.loadMasterKey()

        if (masterKey == null) {
            logger.w { "Session exists but master key is missing — " +
                    "password entry required" }
            return false
        }

        return try {
            cryptoProvider.initialize(masterKey, encryptedKeyset)
            logger.i {
                "CryptoProvider restored from cached master key"
            }
            true
        } catch (e: CryptoException.KeysetDecryptionFailed) {
            logger.e(e) {
                "Cached master key cannot decrypt keyset — clearing cache"
            }
            accountKeyStorage.clear()
            false
        } catch (e: CryptoException) {
            logger.e(e) { "Failed to restore CryptoProvider" }
            false
        }
    }

    private companion object {
        const val LOG_TAG = "AccountBootstrap"
    }
}