package org.penakelex.obscura.data.storage

import android.content.Context
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException

actual class GuestKeyStorage(private val context: Context) {
    private val logger =
        Logger.withTag(StorageConfig.Log.GUEST_KEY_TAG)
    private val mutex = Mutex()

    private val appDir by lazy {
        File(
            context.filesDir,
            StorageConfig.Android.APP_DIR_NAME,
        ).apply {
            if (!exists()) mkdirs()
        }
    }

    private val masterKeyFile by lazy {
        File(appDir, StorageConfig.Android.GUEST_MASTER_KEY_FILE_NAME)
    }

    private val keysetFile by lazy {
        File(appDir, StorageConfig.Android.GUEST_KEYSET_FILE_NAME)
    }

    actual suspend fun saveGuestMasterKey(masterKey: ByteArray) =
        mutex.withLock {
            withContext(Dispatchers.IO) {
                try {
                    masterKeyFile.writeBytes(masterKey)
                    logger.d { "Guest master key saved to file" }
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
                    else masterKeyFile.readBytes().also {
                        logger.d { "Guest master key loaded" }
                    }
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
}