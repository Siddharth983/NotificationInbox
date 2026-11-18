package com.example.notificationinbox.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notificationinbox.data.local.NotificationEntity
import com.example.notificationinbox.data.repository.NotificationRepository
import com.example.notificationinbox.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class NotificationViewModel(
    private val repository: NotificationRepository
) : ViewModel() {

    // Flow of all notifications ordered by timestamp descending
    val notifications: StateFlow<List<NotificationEntity>> =
        repository.allNotifications.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Flow of unread notifications count
    val unreadCount: StateFlow<Int> =
        repository.unreadCount.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val _selectedNotification = MutableStateFlow<NotificationEntity?>(null)
    val selectedNotification: StateFlow<NotificationEntity?> = _selectedNotification.asStateFlow()

    fun loadNotification(id: String) {
        viewModelScope.launch {
            repository.getNotificationById(id).collect { notification ->
                _selectedNotification.value = notification
            }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            repository.markAsRead(id)
        }
    }

    fun markAsUnread(id: String) {
        viewModelScope.launch {
            repository.markAsUnread(id)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllAsRead()
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    /**
     * Inserts a sample notification to facilitate local testing and verification.
     */
    fun addSampleNotification(
        customId: String? = null,
        title: String? = null,
        description: String? = null,
        imageUrl: String? = null
    ) {
        viewModelScope.launch {
            val sampleIndex = (1..3).random()
            val sampleImage = when (sampleIndex) {
                1 -> Constants.SAMPLE_IMAGE_URL_1
                2 -> Constants.SAMPLE_IMAGE_URL_2
                else -> Constants.SAMPLE_IMAGE_URL_3
            }

            val id = customId ?: "sample_${UUID.randomUUID().toString().take(8)}"
            val sampleNotification = NotificationEntity(
                id = id,
                title = title ?: "Security Alert #$sampleIndex",
                description = description
                    ?: "A new login attempt was verified for your account. Tap to inspect full device details and security recommendations.",
                imageUrl = imageUrl ?: sampleImage,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
            repository.insertNotification(sampleNotification)
        }
    }
}
