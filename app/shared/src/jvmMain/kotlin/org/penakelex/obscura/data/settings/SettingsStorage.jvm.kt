package org.penakelex.obscura.data.settings

import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.penakelex.obscura.domain.model.settings.AppSettings
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions

actual class SettingsStorage {

    private val logger = Logger.withTag("SettingsStorage")
    private val mutex = Mutex()
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    private val settingsFile: File by lazy {
        val dir = File(System.getProperty("user.home"), ".obscura")
        if (!dir.exists()) {
            dir.mkdirs()
            restrictPermissions(dir)
        }
        File(dir, "settings.json")
    }

    private val _flow = MutableStateFlow(loadSync())
    actual fun observe(): Flow<AppSettings> = _flow.asStateFlow()

    actual suspend fun get(): AppSettings = _flow.value

    actual suspend fun save(settings: AppSettings) = mutex.withLock {
        withContext(Dispatchers.IO) {
            try {
                val content =
                    json.encodeToString<AppSettings>(settings)
                settingsFile.writeText(content, Charsets.UTF_8)
                restrictPermissions(settingsFile)
                _flow.value = settings
                logger.d { "Settings saved" }
            } catch (e: Exception) {
                logger.e(e) { "Failed to save settings" }
                throw e
            }
        }
    }

    private fun loadSync(): AppSettings = try {
        if (!settingsFile.exists()) {
            AppSettings()
        } else {
            val content = settingsFile.readText(Charsets.UTF_8)
            json.decodeFromString<AppSettings>(content).also {
                logger.d { "Settings restored" }
            }
        }
    } catch (_: FileNotFoundException) {
        AppSettings()
    } catch (e: Exception) {
        logger.w(e) { "Failed to load settings, using defaults" }
        AppSettings()
    }

    private fun restrictPermissions(file: File) {
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
            logger.w(e) { "Could not set POSIX permissions on ${file.name}" }
        }
    }
}