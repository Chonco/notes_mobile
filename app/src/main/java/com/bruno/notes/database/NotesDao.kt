package com.bruno.notes.database

import androidx.room.*
import com.bruno.notes.database.image.Image
import com.bruno.notes.database.note.Note
import com.bruno.notes.database.relations.NoteWithImages
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(note: Note)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertImage(image: Image)

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

    @Transaction
    @Query("SELECT * FROM note WHERE id = :noteId")
    fun getNoteWithImagesByNoteId(noteId: Int): Flow<NoteWithImages>

    @Query("SELECT * FROM note WHERE note_title LIKE '%' || :searchTerm || '%' OR note_body LIKE '%' || :searchTerm || '%'")
    fun search(searchTerm: String): Flow<List<Note>>


}