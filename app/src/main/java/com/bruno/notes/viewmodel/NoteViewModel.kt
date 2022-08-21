package com.bruno.notes.viewmodel

import androidx.lifecycle.*
import com.bruno.notes.database.note.Note
import com.bruno.notes.database.NotesDao
import com.bruno.notes.database.image.Image
import kotlinx.coroutines.launch
import java.util.*

class NoteViewModel(private val notesDao: NotesDao) : ViewModel() {
    val allItems: LiveData<List<Note>> = notesDao.getAllNotesSortedByCreatedAt().asLiveData()

    private fun insertNote(note: Note) {
        viewModelScope.launch { notesDao.insertNote(note) }
    }

    private fun insertImage(image: Image) {
        viewModelScope.launch { notesDao.insertImage(image) }
    }

    private fun updateNote(note: Note) {
        viewModelScope.launch { notesDao.updateNote(note) }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch { notesDao.deleteNote(note) }
    }

    fun deleteImage(image: Image) {
        viewModelScope.launch { notesDao.deleteImage(image) }
    }

    private fun getUpdatedNoteEntry(
        id: Long,
        title: String,
        body: String,
        createdAt: Date,
        updatedAt: Date
    ): Note {
        return Note(id, title, body, java.sql.Date(createdAt.time), java.sql.Date(updatedAt.time))
    }

    fun updateNote(id: Long, title: String, body: String, createdAt: Date) {
        val updatedNote = getUpdatedNoteEntry(id, title, body, createdAt, Date())
        updateNote(updatedNote)
    }

    fun getAll(): LiveData<List<Note>> {
        return notesDao.getAllNotes().asLiveData()
    }

    fun getNote(id: Long): LiveData<Note> {
        return notesDao.getNoteById(id).asLiveData()
    }

    fun search(searchTerm: String): LiveData<List<Note>> {
        return notesDao.searchNotes(searchTerm).asLiveData()
    }

    fun addNewNote(title: String, body: String) {
        val note = getNewNoteEntry(title, body)
        insertNote(note)
    }

    fun createEmptyNote(): LiveData<Long> {
        val emptyNote = getNewNoteEntry("", "")
        insertNote(emptyNote)
        return getIdOfLastInsertedNote()
    }

    private fun getNewNoteEntry(title: String, body: String): Note {
        return Note(
            title = title,
            body = body,
            createdAt = java.sql.Date(Date().time),
            updatedAt = java.sql.Date(Date().time)
        )
    }

    private fun getIdOfLastInsertedNote(): LiveData<Long> {
        return notesDao.getIdOfLastInsertedNote().asLiveData()
    }

    fun addNewImage(path: String, noteId: Long) {
        val image = getNewImageEntry(path, noteId)
        insertImage(image)
    }

    private fun getNewImageEntry(path: String, noteId: Long): Image {
        return Image(
            noteId = noteId,
            path = path
        )
    }

}

class NoteViewModelFactory(private val notesDao: NotesDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java))
            return NoteViewModel(notesDao) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}