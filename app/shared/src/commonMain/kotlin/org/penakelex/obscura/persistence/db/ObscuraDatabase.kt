package org.penakelex.obscura.persistence.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.penakelex.obscura.persistence.dao.NoteDao
import org.penakelex.obscura.persistence.entity.NoteEntity

@Database(
    entities = [NoteEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ObscuraDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}