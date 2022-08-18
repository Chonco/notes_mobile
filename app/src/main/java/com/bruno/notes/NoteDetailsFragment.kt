package com.bruno.notes

import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.bruno.notes.database.note.Note
import com.bruno.notes.databinding.NoteDetailsFragmentBinding
import com.bruno.notes.viewmodel.NoteViewModel
import com.bruno.notes.viewmodel.NoteViewModelFactory

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class NoteDetailsFragment : Fragment() {
    private val args: NoteDetailsFragmentArgs by navArgs()

    private val viewModel: NoteViewModel by activityViewModels {
        NoteViewModelFactory(
            (activity?.application as NotesApplication).database.noteDao()
        )
    }

    private lateinit var note: Note

    private var _binding: NoteDetailsFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NoteDetailsFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    private fun bind(note: Note) {
        val editableFactory = Editable.Factory.getInstance()
        binding.apply {
            noteTitle.text = editableFactory.newEditable(note.title)
            noteBody.text = editableFactory.newEditable(note.body)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isNewNote()) {
            viewModel.getNote(args.noteId).observe(this.viewLifecycleOwner) { selectedNote ->
                note = selectedNote
                bind(note)
            }
        }

        binding.noteBody.requestFocus()
    }

    override fun onDestroyView() {
        if (isEntryValid()) {
            if (isNewNote())
                viewModel.addNewNote(
                    binding.noteTitle.text.toString(),
                    binding.noteBody.text.toString()
                )
            else
                viewModel.updateNote(
                    note.id,
                    binding.noteTitle.text.toString(),
                    binding.noteBody.text.toString(),
                    note.createdAt
                )
        }

        super.onDestroyView()

        _binding = null
    }

    private fun isEntryValid(): Boolean {
        return binding.noteBody.text?.isNotEmpty() == true
    }

    private fun isNewNote(): Boolean {
        return args.noteId == -1
    }
}