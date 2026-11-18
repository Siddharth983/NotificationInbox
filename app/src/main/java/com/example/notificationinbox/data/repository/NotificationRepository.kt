package com.example.notificationinbox.data.repository

import com.example.notificationinbox.data.local.NotificationDao
import com.example.notificationinbox.data.local.NotificationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class NotificationRepository(
    private val notificationDao: NotificationDao
) {

    val allNotifications: Flow<List<NotificationEntity>> =
        notificationDao.getAllNotifications().flowOn(Dispatchers.IO)

    val unreadCount: Flow<Int> =
        notificationDao.getUnreadCount().flowOn(Dispatchers.IO)

    fun getNotificationById(id: String): Flow<NotificationEntity?> {
        return notificationDao.getNotificationById(id).flowOn(Dispatchers.IO)
    }

    suspend fun getNotificationByIdOnce(id: String): NotificationEntity? = withContext(Dispatchers.IO) {
        notificationDao.getNotificationByIdOnce(id)
    }

    suspend fun insertNotification(notification: NotificationEntity) = withContext(Dispatchers.IO) {
        notificationDao.insertNotification(notification)
    }

    suspend fun insertNotifications(notifications: List<NotificationEntity>) = withContext(Dispatchers.IO) {
        notificationDao.insertNotifications(notifications)
    }

    suspend fun markAsRead(id: String) = withContext(Dispatchers.IO) {
        notificationDao.updateReadStatus(id, true)
    }

    suspend fun markAsUnread(id: String) = withContext(Dispatchers.IO) {
        notificationDao.updateReadStatus(id, false)
    }

    suspend fun markAllAsRead() = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead()
    }

    suspend fun deleteNotification(id: String) = withContext(Dispatchers.IO) {
        notificationDao.deleteNotificationById(id)
    }

    suspend fun clearAllNotifications() = withContext(Dispatchers.IO) {
        notificationDao.deleteAllNotifications()
    }
}
