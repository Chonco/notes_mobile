package com.bruno.notes

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import java.util.*
import kotlin.math.abs
import kotlin.math.sqrt

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

    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f

    private val sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            lastAcceleration = currentAcceleration

            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration = abs(acceleration * 0.9f + delta)

            println("Aceleración registrada: $acceleration")

            if (acceleration > 12) {
                binding.noteBody.text = Editable.Factory.getInstance().newEditable("")
            }
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
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

        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH

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

        sensorManager!!.unregisterListener(sensorListener)

        super.onDestroyView()

        _binding = null
    }

    private fun isEntryValid(): Boolean {
        return !isNewNote() || binding.noteBody.text?.isNotEmpty() == true
    }

    private fun isNewNote(): Boolean {
        return args.noteId == -1
    }
}