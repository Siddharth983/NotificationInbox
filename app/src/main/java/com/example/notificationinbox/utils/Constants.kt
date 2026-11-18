package com.example.notificationinbox.utils

object Constants {
    // Notification Channel ID required by specification
    const val NOTIFICATION_CHANNEL_ID = "assignment"
    const val NOTIFICATION_CHANNEL_NAME = "Inbox Notifications"
    const val NOTIFICATION_CHANNEL_DESCRIPTION = "Notifications received for the assignment inbox"

    // Intent extras
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

    // Database name
    const val DATABASE_NAME = "notification_inbox_db"

    // Sample image URLs for testing / fallback demo
    const val SAMPLE_IMAGE_URL_1 = "https://picsum.photos/600/300?random=1"
    const val SAMPLE_IMAGE_URL_2 = "https://picsum.photos/600/300?random=2"
    const val SAMPLE_IMAGE_URL_3 = "https://picsum.photos/600/300?random=3"
}
