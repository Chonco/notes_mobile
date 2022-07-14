package com.bruno.notes

import android.app.Application
import com.google.android.material.color.DynamicColors

class NotesCustomApp: Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}