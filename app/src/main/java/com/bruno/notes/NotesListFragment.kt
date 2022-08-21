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

        notesAdapter = NotesAdapter({
            val action = NotesListFragmentDirections.toNoteDetails(noteId = it.id.toInt())
            view.findNavController().navigate(action)
        }, {
            viewModel.deleteNote(it)
        })

        val recyclerView = binding.notesListRecyclerView
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = notesAdapter

        observeAllNotes()

        binding.addNote.setOnClickListener {
            findNavController().navigate(R.id.to_note_details)
        }

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(searchInput: Editable?) {
                observeSearchNotes(searchInput.toString())
            }
        })

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.notes_list_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.search_menu_option -> {
                        showSearchInput()
                        return true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
}