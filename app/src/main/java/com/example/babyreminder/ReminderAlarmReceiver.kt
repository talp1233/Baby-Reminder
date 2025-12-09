package com.example.babyreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.babyreminder.NotificationHandler.Companion.REPEAT_COUNT_KEY

class ReminderAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Increment the repeat count
        val prefs = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
        val currentRepeats = prefs.getInt(REPEAT_COUNT_KEY, 0)
        prefs.edit().putInt(REPEAT_COUNT_KEY, currentRepeats + 1).apply()

        // Re-show the end driving notification
        val notificationHandler = NotificationHandler(context)
        // Pass isInitialCall = false since this is a repeated call
        notificationHandler.showEndDrivingNotification(isInitialCall = false)
    }
}
