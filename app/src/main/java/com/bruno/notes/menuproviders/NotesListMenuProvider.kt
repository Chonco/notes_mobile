package com.bruno.notes.menuproviders

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import com.bruno.notes.R

class NotesListMenuProvider(private val onSearchClicked: () -> Unit): MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.notes_list_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.search_menu_option -> {
                onSearchClicked()
                return true
            }
            else -> false
        }
    }
}