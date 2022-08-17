package com.bruno.notes.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bruno.notes.NotesListFragmentDirections
import com.bruno.notes.R
import com.bruno.notes.data.dataaccess.NoteRepositoryImpl
import com.bruno.notes.data.model.Note
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat

class NotesAdapter(
    private val activity: Activity,
    private val noteCardLayout: Int,
    val navController: NavController
) :
    RecyclerView.Adapter<NotesAdapter.NotesViewHolder>() {

    private lateinit var allNotes: MutableList<Note>

    inner class NotesViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        private val cardTitle: TextView = view.findViewById(R.id.note_card_title)
        private val creationDate: TextView = view.findViewById(R.id.note_creation_date)
        private val content: TextView = view.findViewById(R.id.note_card_content)
        private val deleteButton: MaterialButton = view.findViewById(R.id.delete_note_button)

        fun render(note: Note) {
            cardTitle.text = note.title
            creationDate.text = SimpleDateFormat("d MMM").format(note.createdAt)
            content.text = note.body

            view.setOnClickListener {
                val action =
                    NotesListFragmentDirections.toNoteDetails(note.id)
                navController.navigate(action)
            }

            deleteButton.setOnClickListener {
                deleteCurrentNote(note)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        allNotes = NoteRepositoryImpl.getInstance().getAll().toMutableList()
        val layoutInflater = LayoutInflater.from(activity)
            .inflate(noteCardLayout, parent, false)
        return NotesViewHolder(layoutInflater)
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        println(allNotes[position])
        holder.render(allNotes[position])
    }

    override fun getItemCount(): Int {
        return NoteRepositoryImpl.getInstance().getAll().size
    }

    private fun deleteCurrentNote(note: Note) {
        val index = allNotes.indexOf(note)
        NoteRepositoryImpl.getInstance().delete(note.id)
        allNotes.removeAt(index)
        notifyItemRemoved(index)
    }
}