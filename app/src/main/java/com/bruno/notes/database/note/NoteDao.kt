package com.bruno.notes.database.note

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT * FROM note")
    fun getAll(): Flow<List<Note>>

    @Query("SELECT * FROM note ORDER BY created_at DESC")
    fun getAllSortedByCreatedAt(): Flow<List<Note>>

    @Query("SELECT * FROM note WHERE id = :id")
    fun getById(id: Int): Flow<Note>

    @Query("SELECT * FROM note WHERE note_title LIKE '%' || :searchTerm || '%' OR note_body LIKE '%' || :searchTerm || '%'")
    fun search(searchTerm: String): Flow<List<Note>>
}