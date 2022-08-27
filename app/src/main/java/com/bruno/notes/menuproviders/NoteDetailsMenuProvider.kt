package com.bruno.notes.menuproviders

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import com.bruno.notes.R

class NoteDetailsMenuProvider(
    private val addImageHandler: () -> Unit,
    private val scheduleNotificationHandler: () -> Unit
) : MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.note_details_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.add_image_option -> {
                addImageHandler()
                return true
            }
            R.id.schedule_notification -> {
                scheduleNotificationHandler()
                return true
            }
            else -> false
        }
    }
}