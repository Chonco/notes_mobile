package com.bruno.notes.helpers

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import android.util.Log

class ImageRetriever(private val displayName: String) {
    private val collection = MediaStore.Images.Media.getContentUri(
        MediaStore.VOLUME_EXTERNAL
    )

    private val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME,
    )

    private val selection = "${MediaStore.Images.Media.DISPLAY_NAME} = ?"

    private val sortOrder = "${MediaStore.Images.Media.DISPLAY_NAME} ASC"

    fun retrieveImagePath(contentResolver: ContentResolver): Uri? {
        var imageUri: Uri? = null

        val query = contentResolver.query(
            collection,
            projection,
            selection,
            arrayOf(displayName),
            sortOrder
        )

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

            Log.i("INSIDE QUERY", "Count: ${cursor.count}")

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)

                imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                break
            }
        }

        return imageUri
    }
}