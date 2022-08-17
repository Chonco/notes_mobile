package com.bruno.notes.data.dataaccess

import com.bruno.notes.data.model.Note
import java.util.*

class NoteRepositoryImpl() : RepositoryInt<Note> {
    private var currentId = 0

    init {
        currentId = DumbDB.DATA[DumbDB.DATA.size - 1].id + 1
    }

    companion object {
        private var instance = NoteRepositoryImpl()

        fun getInstance(): NoteRepositoryImpl {
            return instance
        }

        fun updateInstance() {
            instance = NoteRepositoryImpl()
        }
    }

    override fun getAll(): List<Note> {
        return DumbDB.DATA.toMutableList()
    }

    override fun getById(id: Int): Note {
        return DumbDB.DATA.first { note -> note.id == id }
    }

    override fun getByTitle(title: String): List<Note> {
        return DumbDB.DATA.filter { note -> note.title == title }.toMutableList()
    }

    override fun containsInTitleOrBody(toSearch: String): List<Note> {
        return DumbDB.DATA.filter { note ->
            note.title.contains(toSearch) || note.body.contains(
                toSearch
            )
        }.toMutableList()
    }

    override fun save(input: Note): Int {
        input.id = currentId
        input.createdAt = Date()
        currentId++
        DumbDB.DATA.add(input)

        return input.id
    }

    override fun update(id: Int, toUpdate: Note): Note {
        val note = getById(id)

        note.title = toUpdate.title
        note.body = toUpdate.body
        return note
    }

    override fun delete(id: Int): Boolean {
        return DumbDB.DATA.remove(getById(id))
    }
}