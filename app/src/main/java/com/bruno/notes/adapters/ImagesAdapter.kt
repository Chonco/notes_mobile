package com.bruno.notes.adapters

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bruno.notes.database.image.Image
import com.bruno.notes.databinding.ImagePreviewBinding
import com.bumptech.glide.Glide

class ImagesAdapter(
    private val onImageClicked: (Image) -> Unit,
    private val onImageToDelete: (Image) -> Unit,
    private val activity: FragmentActivity
) : ListAdapter<Image, ImagesAdapter.ImageViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Image>() {
            override fun areItemsTheSame(oldItem: Image, newItem: Image): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Image, newItem: Image): Boolean {
                return oldItem.path == newItem.path
            }
        }
    }

    inner class ImageViewHolder(private var binding: ImagePreviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(image: Image) {
            val imageRetriever = ImageRetriever(image.path)
            val dataToRetrieveImage = imageRetriever.retrieveImagePath(
                activity.contentResolver
            )

            Log.i("ImagesAdapter", "binding layout, before glide")

            Glide.with(activity.baseContext)
                .load(dataToRetrieveImage?.imageUri)
                .centerCrop()
                .into(binding.imageViewContainer)

            binding.deleteImageButton.setOnClickListener { onImageToDelete }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val viewHolder = ImageViewHolder(
            ImagePreviewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            onImageClicked(getItem(position))
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    data class ImageRetrieve(
        val id: Long,
        val displayName: String,
        val relativePath: String,
        val imageUri: Uri
    )

    class ImageRetriever(private val displayName: String) {
        private val collection = MediaStore.Images.Media.getContentUri(
            MediaStore.VOLUME_EXTERNAL
        )

        private val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.RELATIVE_PATH
        )

        private val selection = "${MediaStore.Images.Media.DISPLAY_NAME} = ?"

        private val sortOrder = "${MediaStore.Images.Media.DISPLAY_NAME} ASC"

        fun retrieveImagePath(contentResolver: ContentResolver): ImageRetrieve? {
            var image: ImageRetrieve? = null

            val query = contentResolver.query(
                collection,
                projection,
                selection,
                arrayOf(displayName),
                sortOrder
            )

            query?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val relativePathColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)

                Log.i("INSIDE QUERY", "Count: ${cursor.count}")

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val relativePath = cursor.getString(relativePathColumn)

                    val imageUri: Uri =
                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                    image = ImageRetrieve(
                        id,
                        name,
                        relativePath,
                        imageUri
                    )

                    Log.i("Image Retrieve", image.toString())

                    break
                }
            }

            return image
        }
    }
}