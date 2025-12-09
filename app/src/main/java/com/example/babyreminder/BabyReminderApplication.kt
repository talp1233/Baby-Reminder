package com.example.babyreminder

import android.app.Application
import android.content.Context
import android.content.res.Configuration

class BabyReminderApplication : Application() {

    lateinit var drivingStateDetector: DrivingStateDetector

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(base))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleHelper.onAttach(this)
    }

    override fun onCreate() {
        super.onCreate()
        drivingStateDetector = DrivingStateDetector(this)
        val notificationHandler = NotificationHandler(this)
        notificationHandler.createNotificationChannel()
    }
}
