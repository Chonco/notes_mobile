package com.bruno.notes

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bruno.notes.adapters.NotesAdapter
import com.bruno.notes.database.note.Note
import com.bruno.notes.databinding.NotesListFragmentBinding
import com.bruno.notes.listeners.SearchInputWatcher
import com.bruno.notes.menuproviders.NotesListMenuProvider
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

    private lateinit var notesAdapter: NotesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NotesListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notesAdapter = NotesAdapter({ onNoteClicked(it, view) }, { onDeleteNote(it) })

        val recyclerView = binding.notesListRecyclerView
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = notesAdapter

        observeAllNotes()

        binding.addNote.setOnClickListener {
            findNavController().navigate(R.id.to_note_details)
        }

        binding.searchInput.addTextChangedListener(
            SearchInputWatcher { observeSearchNotes(it) }
        )

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(
            NotesListMenuProvider { showSearchInput() },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
    }

    private fun onNoteClicked(note: Note, view: View) {
        val action = NotesListFragmentDirections.toNoteDetails(noteId = note.id.toInt())
        view.findNavController().navigate(action)
    }

    private fun onDeleteNote(note: Note) {
        viewModel.getImagesOfNote(note.id).observe(this.viewLifecycleOwner) { notesImages ->
            notesImages.forEach {
                viewModel.deleteImage(it)
            }
        }
        viewModel.deleteNote(note)
    }

    private fun showSearchInput() {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.notesListConstraintLayout)
        constraintSet.connect(
            R.id.notes_list_recycler_view,
            ConstraintSet.TOP,
            R.id.search_input,
            ConstraintSet.BOTTOM
        )
        constraintSet.applyTo(binding.notesListConstraintLayout)

        binding.searchInput.visibility = View.VISIBLE
    }

    private fun observeAllNotes() {
        viewModel.allItems.observe(this.viewLifecycleOwner) { notes ->
            notes.let { notesAdapter.submitList(it) }
        }
    }

    private fun observeSearchNotes(searchInput: String) {
        viewModel.search(searchInput).observe(viewLifecycleOwner) { notes ->
            notes.let { notesAdapter.submitList(it) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}