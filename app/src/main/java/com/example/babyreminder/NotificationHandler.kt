package com.example.babyreminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat

class NotificationHandler(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val mainPrefs = context.getSharedPreferences("main_prefs", Context.MODE_PRIVATE)
    private val reminderPrefs = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)

    companion object {
        const val DRIVING_CHANNEL_ID = "driving_channel"
        const val EMERGENCY_CHANNEL_ID = "emergency_channel"
        const val START_DRIVING_NOTIFICATION_ID = 1
        const val END_DRIVING_NOTIFICATION_ID = 2
        const val PERMISSION_NOTIFICATION_ID = 3
        const val REPEAT_COUNT_KEY = "repeat_count"
        const val MAX_REPEATS = 10

        const val ACTION_YES = "com.example.babyreminder.ACTION_YES"
        const val ACTION_NO = "com.example.babyreminder.ACTION_NO"
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Standard Channel
            val name = "Driving Notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(DRIVING_CHANNEL_ID, name, importance).apply {
                description = "Notifications for driving state changes"
                setBypassDnd(true)
            }
            notificationManager.createNotificationChannel(channel)

            // Emergency Channel
            val emergencyName = "Emergency Reminders"
            val emergencyChannel = NotificationChannel(EMERGENCY_CHANNEL_ID, emergencyName, importance).apply {
                description = "Critical reminder notifications"
                setBypassDnd(true)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), audioAttributes)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
            }
            notificationManager.createNotificationChannel(emergencyChannel)
        }
    }

    private fun createFullScreenIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun showStartDrivingNotification() {
        val yesIntent = Intent(context, NotificationActionReceiver::class.java).apply { action = ACTION_YES }
        val yesPendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 101, yesIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val noIntent = Intent(context, NotificationActionReceiver::class.java).apply { action = ACTION_NO }
        val noPendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 102, noIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, DRIVING_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.start_driving_notification_title))
            .setContentText(context.getString(R.string.start_driving_notification_text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(createFullScreenIntent(), true)
            .addAction(NotificationCompat.Action(null, context.getString(R.string.notification_action_yes), yesPendingIntent))
            .addAction(NotificationCompat.Action(null, context.getString(R.string.notification_action_no), noPendingIntent))
            .setAutoCancel(true)

        if (!mainPrefs.getBoolean("enable_sound", true)) {
            builder.setSound(null).setVibrate(null)
        }

        notificationManager.notify(START_DRIVING_NOTIFICATION_ID, builder.build())
    }

    fun showEndDrivingNotification(isInitialCall: Boolean = false) {
        if (isInitialCall) {
            reminderPrefs.edit().putInt(REPEAT_COUNT_KEY, 0).apply()
        }

        val currentRepeats = reminderPrefs.getInt(REPEAT_COUNT_KEY, 0)
        val isEmergency = currentRepeats >= MAX_REPEATS - 1
        val channelId = if (isEmergency) EMERGENCY_CHANNEL_ID else DRIVING_CHANNEL_ID

        val yesIntent = Intent(context, NotificationActionReceiver::class.java).apply { action = ACTION_YES }
        val yesPendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 103, yesIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.end_driving_notification_title))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(createFullScreenIntent(), true)
            .setOngoing(true)
            .addAction(NotificationCompat.Action(null, context.getString(R.string.notification_action_confirm_yes), yesPendingIntent))

        if (!mainPrefs.getBoolean("enable_sound", true)) {
             builder.setSound(null).setVibrate(null)
        }

        notificationManager.notify(END_DRIVING_NOTIFICATION_ID, builder.build())
        scheduleReminder()
    }

    private fun scheduleReminder() {
        // ... (code unchanged)
    }

    private fun showPermissionRequestNotification() {
       // ... (code unchanged)
    }

    fun cancelReminder() {
        reminderPrefs.edit().putInt(REPEAT_COUNT_KEY, MAX_REPEATS).apply()
        val intent = Intent(context, ReminderAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 104, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
}
