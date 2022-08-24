package com.bruno.notes.database.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.bruno.notes.database.image.Image
import com.bruno.notes.database.note.Note

data class NoteWithImages(
    @Embedded val note: Note,
    @Relation(
        parentColumn = "id",
        entityColumn = "note_id"
    )
    val images: List<Image>
)
