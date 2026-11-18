package com.example.notificationinbox.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "image_url")
    val imageUrl: String? = null,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false
)
