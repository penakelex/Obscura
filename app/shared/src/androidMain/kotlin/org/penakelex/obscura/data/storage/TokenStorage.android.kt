package org.penakelex.obscura.data.storage

import android.content.Context
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.penakelex.obscura.domain.model.auth.SessionData
import java.io.File
import java.io.FileNotFoundException

actual class TokenStorage(private val context: Context) {
    private val logger = Logger.withTag(StorageConfig.Log.TAG)
    private val mutex = Mutex()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
        isLenient = false
    }

    private val sessionFile: File by lazy {
        val dir =
            File(context.filesDir, StorageConfig.Android.APP_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        File(dir, StorageConfig.Android.SESSION_FILE_NAME)
    }

    private val _sessionFlow = MutableStateFlow(loadSync())
    actual val sessionFlow: StateFlow<SessionData?> =
        _sessionFlow.asStateFlow()

    actual suspend fun save(session: SessionData) = mutex.withLock {
        withContext(Dispatchers.IO) {
            try {
                val content = json.encodeToString(session)
                sessionFile.writeText(content, Charsets.UTF_8)
                _sessionFlow.value = session
                logger.i {
                    "Session saved for user: ${session.userId}"
                }
            } catch (e: Exception) {
                logger.e(e) { "Failed to save session" }
                throw TokenStorageException.SaveFailed(e)
            }
        }
    }

    actual suspend fun clear() = mutex.withLock {
        withContext(Dispatchers.IO) {
            try {
                if (sessionFile.exists()) {
                    sessionFile.delete()
                }
                _sessionFlow.value = null
                logger.i { "Session cleared" }
            } catch (e: Exception) {
                logger.e(e) { "Failed to clear session" }
                throw TokenStorageException.ClearFailed(e)
            }
        }
    }

    private fun loadSync(): SessionData? = try {
        if (!sessionFile.exists()) {
            null
        } else {
            val content = sessionFile.readText(Charsets.UTF_8)
            json.decodeFromString<SessionData>(content).also {
                logger.d { "Session restored for user: ${it.userId}" }
            }
        }
    } catch (_: FileNotFoundException) {
        null
    } catch (e: Exception) {
        logger.w(e) { "Failed to restore session on init" }
        null
    }
}