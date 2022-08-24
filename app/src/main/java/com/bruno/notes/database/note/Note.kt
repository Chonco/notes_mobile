package com.bruno.notes.database.note

import androidx.room.*
import com.bruno.notes.database.converters.DateConverter
import java.sql.Date

@Entity(tableName = "note")
@TypeConverters(value = [DateConverter::class])
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "note_title")
    val title: String,

    @ColumnInfo(name = "note_body")
    val body: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Date,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Date
)
