package org.penakelex.obscura.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.penakelex.obscura.data.local.dao.NoteDao
import org.penakelex.obscura.data.local.entity.NoteEntity

@Database(
    entities = [NoteEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ObscuraDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}