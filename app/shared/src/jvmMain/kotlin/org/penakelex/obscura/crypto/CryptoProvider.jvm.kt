package org.penakelex.obscura.crypto

import co.touchlab.kermit.Logger
import com.google.crypto.tink.Aead
import com.google.crypto.tink.InsecureSecretKeyAccess
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.TinkJsonProtoKeysetFormat
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.PredefinedAeadParameters
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions

actual class CryptoProvider {
    private val logger = Logger.withTag(CRYPTO_LOG_TAG)
    private val aeads: Map<CipherType, Aead>

    init {
        AeadConfig.register()
        val keysetsDir = resolveKeysetsDirectory()
        aeads = CipherType.entries.associateWith { cipher ->
            loadOrCreateKeyset(keysetsDir, cipher)
                .getPrimitive(
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

    private fun loadOrCreateKeyset(
        dir: File,
        cipher: CipherType
    ): KeysetHandle {
        val keysetFile = File(dir, keysetFileName(cipher))
        val access = InsecureSecretKeyAccess.get()

        return if (keysetFile.exists()) {
            logger.d { "Loading existing keyset for $cipher" }
            val json = Files.readString(keysetFile.toPath())
            TinkJsonProtoKeysetFormat.parseKeyset(json, access)
        } else {
            logger.i { "Generating new keyset for $cipher" }
            val handle =
                KeysetHandle.generateNew(parametersFor(cipher))
            val json = TinkJsonProtoKeysetFormat.serializeKeyset(
                handle,
                access
            )
            Files.writeString(keysetFile.toPath(), json)
            restrictFilePermissions(keysetFile)
            handle
        }
    }

    private fun resolveKeysetsDirectory(): File {
        val dir =
            File(System.getProperty("user.home"), KEYSETS_DIR_PATH)
        if (!dir.exists()) {
            dir.mkdirs()
            restrictFilePermissions(dir)
        }
        return dir
    }

    private fun restrictFilePermissions(file: File) {
        try {
            val path = file.toPath()
            val fs = path.fileSystem
            if (fs.supportedFileAttributeViews().contains("posix")) {
                Files.setPosixFilePermissions(
                    path,
                    PosixFilePermissions.fromString("rw-------")
                )
            }
        } catch (e: Exception) {
            logger.w(e) {
                "Could not set POSIX permissions on ${file.name}"
            }
        }
    }

    private fun keysetFileName(cipher: CipherType): String =
        "${KEYSET_PREFIX}_${cipher.name.lowercase()}.json"

    private fun parametersFor(cipher: CipherType) = when (cipher) {
        CipherType.AES_GCM -> PredefinedAeadParameters.AES256_GCM
        CipherType.XCHACHA20_POLY1305 ->
            PredefinedAeadParameters.XCHACHA20_POLY1305
    }

    private companion object {
        const val KEYSETS_DIR_PATH = ".obscura/keysets"
        const val KEYSET_PREFIX = "obscura_keyset"
    }
}