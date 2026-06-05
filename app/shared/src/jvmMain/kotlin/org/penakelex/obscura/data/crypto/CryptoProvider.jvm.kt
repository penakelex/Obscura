package org.penakelex.obscura.data.crypto

import co.touchlab.kermit.Logger
import com.google.crypto.tink.Aead
import com.google.crypto.tink.InsecureSecretKeyAccess
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.TinkJsonProtoKeysetFormat
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.PredefinedAeadParameters
import org.penakelex.obscura.domain.model.common.CipherType
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

actual class CryptoProvider {
    private val logger = Logger.withTag(CRYPTO_LOG_TAG)
    private var aeads: Map<CipherType, Aead> = emptyMap()

    actual val isInitialized: Boolean
        get() = aeads.isNotEmpty()

    init {
        AeadConfig.register()
    }

    actual fun initialize(
        masterKey: ByteArray,
        encryptedKeysetJson: String?
    ): String {
        val keysetJson = if (encryptedKeysetJson != null) {
            decryptKeysetJson(encryptedKeysetJson, masterKey)
        } else {
            generateNewKeysetsJson()
        }

        loadAeadsFromJson(keysetJson)

        return encryptedKeysetJson
            ?: encryptKeysetJson(keysetJson, masterKey)
    }

    actual fun encrypt(
        plaintext: ByteArray,
        cipherType: CipherType
    ): ByteArray = try {
        aeadFor(cipherType).encrypt(plaintext, null)
    } catch (e: Exception) {
        throw CryptoException.EncryptionFailed(e)
    }

    actual fun decrypt(
        ciphertext: ByteArray,
        cipherType: CipherType
    ): ByteArray = try {
        aeadFor(cipherType).decrypt(ciphertext, null)
    } catch (e: Exception) {
        throw CryptoException.DecryptionFailed(e)
    }

    actual fun reEncryptKeyset(
        currentEncryptedKeyset: String,
        currentMasterKey: ByteArray,
        newMasterKey: ByteArray
    ): String {
        val keysetJson = decryptKeysetJson(currentEncryptedKeyset, currentMasterKey)
        return encryptKeysetJson(keysetJson, newMasterKey)
    }

    actual fun reset() {
        aeads = emptyMap()
        logger.i { "CryptoProvider reset" }
    }

    private fun aeadFor(cipherType: CipherType): Aead =
        aeads[cipherType] ?: throw CryptoException.NotInitialized()

    private fun generateNewKeysetsJson(): String {
        logger.i { "Generating new Tink keysets" }
        val handle = KeysetHandle.generateNew(
            PredefinedAeadParameters.AES256_GCM
        )
        return TinkJsonProtoKeysetFormat.serializeKeyset(
            handle,
            InsecureSecretKeyAccess.get()
        )
    }

    private fun loadAeadsFromJson(keysetJson: String) {
        val handle = TinkJsonProtoKeysetFormat.parseKeyset(
            keysetJson,
            InsecureSecretKeyAccess.get()
        )
        val aead = handle.getPrimitive(
            RegistryConfiguration.get(),
            Aead::class.java
        )
        aeads = CipherType.entries.associateWith { aead }
        logger.i { "CryptoProvider initialized with ${aeads.size} ciphers" }
    }

    private fun encryptKeysetJson(json: String, masterKey: ByteArray): String {
        val iv = ByteArray(GCM_IV_LENGTH).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.ENCRYPT_MODE,
            SecretKeySpec(masterKey, "AES"),
            GCMParameterSpec(GCM_TAG_LENGTH, iv)
        )
        val encrypted = cipher.doFinal(json.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(iv + encrypted)
    }

    private fun decryptKeysetJson(encrypted: String, masterKey: ByteArray): String {
        val data = try {
            Base64.getDecoder().decode(encrypted)
        } catch (e: Exception) {
            throw CryptoException.KeysetDecryptionFailed(e)
        }
        if (data.size < GCM_IV_LENGTH) {
            throw CryptoException.KeysetDecryptionFailed(
                IllegalStateException("Encrypted keyset too short")
            )
        }
        val iv = data.copyOfRange(0, GCM_IV_LENGTH)
        val ciphertext = data.copyOfRange(GCM_IV_LENGTH, data.size)
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(masterKey, "AES"),
                GCMParameterSpec(GCM_TAG_LENGTH, iv)
            )
            String(cipher.doFinal(ciphertext), Charsets.UTF_8)
        } catch (e: Exception) {
            throw CryptoException.KeysetDecryptionFailed(e)
        }
    }

    private companion object {
        const val GCM_IV_LENGTH = 12
        const val GCM_TAG_LENGTH = 128
    }
}