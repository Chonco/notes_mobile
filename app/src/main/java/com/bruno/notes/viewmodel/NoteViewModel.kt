package com.bruno.notes.viewmodel

import androidx.lifecycle.*
import com.bruno.notes.database.note.Note
import com.bruno.notes.database.note.NoteDao
import kotlinx.coroutines.launch
import java.util.*

class NoteViewModel(private val noteDao: NoteDao) : ViewModel() {
    val allItems: LiveData<List<Note>> = noteDao.getAllSortedByCreatedAt().asLiveData()

    private fun insertNote(note: Note) {
        viewModelScope.launch { noteDao.insert(note) }
    }

    private fun updateNote(note: Note) {
        viewModelScope.launch { noteDao.update(note) }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch { noteDao.delete(note) }
    }

    private fun getUpdatedNoteEntry(
        id: Int,
        title: String,
        body: String,
        createdAt: Date,
        updatedAt: Date
    ): Note {
        return Note(id, title, body, java.sql.Date(createdAt.time), java.sql.Date(updatedAt.time))
    }

    fun updateNote(id: Int, title: String, body: String, createdAt: Date) {
        val updatedNote = getUpdatedNoteEntry(id, title, body, createdAt, Date())
        updateNote(updatedNote)
    }

    fun getAll(): LiveData<List<Note>> {
        return noteDao.getAll().asLiveData()
    }

    fun getNote(id: Int): LiveData<Note> {
        return noteDao.getById(id).asLiveData()
    }

    fun search(searchTerm: String): LiveData<List<Note>> {
        return noteDao.search(searchTerm).asLiveData()
    }

    fun addNewNote(title: String, body: String) {
        val note = getNewNoteEntry(title, body)
        insertNote(note)
    }

    private fun getNewNoteEntry(title: String, body: String): Note {
        return Note(
            title = title,
            body = body,
            createdAt = java.sql.Date(Date().time),
            updatedAt = java.sql.Date(Date().time)
        )
    }
}

class NoteViewModelFactory(private val noteDao: NoteDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java))
            return NoteViewModel(noteDao) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}