package com.bruno.notes.database.image

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image")
data class Image(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "note_id")
    val noteId: Long = 0,

    @ColumnInfo(name = "path")
    val path: String
)