package com.bruno.notes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bruno.notes.adapters.NotesAdapter
import com.bruno.notes.databinding.NotesListFragmentBinding
import com.bruno.notes.viewmodel.NoteViewModel
import com.bruno.notes.viewmodel.NoteViewModelFactory

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class NotesListFragment : Fragment() {

    private val viewModel: NoteViewModel by activityViewModels {
        NoteViewModelFactory(
            (activity?.application as NotesApplication).database.noteDao()
        )
    }

    private var _binding: NotesListFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NotesListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val notesAdapter = NotesAdapter({
            val action = NotesListFragmentDirections.toNoteDetails(noteId = it.id)
            view.findNavController().navigate(action)
        }, {
            viewModel.deleteNote(it)
        })

        val recyclerView = binding.notesListRecyclerView
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = notesAdapter

        viewModel.allItems.observe(this.viewLifecycleOwner) { notes ->
            notes.let { notesAdapter.submitList(it) }
        }

        binding.addNote.setOnClickListener {
            findNavController().navigate(R.id.to_note_details)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}