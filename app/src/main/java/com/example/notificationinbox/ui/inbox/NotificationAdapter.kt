package com.example.notificationinbox.ui.inbox

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.notificationinbox.R
import com.example.notificationinbox.data.local.NotificationEntity
import com.example.notificationinbox.databinding.ItemNotificationBinding
import com.example.notificationinbox.utils.DateUtils

class NotificationAdapter(
    private val onNotificationClick: (NotificationEntity) -> Unit
) : ListAdapter<NotificationEntity, NotificationAdapter.NotificationViewHolder>(NotificationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding, onNotificationClick)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NotificationViewHolder(
        private val binding: ItemNotificationBinding,
        private val onNotificationClick: (NotificationEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NotificationEntity) {
            val context = itemView.context

            // Title & Description
            binding.tvTitle.text = item.title
            binding.tvDescription.text = item.description

            // Timestamp
            binding.tvTimestamp.text = DateUtils.formatRelativeTime(item.timestamp)

            // Read vs Unread Visual Styling
            if (item.isRead) {
                // Read Styling: clean white background, subtle border, no unread dot
                binding.innerLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.card_bg_read))
                binding.cardNotification.strokeColor = ContextCompat.getColor(context, R.color.card_stroke_read)
                binding.ivUnreadDot.visibility = View.GONE
                binding.tvTitle.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleSmall)
                binding.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                binding.tvStatusTag.text = context.getString(R.string.status_read)
                binding.tvStatusTag.setTextColor(ContextCompat.getColor(context, R.color.status_read_text))
            } else {
                // Unread Styling: subtle tinted background, colored border, unread dot visible
                binding.innerLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.card_bg_unread))
                binding.cardNotification.strokeColor = ContextCompat.getColor(context, R.color.card_stroke_unread)
                binding.ivUnreadDot.visibility = View.VISIBLE
                binding.tvTitle.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleMedium)
                binding.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                binding.tvStatusTag.text = context.getString(R.string.status_unread)
                binding.tvStatusTag.setTextColor(ContextCompat.getColor(context, R.color.status_unread_text))
            }

            // Image loading with Glide: handle valid, null, and broken image URLs
            val cornerRadiusPx = (context.resources.displayMetrics.density * 10).toInt()
            Glide.with(context)
                .load(item.imageUrl)
                .transform(CenterCrop(), RoundedCorners(cornerRadiusPx))
                .placeholder(R.drawable.ic_placeholder_image)
                .error(R.drawable.ic_broken_image)
                .into(binding.ivNotificationImage)

            // Click listener
            itemView.setOnClickListener {
                onNotificationClick(item)
            }
        }
    }
}
