package org.penakelex.obscura.data.crypto

import co.touchlab.kermit.Logger
import org.penakelex.obscura.data.storage.GuestKeyStorage
import java.security.SecureRandom

class GuestCryptoManager(
    private val cryptoProvider: CryptoProvider,
    private val guestKeyStorage: GuestKeyStorage,
) {
    private val logger = Logger.withTag("GuestCryptoManager")

    suspend fun initializeGuestMode() {
        if (cryptoProvider.isInitialized) {
            logger.d { "CryptoProvider already initialized, skipping guest init" }
            return
        }

        val masterKey = guestKeyStorage.loadGuestMasterKey() ?: run {
            val newKey = generateGuestMasterKey()
            guestKeyStorage.saveGuestMasterKey(newKey)
            newKey
        }

        val existingKeyset = guestKeyStorage.loadGuestKeyset()

        val encryptedKeyset = cryptoProvider.initialize(
            masterKey = masterKey,
            encryptedKeysetJson = existingKeyset
        )

        if (existingKeyset == null) {
            guestKeyStorage.saveGuestKeyset(encryptedKeyset)
            logger.i { "Guest mode initialized with new keyset and persisted master key" }
        } else {
            logger.i { "Guest mode restored from saved keyset and master key" }
        }
    }

    suspend fun clearGuestMode() {
        guestKeyStorage.clearGuestData()
        logger.i { "Guest mode data cleared (files removed, provider untouched)" }
    }

    private fun generateGuestMasterKey(): ByteArray =
        ByteArray(GUEST_KEY_LENGTH).also { SecureRandom().nextBytes(it) }

    private companion object {
        const val GUEST_KEY_LENGTH = 32
    }
}