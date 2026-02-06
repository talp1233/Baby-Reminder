package com.example.babyreminder

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.UiModeManager
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DrivingStateBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_AUTO_RESPONSE = "com.example.babyreminder.ACTION_AUTO_RESPONSE"
        private const val AUTO_RESPONSE_DELAY_MS = 10_000L // 10 seconds
    }

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val notificationHandler = NotificationHandler(context)
        val drivingPrefs = context.getSharedPreferences("driving_prefs", Context.MODE_PRIVATE)
        val devicePrefs = context.getSharedPreferences("bluetooth_devices_prefs", Context.MODE_PRIVATE)
        val drivingStateDetector = DrivingStateDetector(context)

        when (intent.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                val device: BluetoothDevice? = getParcelableExtra(intent, BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                if (device?.name != null && isCarBluetoothDevice(device.name, devicePrefs)) {
                    handleDrivingStart(context, drivingStateDetector, notificationHandler, drivingPrefs)
                }
            }
            UiModeManager.ACTION_ENTER_CAR_MODE -> {
                handleDrivingStart(context, drivingStateDetector, notificationHandler, drivingPrefs)
            }

            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                val device: BluetoothDevice? = getParcelableExtra(intent, BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                if (device?.name != null && isCarBluetoothDevice(device.name, devicePrefs)) {
                    handleDrivingStop(context, drivingStateDetector, notificationHandler, drivingPrefs)
                }
            }
            UiModeManager.ACTION_EXIT_CAR_MODE -> {
                handleDrivingStop(context, drivingStateDetector, notificationHandler, drivingPrefs)
            }

            ACTION_AUTO_RESPONSE -> {
                // Fired by AlarmManager after the auto-response delay
                val responded = drivingPrefs.getBoolean("responded_to_start", false)
                if (!responded) {
                    applyDefaultAction(context, drivingPrefs)
                }
            }

            Intent.ACTION_BOOT_COMPLETED -> {
                // Re-initialize driving state after reboot
                Log.d("DrivingStateReceiver", "Device booted. Resetting driving state.")
                drivingStateDetector.setDrivingState(false, context)
                drivingPrefs.edit()
                    .putBoolean("responded_to_start", false)
                    .putBoolean("user_denied_session", false)
                    .apply()
            }
        }
    }

    private fun handleDrivingStart(
        context: Context,
        drivingStateDetector: DrivingStateDetector,
        notificationHandler: NotificationHandler,
        drivingPrefs: SharedPreferences
    ) {
        Log.d("DrivingStateReceiver", "Driving started event processed.")
        drivingStateDetector.setDrivingState(true, context)
        drivingPrefs.edit().putBoolean("responded_to_start", false).apply()
        notificationHandler.showStartDrivingNotification()

        // Schedule auto-response via AlarmManager instead of GlobalScope
        scheduleAutoResponse(context)
    }

    private fun scheduleAutoResponse(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DrivingStateBroadcastReceiver::class.java).apply {
            action = ACTION_AUTO_RESPONSE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 300, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerTime = System.currentTimeMillis() + AUTO_RESPONSE_DELAY_MS
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
    }

    private fun applyDefaultAction(context: Context, drivingPrefs: SharedPreferences) {
        val mainPrefs = context.getSharedPreferences("main_prefs", Context.MODE_PRIVATE)
        val schedulePrefs = context.getSharedPreferences("schedule_prefs", Context.MODE_PRIVATE)

        val isDefaultYes = mainPrefs.getBoolean("default_yes", true)
        val isInScheduledTime = isCurrentlyInScheduledTime(schedulePrefs)

        // The final answer is "Yes" if the main default is "Yes", OR if we are in a scheduled override time.
        val finalAnswerIsYes = isDefaultYes || isInScheduledTime

        drivingPrefs.edit().putBoolean("user_denied_session", !finalAnswerIsYes).apply()
        Log.d("DrivingStateReceiver", "No response. Default is Yes: $isDefaultYes, In Schedule: $isInScheduledTime. Final Answer is Yes: $finalAnswerIsYes")
    }

    private fun isCurrentlyInScheduledTime(prefs: SharedPreferences): Boolean {
        val rules = prefs.getStringSet("rules", emptySet())?.mapNotNull { stringToRule(it) } ?: return false
        if (rules.isEmpty()) return false

        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US) ?: return false

        val timeFormat = SimpleDateFormat("HH:mm", Locale.US)

        for (rule in rules) {
            if (dayOfWeek in rule.days) {
                try {
                    val startTime = timeFormat.parse(rule.startTime)
                    val endTime = timeFormat.parse(rule.endTime)

                    val calStart = Calendar.getInstance().apply { time = startTime!! }
                    val calEnd = Calendar.getInstance().apply { time = endTime!! }

                    val calNow = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
                        set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    // Handle overnight schedules (e.g., 22:00 - 02:00)
                    if (calStart.after(calEnd)) {
                        if (!calNow.before(calStart) || !calNow.after(calEnd)) {
                            return true
                        }
                    } else {
                         if (!calNow.before(calStart) && calNow.before(calEnd)) {
                            return true
                        }
                    }
                } catch (e: Exception) {
                    Log.e("DrivingStateReceiver", "Error parsing schedule time", e)
                }
            }
        }
        return false
    }

    private fun handleDrivingStop(
        context: Context,
        drivingStateDetector: DrivingStateDetector,
        notificationHandler: NotificationHandler,
        drivingPrefs: SharedPreferences
    ) {
        Log.d("DrivingStateReceiver", "Driving stopped event processed.")
        drivingStateDetector.setDrivingState(false, context)
        drivingPrefs.edit().putBoolean("responded_to_start", false).apply()

        val userDenied = drivingPrefs.getBoolean("user_denied_session", false)
        if (!userDenied) {
            notificationHandler.showEndDrivingNotification(isInitialCall = true)
        }
    }

    private fun isCarBluetoothDevice(deviceName: String, prefs: SharedPreferences): Boolean {
        val savedDevices = prefs.getStringSet("device_names", emptySet()) ?: emptySet()
        if (savedDevices.any { deviceName.contains(it, ignoreCase = true) }) {
            return true
        }

        val popularCarManufacturers = listOf(
            "Toyota", "Volkswagen", "Ford", "Honda", "Chevrolet", "Nissan", "BMW", "Mercedes", "Audi", "Hyundai", "Kia", "Subaru", "Mazda", "Lexus", "Jeep", "GMC", "Ram", "Cadillac", "Buick", "Acura", "Volvo", "Tesla"
        )
        return popularCarManufacturers.any { deviceName.contains(it, ignoreCase = true) }
    }

    private fun <T> getParcelableExtra(intent: Intent, key: String, clazz: Class<T>): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(key, clazz)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(key) as? T
        }
    }
}
