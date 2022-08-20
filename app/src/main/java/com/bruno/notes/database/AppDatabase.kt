package com.bruno.notes.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bruno.notes.database.image.Image
import com.bruno.notes.database.note.Note

@Database(entities = [Note::class, Image::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): NotesDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .createFromAsset("database/notes.db")
                    .build()

                INSTANCE = instance

                return instance
            }
        }
    }
}