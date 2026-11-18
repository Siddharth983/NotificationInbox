package com.example.notificationinbox

import android.app.Application
import com.example.notificationinbox.data.local.AppDatabase
import com.example.notificationinbox.data.repository.NotificationRepository
import com.example.notificationinbox.utils.NotificationHelper

class NotificationInboxApp : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    val repository: NotificationRepository by lazy {
        NotificationRepository(database.notificationDao())
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        // Initialize notification channel with ID 'assignment'
        NotificationHelper.createNotificationChannel(this)
    }

    companion object {
        lateinit var instance: NotificationInboxApp
            private set
    }
}
