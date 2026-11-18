package com.example.notificationinbox.data.model

import com.example.notificationinbox.data.local.NotificationEntity

data class NotificationItem(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val timestamp: Long,
    val isRead: Boolean
) {
    fun toEntity(): NotificationEntity = NotificationEntity(
        id = id,
        title = title,
        description = description,
        imageUrl = imageUrl,
        timestamp = timestamp,
        isRead = isRead
    )

    companion object {
        fun fromEntity(entity: NotificationEntity): NotificationItem = NotificationItem(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            imageUrl = entity.imageUrl,
            timestamp = entity.timestamp,
            isRead = entity.isRead
        )
    }
}
