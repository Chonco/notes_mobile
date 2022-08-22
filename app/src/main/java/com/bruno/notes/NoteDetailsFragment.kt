package com.bruno.notes

import android.app.AlertDialog
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.*
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bruno.notes.adapters.ImagesAdapter
import com.bruno.notes.adapters.NotesAdapter
import com.bruno.notes.database.image.Image
import com.bruno.notes.database.note.Note
import com.bruno.notes.databinding.NoteDetailsFragmentBinding
import com.bruno.notes.helpers.TakePictureAndDetailsCommunication
import com.bruno.notes.listeners.SensorShakeListener
import com.bruno.notes.viewmodel.NoteViewModel
import com.bruno.notes.viewmodel.NoteViewModelFactory
import java.util.*

class NoteDetailsFragment : Fragment() {
    private val args: NoteDetailsFragmentArgs by navArgs()

    private val viewModel: NoteViewModel by activityViewModels {
        NoteViewModelFactory(
            (activity?.application as NotesApplication).database.noteDao()
        )
    }

    private var noteId: Long = -1
    private lateinit var noteCreatedAt: Date

    private var _binding: NoteDetailsFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var sensorManager: SensorManager? = null

    private val takePictureCommunication = TakePictureAndDetailsCommunication.getInstance()

    private val sensorListener = SensorShakeListener {
        onPause()
        activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(R.string.delete_body_alert_title)
                setMessage(R.string.delete_body_alert_body)
                setPositiveButton(R.string.delete_option_text) { _, _ ->
                    binding.noteBody.text = Editable.Factory.getInstance().newEditable("")
                    onResume()
                }
                setNegativeButton(R.string.cancel_option_text) { _, _ ->
                    onResume()
                }
            }
                .create()
        }?.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NoteDetailsFragmentBinding.inflate(inflater, container, false)

        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        Objects.requireNonNull(sensorManager)
            ?.registerListener(
                sensorListener,
                sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL
            )

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

        if (takePictureCommunication.comesFromTakePicture) {
            noteId = takePictureCommunication.noteId
            takePictureCommunication.comesFromTakePicture = false
        } else {
            noteId = args.noteId.toLong()
        }

        if (!isNewNote()) {
            viewModel.getNote(noteId).observe(this.viewLifecycleOwner) {
                noteCreatedAt = it.createdAt
                bind(it)
            }

            viewModel.getImagesOfNote(noteId).observe(this.viewLifecycleOwner) {
                if (it.isEmpty())
                    return@observe
                showImageListRecyclerView()
                setRecyclerView(it)
            }
        } else {
            viewModel.createEmptyNote().observe(this.viewLifecycleOwner) {
                noteId = it
                viewModel.getNote(it).observe(this.viewLifecycleOwner) { emptyNote ->
                    noteCreatedAt = emptyNote.createdAt
                }
            }
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.note_details_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.add_image_option -> {
                        val action =
                            NoteDetailsFragmentDirections.takePicture(noteId = noteId.toInt())
                        view.findNavController().navigate(action)
                        return true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding.noteBody.requestFocus()
    }

    private fun setRecyclerView(imagesList: List<Image>) {
        val imagesAdapter = ImagesAdapter({
            TODO("GO TO FRAGMENT TO VIEW COMPLETE IMAGE")
        }, {
            viewModel.deleteImage(it)
        }, requireActivity())

        val recyclerView = binding.imageListRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        recyclerView.adapter = imagesAdapter
        imagesAdapter.submitList(imagesList)
    }

    private fun showImageListRecyclerView() {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.noteDetailsConstraintLayout)
        constraintSet.connect(
            R.id.note_body,
            ConstraintSet.TOP,
            R.id.image_list_recycler_view,
            ConstraintSet.BOTTOM
        )
        constraintSet.applyTo(binding.noteDetailsConstraintLayout)

        binding.imageListRecyclerView.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        sensorManager!!.registerListener(
            sensorListener,
            sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager!!.unregisterListener(sensorListener)
    }

    override fun onDestroyView() {
        viewModel.updateNote(
            noteId,
            binding.noteTitle.text.toString(),
            binding.noteBody.text.toString(),
            noteCreatedAt
        )

        sensorManager!!.unregisterListener(sensorListener)

        super.onDestroyView()

        _binding = null
    }

    private fun isNewNote(): Boolean {
        return noteId == (-1).toLong()
    }


    private companion object {
        const val TAG = "NotesApp.NotesDetails"
    }
}