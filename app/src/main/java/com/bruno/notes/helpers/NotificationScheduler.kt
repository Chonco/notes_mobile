package com.bruno.notes.helpers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.bruno.notes.AppReceiver
import com.bruno.notes.listeners.DatePickerFragment
import com.bruno.notes.listeners.TimePickerFragment
import java.text.SimpleDateFormat
import java.util.*

class NotificationScheduler(
    private val noteId: Long,
    private val noteCreatedAt: Date
) {
    fun scheduleNotification(context: Context, fragmentManager: FragmentManager) {
        setWhenNotification(fragmentManager) { date ->
            scheduleAlertAtParticularTime(context, date)
        }
    }

    private fun setWhenNotification(
        fragmentManager: FragmentManager,
        onContinue: (GeneralResponse) -> Unit
    ) {
        Log.i(TAG, "To request complete date")

        val dateFragment = DatePickerFragment { year, month, day ->
            val timePicker = TimePickerFragment { hour, minute ->
                val date = DatePickerResponse(year, month, day)
                val time = TimePickerResponse(hour, minute)

                onContinue(GeneralResponse(date, time))
            }

            timePicker.show(fragmentManager, "Time Picker")
        }

        dateFragment.show(fragmentManager, "Date Picker")
    }

    private fun scheduleAlertAtParticularTime(context: Context, date: GeneralResponse) {
        val intent = Intent(context, AppReceiver::class.java)

        intent.putExtra("noteId", noteId)
        intent.putExtra(
            "noteCreatedAt",
            SimpleDateFormat("d MMM").format(noteCreatedAt)
        )

        Log.i(TAG, "Intent populated")

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            noteId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE,
        )

        val calendar = Calendar.getInstance()
        calendar.set(
            date.date!!.year, date.date.month, date.date.day,
            date.time!!.hour, date.time.minute
        )

        val alarmManager: AlarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        Log.i(TAG, "Alarm Manager instantiated")

        alarmManager.setExact(AlarmManager.RTC, calendar.timeInMillis, pendingIntent)

        Log.i(TAG, "Alarm scheduled")

        Toast.makeText(context, "Reminder scheduled", Toast.LENGTH_SHORT).show()
    }

    data class GeneralResponse(
        val date: DatePickerResponse?,
        val time: TimePickerResponse?
    )

    data class DatePickerResponse(
        val year: Int,
        val month: Int,
        val day: Int
    )

    data class TimePickerResponse(
        val hour: Int,
        val minute: Int
    )

    private companion object {
        const val TAG = "NotificationScheduler"
    }
}