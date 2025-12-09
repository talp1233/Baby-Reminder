package com.example.babyreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationHandler = NotificationHandler(context)
        val drivingPrefs = context.getSharedPreferences("driving_prefs", Context.MODE_PRIVATE)

        // Mark that a response was received
        drivingPrefs.edit().putBoolean("responded_to_start", true).apply()

        when (intent.action) {
            NotificationHandler.ACTION_YES -> {
                // This action is used for both notifications.
                // It confirms the user wants reminders for this session.
                drivingPrefs.edit().putBoolean("user_denied_session", false).apply()
                notificationHandler.cancelReminder()
                notificationHandler.cancelNotification(NotificationHandler.START_DRIVING_NOTIFICATION_ID)
                notificationHandler.cancelNotification(NotificationHandler.END_DRIVING_NOTIFICATION_ID)
            }
            NotificationHandler.ACTION_NO -> {
                // User denies this driving session.
                drivingPrefs.edit().putBoolean("user_denied_session", true).apply()
                notificationHandler.cancelNotification(NotificationHandler.START_DRIVING_NOTIFICATION_ID)

                // Broadcast that the session was denied
                val sessionIntent = Intent("com.example.babyreminder.ACTION_SESSION_DENIED")
                context.sendBroadcast(sessionIntent)
            }
        }
    }
}
