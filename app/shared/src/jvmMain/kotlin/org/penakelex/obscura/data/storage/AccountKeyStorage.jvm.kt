package org.penakelex.obscura.data.storage

import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.encoding.Base64

actual class AccountKeyStorage {

    private val logger = Logger.withTag(StorageConfig.Log.ACCOUNT_KEY_TAG)

    private val keyFile: File by lazy {
        val dir = File(
            System.getProperty("user.home"),
            StorageConfig.Desktop.APP_DIR_NAME
        )
        if (!dir.exists()) dir.mkdirs()
        File(dir, StorageConfig.Desktop.ACCOUNT_KEY_FILE_NAME).apply {
            if (exists()) restrictPermissions(this)
        }
    }

    actual suspend fun saveMasterKey(masterKey: ByteArray) =
        withContext(Dispatchers.IO) {
            keyFile.writeText(Base64.encode(masterKey), Charsets.UTF_8)
            restrictPermissions(keyFile)
            logger.d { "Account master key saved" }
        }

    actual suspend fun loadMasterKey(): ByteArray? =
        withContext(Dispatchers.IO) {
            try {
                if (!keyFile.exists()) return@withContext null
                Base64.decode(keyFile.readText(Charsets.UTF_8)).also {
                    logger.d { "Account master key loaded (${it.size} bytes)" }
                }
            } catch (_: FileNotFoundException) {
                null
            } catch (e: Exception) {
                logger.w(e) { "Failed to load account master key" }
                null
            }
        }

    actual suspend fun clear() = withContext(Dispatchers.IO) {
        if (keyFile.exists()) keyFile.delete()
        logger.d { "Account master key cleared" }
    }

    private fun restrictPermissions(file: File) {
        try {
            val path = file.toPath()
            val fs = path.fileSystem
            if (fs.supportedFileAttributeViews().contains("posix")) {
                Files.setPosixFilePermissions(
                    path,
                    PosixFilePermissions.fromString(
                        StorageConfig.Desktop.POSIX_PERMISSIONS
                    )
                )
            }
        } catch (e: Exception) {
            logger.w(e) { "Could not set POSIX permissions on ${file.name}" }
        }
    }
}