package com.bruno.notes.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bruno.notes.database.image.Image
import com.bruno.notes.databinding.ImagePreviewBinding
import com.bruno.notes.helpers.ImageRetriever
import com.bumptech.glide.Glide

class ImagesAdapter(
    private val onImageClicked: (Uri) -> Unit,
    private val onImageToDelete: (Image) -> Unit,
    private val activity: FragmentActivity
) : ListAdapter<Image, ImagesAdapter.ImageViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Image>() {
            override fun areItemsTheSame(oldItem: Image, newItem: Image): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Image, newItem: Image): Boolean {
                return oldItem.displayName == newItem.displayName
            }
        }
    }

    inner class ImageViewHolder(private var binding: ImagePreviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(image: Image) {
            val imageRetriever = ImageRetriever(image.displayName)
            val imageUri = imageRetriever.retrieveImagePath(
                activity.contentResolver
            )

            Glide.with(activity.baseContext)
                .load(imageUri)
                .centerCrop()
                .into(binding.imageViewContainer)

            binding.deleteImageButton.setOnClickListener { onImageToDelete(image) }

            binding.imageViewContainer.setOnClickListener {
                if (imageUri != null) {
                    onImageClicked(imageUri)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(
            ImagePreviewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}