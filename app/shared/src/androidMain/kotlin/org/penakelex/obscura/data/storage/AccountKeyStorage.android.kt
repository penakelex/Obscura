package org.penakelex.obscura.data.storage

import android.content.Context
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException

actual class AccountKeyStorage(private val context: Context) {
    private val logger =
        Logger.withTag(StorageConfig.Log.ACCOUNT_KEY_TAG)
    private val mutex = Mutex()

    private val appDir by lazy {
        File(
            context.filesDir,
            StorageConfig.Android.APP_DIR_NAME,
        ).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    private val masterKeyFile by lazy {
        File(
            appDir,
            StorageConfig.Android.ACCOUNT_MASTER_KEY_FILE_NAME,
        )
    }

    actual suspend fun saveMasterKey(masterKey: ByteArray) =
        mutex.withLock {
            withContext(Dispatchers.IO) {
                try {
                    masterKeyFile.writeBytes(masterKey)
                    logger.d {
                        "Account master key saved to file (${masterKey.size} bytes)"
                    }
                } catch (e: Exception) {
                    logger.e(e) {
                        "Failed to save account master key"
                    }
                    throw AccountKeyStorageException.SaveFailed(e)
                }
            }
        }

    actual suspend fun loadMasterKey(): ByteArray? = mutex.withLock {
        withContext(Dispatchers.IO) {
            try {
                logger.d {
                    appDir.list().contentToString()
                }

                if (!masterKeyFile.exists()) {
                    null
                } else {
                    masterKeyFile.readBytes().also {
                        logger.d {
                            "Account master key loaded (${it.size} bytes)"
                        }
                    }
                }
            } catch (_: FileNotFoundException) {
                null
            } catch (e: Exception) {
                logger.w(e) { "Failed to load account master key" }
                null
            }
        }
    }

    actual suspend fun clear() = mutex.withLock {
        withContext(Dispatchers.IO) {
            try {
                if (masterKeyFile.exists()) {
                    masterKeyFile.delete()
                }

                logger.d { "Account master key cleared" }
            } catch (e: Exception) {
                logger.e(e) { "Failed to clear account master key" }
                throw AccountKeyStorageException.ClearFailed(e)
            }
        }
    }
}