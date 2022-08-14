package com.bruno.notes.data.dataaccess

import com.bruno.notes.data.model.Note

class DumbDB {
    companion object {
        val DATA: MutableList<Note> = mutableListOf(
            Note(1, "Test title 1", "Test Body 1"),
            Note(2, "Test title 2", "Test Body 2"),
            Note(3, "Test title 3", "Test Body 3"),
            Note(4, "Test title 4", "Test Body 4"),
        )
    }
}