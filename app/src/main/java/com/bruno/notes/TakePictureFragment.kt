package com.bruno.notes

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bruno.notes.databinding.FragmentTakePictureBinding
import com.bruno.notes.helpers.TakePictureAndDetailsCommunication
import com.bruno.notes.viewmodel.NoteViewModel
import com.bruno.notes.viewmodel.NoteViewModelFactory
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class TakePictureFragment : Fragment() {
    private var _binding: FragmentTakePictureBinding? = null
    private val binding get() = _binding!!

    private val args: TakePictureFragmentArgs by navArgs()
    private val viewModel: NoteViewModel by activityViewModels {
        NoteViewModelFactory(
            (activity?.application as NotesApplication).database.noteDao()
        )
    }

    private val takePictureCommunication = TakePictureAndDetailsCommunication.getInstance()

    private var imageCapture: ImageCapture? = null

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (allPermissionsGranted())
                startCamera()
            else {
                Toast.makeText(
                    requireActivity(),
                    "Permissions not granted by the user",
                    Toast.LENGTH_SHORT
                ).show()
                returnToPrevPage()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTakePictureBinding.inflate(inflater, container, false)

        if (allPermissionsGranted())
            startCamera()
        else
            requestMultiplePermissions.launch(REQUIRED_PERMISSIONS)

        binding.cancelImageCapture.setOnClickListener {
            returnToPrevPage()
        }

        binding.imageCaptureButton.setOnClickListener { takePhoto() }

        return binding.root
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/NotesApp")
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                requireActivity().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val msg = "Photo saved successfully"
                    Toast.makeText(requireActivity().baseContext, msg, Toast.LENGTH_SHORT).show()
                    saveImageDisplayName(name)
                    returnToPrevPage()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                    returnToPrevPage()
                }
            }
        )
    }

    private fun saveImageDisplayName(displayName: String) {
        viewModel.addNewImage(
            "$displayName.jpg",
            args.noteId.toLong()
        )
    }

    private fun returnToPrevPage() {
        takePictureCommunication.comesFromTakePicture = true
        takePictureCommunication.noteId = args.noteId.toLong()
        findNavController().navigateUp()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireActivity().baseContext,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val TAG = "NotesApp.TakePictureFragment"
        const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA
            ).toTypedArray()
    }
}