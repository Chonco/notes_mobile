package com.bruno.notes.data.dataaccess

import com.bruno.notes.data.model.Note

class NoteRepositoryImpl() : RepositoryInt<Note> {
    private var currentId = 0

    init {
        currentId = DumbDB.DATA.size
    }

    override fun getAll(): List<Note> {
        return DumbDB.DATA.toMutableList();
    }

    override fun getById(id: Int): Note {
        return DumbDB.DATA.first { note -> note.id == id }
            .copy()
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
        input.id = currentId;
        currentId++;
        DumbDB.DATA.add(input)

        return input.id
    }

    override fun update(id: Int, toUpdate: Note): Note {
        val note = getById(id)

        note.title = toUpdate.title
        note.body = toUpdate.body
        return note.copy()
    }

    override fun delete(id: Int): Boolean {
        return DumbDB.DATA.remove(getById(id))
    }
}