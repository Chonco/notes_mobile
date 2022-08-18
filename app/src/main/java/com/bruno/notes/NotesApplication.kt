package com.bruno.notes

import android.app.Application
import com.bruno.notes.database.AppDatabase
import com.google.android.material.color.DynamicColors

class NotesApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}