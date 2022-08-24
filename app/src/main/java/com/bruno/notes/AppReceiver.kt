package com.bruno.notes

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder

class AppReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "Event received")

        val noteId = intent.extras?.getLong("noteId") ?: return

        val noteTitle = intent.getStringExtra("noteCreatedAt") ?: "Your Note"

        val args = NoteDetailsFragmentArgs(noteId.toInt()).toBundle()

        val pendingIntent = NavDeepLinkBuilder(context)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.note_details_nav_fragment)
            .setArguments(args)
            .createPendingIntent()

        Log.i(TAG, "Pending intent created.")

        createNotificationChannel(context)

        val notificationBuilder =
            NotificationCompat.Builder(context, context.getString(R.string.notification_channel_id))
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(noteTitle)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_baseline_sticky_note_2_24)

        Log.i(TAG, "NotificationBuild instantiated")

        with(NotificationManagerCompat.from(context)) {
            this.notify(noteId.toInt(), notificationBuilder.build())
        }
    }

    private fun createNotificationChannel(context: Context) {
        val name = context.getString(R.string.notification_channel_name)
        val descriptionText = context.getString(R.string.notification_channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(
            context.getString(R.string.notification_channel_id),
            name,
            importance
        ).apply {
            description = descriptionText
        }

        // Register the channel with the system
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private companion object {
        const val TAG = "AppReceiver"
    }
}