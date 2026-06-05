package org.penakelex.obscura.data.storage

import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions

actual class GuestKeyStorage {
    private val logger =
        Logger.withTag(StorageConfig.Log.GUEST_KEY_TAG)
    private val mutex = Mutex()

    private val masterKeyFile: File by lazy {
        val dir = File(
            System.getProperty("user.home"),
            StorageConfig.Desktop.APP_DIR_NAME
        )
        if (!dir.exists()) dir.mkdirs()
        File(
            dir,
            StorageConfig.Desktop.GUEST_MASTER_KEY_FILE_NAME
        ).apply {
            if (exists()) restrictPermissions(this)
        }
    }

    private val keysetFile: File by lazy {
        val dir = File(
            System.getProperty("user.home"),
            StorageConfig.Desktop.APP_DIR_NAME
        )
        if (!dir.exists()) dir.mkdirs()
        File(
            dir,
            StorageConfig.Desktop.GUEST_KEYSET_FILE_NAME
        ).apply {
            if (exists()) restrictPermissions(this)
        }
    }

    actual suspend fun saveGuestMasterKey(masterKey: ByteArray) =
        mutex.withLock {
            withContext(Dispatchers.IO) {
                try {
                    masterKeyFile.writeBytes(masterKey)
                    restrictPermissions(masterKeyFile)
                    logger.d { "Guest master key saved" }
                } catch (e: Exception) {
                    logger.e(e) { "Failed to save guest master key" }
                    throw GuestKeyStorageException.SaveFailed(e)
                }
            }
        }

    actual suspend fun loadGuestMasterKey(): ByteArray? =
        mutex.withLock {
            withContext(Dispatchers.IO) {
                try {
                    if (!masterKeyFile.exists()) null
                    else masterKeyFile.readBytes()
                        .also { logger.d { "Guest master key loaded" } }
                } catch (_: FileNotFoundException) {
                    null
                } catch (e: Exception) {
                    logger.w(e) { "Failed to load guest master key" }
                    null
                }
            }
        }

    actual suspend fun saveGuestKeyset(encryptedKeysetJson: String) =
        mutex.withLock {
            withContext(Dispatchers.IO) {
                try {
                    keysetFile.writeText(
                        encryptedKeysetJson,
                        Charsets.UTF_8
                    )
                    restrictPermissions(keysetFile)
                    logger.d { "Guest keyset saved" }
                } catch (e: Exception) {
                    logger.e(e) { "Failed to save guest keyset" }
                    throw GuestKeyStorageException.SaveFailed(e)
                }
            }
        }

    actual suspend fun loadGuestKeyset(): String? = mutex.withLock {
        withContext(Dispatchers.IO) {
            try {
                if (!keysetFile.exists()) null
                else keysetFile.readText(Charsets.UTF_8)
                    .also { logger.d { "Guest keyset loaded" } }
            } catch (_: FileNotFoundException) {
                null
            } catch (e: Exception) {
                logger.w(e) { "Failed to load guest keyset" }
                null
            }
        }
    }

    actual suspend fun clearGuestData() = mutex.withLock {
        withContext(Dispatchers.IO) {
            try {
                if (masterKeyFile.exists()) masterKeyFile.delete()
                if (keysetFile.exists()) keysetFile.delete()
                logger.i { "All guest data cleared" }
            } catch (e: Exception) {
                logger.e(e) { "Failed to clear guest data" }
                throw GuestKeyStorageException.ClearFailed(e)
            }
        }
    }

    private fun restrictPermissions(file: File) {
        try {
            val path = file.toPath()
            val fs = path.fileSystem
            if (fs.supportedFileAttributeViews().contains("posix")) {
                Files.setPosixFilePermissions(
                    path,
                    PosixFilePermissions.fromString(StorageConfig.Desktop.POSIX_PERMISSIONS)
                )
            }
        } catch (e: Exception) {
            logger.w(e) { "Could not set POSIX permissions on ${file.name}" }
        }
    }
}