package com.bruno.notes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bruno.notes.adapters.NotesAdapter
import com.bruno.notes.data.dataaccess.NoteRepositoryImpl
import com.bruno.notes.databinding.NotesListFragmentBinding
import com.google.android.material.snackbar.Snackbar

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class NotesListFragment : Fragment() {

    private var _binding: NotesListFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var repository: NoteRepositoryImpl

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NotesListFragmentBinding.inflate(inflater, container, false)

        binding.addNote.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.add_note)
                .setAction("Action", null).show()
        }

        repository = NoteRepositoryImpl()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val notesAdapter = NotesAdapter(
            this.requireActivity(),
            R.layout.note_preview_card,
            repository,
            findNavController()
        )

        val recyclerLayoutManager = GridLayoutManager(activity, 2)

        binding.notesListRecyclerView.apply {
            layoutManager = recyclerLayoutManager
            adapter = notesAdapter
        }

        binding.addNote.setOnClickListener {
            findNavController().navigate(R.id.action_ListFragment_to_DetailsFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}