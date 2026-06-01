package org.penakelex.obscura.data.crypto

import android.content.Context
import co.touchlab.kermit.Logger
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplate
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.crypto.tink.integration.android.AndroidKeystoreKmsClient
import org.penakelex.obscura.domain.model.common.CipherType

actual class CryptoProvider(context: Context) {
    private val logger = Logger.withTag(CRYPTO_LOG_TAG)
    private val aeads: Map<CipherType, Aead>

    init {
        AeadConfig.register()

        AndroidKeystoreKmsClient.getOrGenerateNewAeadKey(
            MASTER_KEY_URI
        )
        logger.d { "Master key ensured in Android Keystore" }

        aeads = CipherType.entries.associateWith { cipher ->
            val keysetHandle = AndroidKeysetManager.Builder()
                .withSharedPref(
                    context,
                    keysetName(cipher),
                    PREF_FILE_NAME
                )
                .withKeyTemplate(keyTemplate(cipher))
                .withMasterKeyUri(MASTER_KEY_URI)
                .build()
                .keysetHandle

            keysetHandle.getPrimitive(
                RegistryConfiguration.get(),
                Aead::class.java
            )
        }
        logger.i {
            "CryptoProvider initialized with ${aeads.size} ciphers"
        }
    }

    actual fun encrypt(
        plaintext: ByteArray,
        cipherType: CipherType
    ): ByteArray = try {
        aeadFor(cipherType).encrypt(plaintext, null)
    } catch (e: Exception) {
        logger.e(e) { "Encryption failed for cipher=$cipherType" }
        throw CryptoException("Encryption failed: ${e.message}", e)
    }

    actual fun decrypt(
        ciphertext: ByteArray,
        cipherType: CipherType
    ): ByteArray = try {
        aeadFor(cipherType).decrypt(ciphertext, null)
    } catch (e: Exception) {
        logger.e(e) { "Decryption failed for cipher=$cipherType" }
        throw CryptoException("Decryption failed: ${e.message}", e)
    }

    private fun aeadFor(cipherType: CipherType): Aead =
        aeads[cipherType]
            ?: throw CryptoException("Unsupported cipher: $cipherType")

    private fun keysetName(cipher: CipherType): String =
        "${KEYSET_PREFIX}_${cipher.name.lowercase()}"

    private fun keyTemplate(cipher: CipherType): KeyTemplate =
        when (cipher) {
            CipherType.AES_GCM -> KeyTemplates.get("AES256_GCM")
            CipherType.XCHACHA20_POLY1305 ->
                KeyTemplates.get("XCHACHA20_POLY1305")
        }

    private companion object {
        const val PREF_FILE_NAME = "obscura_keysets"
        const val KEYSET_PREFIX = "obscura_keyset"
        const val MASTER_KEY_URI =
            "android-keystore://obscura_master_key"
    }
}