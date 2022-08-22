package com.bruno.notes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.bruno.notes.databinding.FragmentImageViewBinding
import com.bumptech.glide.Glide

class ImageViewFragment : Fragment() {
    private val args: ImageViewFragmentArgs by navArgs()

    private var _binding: FragmentImageViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageViewBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(requireActivity().baseContext)
            .load(args.noteUri)
            .centerCrop()
            .into(binding.imageHolder)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}