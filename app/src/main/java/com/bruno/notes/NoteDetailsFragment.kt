package com.bruno.notes

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bruno.notes.adapters.ImagesAdapter
import com.bruno.notes.database.note.Note
import com.bruno.notes.databinding.NoteDetailsFragmentBinding
import com.bruno.notes.helpers.NotificationScheduler
import com.bruno.notes.helpers.TakePictureAndDetailsCommunication
import com.bruno.notes.listeners.SensorShakeListener
import com.bruno.notes.menuproviders.NoteDetailsMenuProvider
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

    private lateinit var imagesAdapter: ImagesAdapter
    private var goingToTakePicture = false

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

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (allPermissionsGranted())
                startScheduleNotificationProcess()
            else {
                Toast.makeText(
                    requireActivity(),
                    "Permissions not granted by the user",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NoteDetailsFragmentBinding.inflate(inflater, container, false)

        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager!!.registerListener(
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

        goingToTakePicture = false

        if (takePictureCommunication.comesFromTakePicture) {
            noteId = takePictureCommunication.noteId
            takePictureCommunication.comesFromTakePicture = false
        } else {
            noteId = args.noteId.toLong()
        }

        imagesAdapter = setRecyclerViewAndGetAdapter()

        if (!isNewNote()) {
            viewModel.getNote(noteId).observe(this.viewLifecycleOwner) {
                noteCreatedAt = it.createdAt
                bind(it)
            }

            viewModel.getImagesOfNote(noteId).observe(this.viewLifecycleOwner) { listImages ->
                if (listImages.isEmpty()) {
                    hideImageListRecyclerView()
                    imagesAdapter.submitList(emptyList())
                    return@observe
                }

                listImages.let { imagesAdapter.submitList(it) }
                showImageListRecyclerView()
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

        menuHost.addMenuProvider(NoteDetailsMenuProvider({
            goingToTakePicture = true
            val action =
                NoteDetailsFragmentDirections.takePicture(noteId = noteId.toInt())
            view.findNavController().navigate(action)
        }, {
            if (allPermissionsGranted())
                startScheduleNotificationProcess()
            else
                requestMultiplePermissions.launch(REQUIRED_PERMISSIONS)
        }), viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding.noteBody.requestFocus()
    }

    private fun startScheduleNotificationProcess() {
        NotificationScheduler(
            noteId,
            noteCreatedAt
        ).scheduleNotification(
            requireContext(),
            requireActivity().supportFragmentManager
        )
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireActivity().baseContext,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun setRecyclerViewAndGetAdapter(): ImagesAdapter {
        val imagesAdapter = ImagesAdapter({
            val action = NoteDetailsFragmentDirections.viewFullImageAction(it)
            view?.findNavController()?.navigate(action)
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
        return imagesAdapter
    }

    private fun showImageListRecyclerView() {
        updateUIAccordingImageListRecyclerViewVisibility(
            R.id.image_list_recycler_view,
            View.VISIBLE
        )
    }

    private fun hideImageListRecyclerView() {
        updateUIAccordingImageListRecyclerViewVisibility(
            R.id.note_title,
            View.GONE
        )
    }

    private fun updateUIAccordingImageListRecyclerViewVisibility(
        viewId: Int, visibility: Int
    ) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.noteDetailsConstraintLayout)
        constraintSet.connect(
            R.id.note_body,
            ConstraintSet.TOP,
            viewId,
            ConstraintSet.BOTTOM
        )
        constraintSet.applyTo(binding.noteDetailsConstraintLayout)

        binding.imageListRecyclerView.visibility = visibility
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
        if (isNotEmptyOrIsGoingToTakePicture()) {
            viewModel.updateNote(
                noteId,
                binding.noteTitle.text.toString(),
                binding.noteBody.text.toString(),
                noteCreatedAt
            )
        } else {
            Log.i(TAG, "Delete note because it's empty")
            viewModel.deleteNote(noteId)
        }

        sensorManager!!.unregisterListener(sensorListener)

        super.onDestroyView()

        _binding = null
    }

    private fun isNotEmptyOrIsGoingToTakePicture(): Boolean {
        return goingToTakePicture ||
                binding.noteTitle.text.toString().isNotEmpty() ||
                binding.noteBody.text.toString().isNotEmpty() ||
                imagesAdapter.currentList.isNotEmpty()
    }

    private fun isNewNote(): Boolean {
        return noteId == (-1).toLong()
    }


    companion object {
        private const val TAG = "NotesApp.NotesDetails"
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.SCHEDULE_EXACT_ALARM
        ).toTypedArray()
    }
}