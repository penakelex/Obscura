package org.penakelex.obscura.persistence.db

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

actual class DatabaseFactory(private val context: Context) {
    actual fun create(): ObscuraDatabase {
        val dbFile = context.getDatabasePath("obscura.db")
        return Room
            .databaseBuilder<ObscuraDatabase>(
                context = context,
                name = dbFile.absolutePath
            )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .fallbackToDestructiveMigration(true)
            .build()
    }
}