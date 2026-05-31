package org.penakelex.obscura.persistence.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import java.io.File

actual class DatabaseFactory {
    actual fun create(): ObscuraDatabase {
        val dbPath = File(System.getProperty("user.home"), ".obscura")
        dbPath.mkdirs()
        val dbFile = File(dbPath, "obscura.db")

        return Room
            .databaseBuilder<ObscuraDatabase>(
                name = dbFile.absolutePath
            )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .fallbackToDestructiveMigration(true)
            .build()
    }
}