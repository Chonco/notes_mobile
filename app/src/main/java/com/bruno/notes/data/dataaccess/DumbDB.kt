package com.bruno.notes.data.dataaccess

import com.bruno.notes.data.model.Note
import java.util.*

class DumbDB {
    companion object {
        val DATA: MutableList<Note> = mutableListOf(
            Note(1, "Test title 1", "Test Body 1", Date()),
            Note(2, "Test title 2", "Test Body 2", Date()),
            Note(3, "Test title 3", "Test Body 3", Date()),
            Note(4, "Test title 4", "Test Body 4", Date()),
        )
    }
}