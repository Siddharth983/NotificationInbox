package com.example.notificationinbox.ui.details

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.notificationinbox.NotificationInboxApp
import com.example.notificationinbox.R
import com.example.notificationinbox.data.local.NotificationEntity
import com.example.notificationinbox.databinding.ActivityNotificationDetailBinding
import com.example.notificationinbox.utils.Constants
import com.example.notificationinbox.utils.DateUtils
import com.example.notificationinbox.viewmodel.NotificationViewModel
import com.example.notificationinbox.viewmodel.NotificationViewModelFactory
import kotlinx.coroutines.launch

class NotificationDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationDetailBinding

    private val viewModel: NotificationViewModel by viewModels {
        NotificationViewModelFactory(NotificationInboxApp.instance.repository)
    }

    private var currentNotificationId: String? = null
    private var currentNotification: NotificationEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNotificationDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.detailMain) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupListeners()

        currentNotificationId = intent.getStringExtra(Constants.EXTRA_NOTIFICATION_ID)
        if (currentNotificationId == null) {
            Toast.makeText(this, R.string.notification_not_found, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        observeNotification(currentNotificationId!!)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupListeners() {
        binding.btnToggleRead.setOnClickListener {
            currentNotification?.let { item ->
                if (item.isRead) {
                    viewModel.markAsUnread(item.id)
                } else {
                    viewModel.markAsRead(item.id)
                }
            }
        }

        binding.btnDelete.setOnClickListener {
            currentNotification?.let { item ->
                viewModel.deleteNotification(item.id)
                Toast.makeText(this, R.string.notification_deleted, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun observeNotification(id: String) {
        viewModel.loadNotification(id)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedNotification.collect { notification ->
                    if (notification != null) {
                        currentNotification = notification
                        bindNotification(notification)

                        // Requirement: Automatically mark notification as read upon opening
                        if (!notification.isRead) {
                            viewModel.markAsRead(notification.id)
                        }
                    }
                }
            }
        }
    }

    private fun bindNotification(item: NotificationEntity) {
        binding.tvDetailTitle.text = item.title
        binding.tvDetailDescription.text = item.description
        binding.tvDetailTimestamp.text = DateUtils.formatFullDateTime(item.timestamp)

        // Status badge styling
        if (item.isRead) {
            binding.tvStatusBadge.text = getString(R.string.status_read)
            binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_chip_read)
            binding.tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_read_text))
            binding.btnToggleRead.text = getString(R.string.mark_as_unread)
        } else {
            binding.tvStatusBadge.text = getString(R.string.status_unread)
            binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_chip_unread)
            binding.tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_unread_text))
            binding.btnToggleRead.text = getString(R.string.mark_as_read)
        }

        // Full banner image loading with Glide
        Glide.with(this)
            .load(item.imageUrl)
            .placeholder(R.drawable.ic_placeholder_image)
            .error(R.drawable.ic_broken_image)
            .into(binding.ivFullImage)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_details, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val toggleItem = menu?.findItem(R.id.action_toggle_read)
        currentNotification?.let { item ->
            toggleItem?.title = if (item.isRead) {
                getString(R.string.mark_as_unread)
            } else {
                getString(R.string.mark_as_read)
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_toggle_read -> {
                currentNotification?.let { notif ->
                    if (notif.isRead) {
                        viewModel.markAsUnread(notif.id)
                    } else {
                        viewModel.markAsRead(notif.id)
                    }
                }
                true
            }
            R.id.action_delete -> {
                currentNotification?.let { notif ->
                    viewModel.deleteNotification(notif.id)
                    Toast.makeText(this, R.string.notification_deleted, Toast.LENGTH_SHORT).show()
                    finish()
                }
                true
            }
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
