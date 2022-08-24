package com.bruno.notes.database

import androidx.room.*
import com.bruno.notes.database.image.Image
import com.bruno.notes.database.note.Note
import com.bruno.notes.database.relations.NoteWithImages
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNote(note: Note): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertImage(image: Image): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Delete
    suspend fun deleteImage(image: Image)

    @Query("DELETE FROM note WHERE id = :id")
    suspend fun deleteNoteWithId(id: Long)

    @Query("SELECT * FROM note")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM note ORDER BY created_at DESC")
    fun getAllNotesSortedByCreatedAt(): Flow<List<Note>>

    @Query("SELECT * FROM note WHERE id = :id")
    fun getNoteById(id: Long): Flow<Note>

    @Transaction
    @Query("SELECT * FROM note WHERE id = :noteId")
    fun getNoteWithImagesByNoteId(noteId: Long): Flow<NoteWithImages>

    @Query("SELECT * FROM note WHERE note_title LIKE '%' || :searchTerm || '%' OR note_body LIKE '%' || :searchTerm || '%'")
    fun searchNotes(searchTerm: String): Flow<List<Note>>

    @Query("SELECT id FROM note ORDER BY id DESC LIMIT 1")
    fun getIdOfLastInsertedNote(): Flow<Long>

    @Query("SELECT * FROM image WHERE note_id = :noteId")
    fun getImagesOfNote(noteId: Long): Flow<List<Image>>
}