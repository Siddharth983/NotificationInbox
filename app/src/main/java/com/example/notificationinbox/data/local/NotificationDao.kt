package com.example.notificationinbox.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE is_read = 0")
    fun getUnreadCount(): Flow<Int>

    @Query("SELECT * FROM notifications WHERE id = :id LIMIT 1")
    fun getNotificationById(id: String): Flow<NotificationEntity?>

    @Query("SELECT * FROM notifications WHERE id = :id LIMIT 1")
    suspend fun getNotificationByIdOnce(id: String): NotificationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>): List<Long>

    @Update
    suspend fun updateNotification(notification: NotificationEntity): Int

    @Query("UPDATE notifications SET is_read = :isRead WHERE id = :id")
    suspend fun updateReadStatus(id: String, isRead: Boolean): Int

    @Query("UPDATE notifications SET is_read = 1")
    suspend fun markAllAsRead(): Int

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotificationById(id: String): Int

    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications(): Int
}
