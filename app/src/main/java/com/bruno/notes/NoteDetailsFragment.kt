package com.bruno.notes

import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.bruno.notes.data.dataaccess.NoteRepositoryImpl
import com.bruno.notes.data.model.Note
import com.bruno.notes.databinding.NoteDetailsFragmentBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class NoteDetailsFragment() : Fragment() {
    private val args: NoteDetailsFragmentArgs by navArgs()
    private lateinit var currentNote: Note

    private var _binding: NoteDetailsFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NoteDetailsFragmentBinding.inflate(inflater, container, false)

        currentNote = if (args.noteId != -1)
            NoteRepositoryImpl.getInstance().getById(args.noteId)
        else Note()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editableFactory = Editable.Factory.getInstance()

        binding.noteTitle.text = editableFactory.newEditable(currentNote.title)
        binding.noteBody.text = editableFactory.newEditable(currentNote.body)

        binding.noteBody.requestFocus()
    }

    override fun onDestroyView() {
        if (binding.noteBody.text?.isNotEmpty() == true) {
            currentNote.title = binding.noteTitle.text.toString()
            currentNote.body = binding.noteBody.text.toString()

            if (currentNote.id == -1)
                NoteRepositoryImpl.getInstance().save(currentNote)
            else
                NoteRepositoryImpl.getInstance().update(currentNote.id, currentNote)
        }

        super.onDestroyView()

        _binding = null
    }
}