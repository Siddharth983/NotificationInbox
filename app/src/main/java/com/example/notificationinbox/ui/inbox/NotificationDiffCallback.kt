package com.example.notificationinbox.ui.inbox

import androidx.recyclerview.widget.DiffUtil
import com.example.notificationinbox.data.local.NotificationEntity

class NotificationDiffCallback : DiffUtil.ItemCallback<NotificationEntity>() {
    override fun areItemsTheSame(oldItem: NotificationEntity, newItem: NotificationEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: NotificationEntity, newItem: NotificationEntity): Boolean {
        return oldItem == newItem
    }
}
