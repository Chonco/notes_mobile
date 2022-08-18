package com.bruno.notes.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bruno.notes.database.note.Note
import com.bruno.notes.databinding.NotePreviewCardBinding
import java.text.SimpleDateFormat

class NotesAdapter(
    private val onItemClicked: (Note) -> Unit,
    private val onItemToDelete: (Note) -> Unit
) :
    ListAdapter<Note, NotesAdapter.NotesViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem.body == newItem.body && oldItem.title == newItem.title
            }
        }
    }

    inner class NotesViewHolder(private var binding: NotePreviewCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note) {
            binding.apply {
                noteCardTitle.text = note.title
                noteCardContent.text = note.body
                noteCreationDate.text =
                    SimpleDateFormat("d MMM").format(note.updatedAt)
                deleteNoteButton.setOnClickListener { onItemToDelete(note) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        val viewHolder = NotesViewHolder(
            NotePreviewCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            onItemClicked(getItem(position))
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}