package com.example.notificationinbox.service

import android.util.Log
import com.example.notificationinbox.NotificationInboxApp
import com.example.notificationinbox.data.local.NotificationEntity
import com.example.notificationinbox.utils.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

class NotificationInboxFirebaseService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}, MessageId: ${remoteMessage.messageId}")

        // 1. Extract Notification ID (prefer explicit id from data payload, fallback to messageId or generated UUID)
        val id = remoteMessage.data["id"]
            ?: remoteMessage.data["notification_id"]
            ?: remoteMessage.messageId
            ?: UUID.randomUUID().toString()

        // 2. Extract Title
        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "New Notification"

        // 3. Extract Description / Body
        val description = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: remoteMessage.data["description"]
            ?: ""

        // 4. Extract Image URL
        val imageUrl = remoteMessage.notification?.imageUrl?.toString()
            ?: remoteMessage.data["image_url"]
            ?: remoteMessage.data["imageUrl"]

        // 5. Extract Timestamp
        val timestamp = if (remoteMessage.sentTime > 0) {
            remoteMessage.sentTime
        } else {
            System.currentTimeMillis()
        }

        val notificationEntity = NotificationEntity(
            id = id,
            title = title,
            description = description,
            imageUrl = imageUrl,
            timestamp = timestamp,
            isRead = false
        )

        // Save locally to Room database via Repository
        serviceScope.launch {
            try {
                val repository = NotificationInboxApp.instance.repository
                repository.insertNotification(notificationEntity)
                Log.d(TAG, "Notification saved to Room DB: $id")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving notification to Room DB", e)
            }
        }

        // Display system notification with channel 'assignment'
        NotificationHelper.showSystemNotification(
            context = applicationContext,
            notificationId = id,
            title = title,
            message = description,
            imageUrl = imageUrl
        )
    }

    companion object {
        private const val TAG = "FCMInboxService"
    }
}
