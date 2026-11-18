package com.example.notificationinbox

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notificationinbox.data.local.NotificationEntity
import com.example.notificationinbox.databinding.ActivityMainBinding
import com.example.notificationinbox.ui.details.NotificationDetailActivity
import com.example.notificationinbox.ui.inbox.NotificationAdapter
import com.example.notificationinbox.utils.Constants
import com.example.notificationinbox.viewmodel.NotificationViewModel
import com.example.notificationinbox.viewmodel.NotificationViewModelFactory
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: NotificationViewModel by viewModels {
        NotificationViewModelFactory(NotificationInboxApp.instance.repository)
    }

    private lateinit var notificationAdapter: NotificationAdapter

    // Permission launcher for Android 13+ (POST_NOTIFICATIONS)
    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupRecyclerView()
        setupListeners()
        observeData()

        // Check and request Android 13+ notification permission
        checkNotificationPermission()

        // Fetch and print FCM registration token to Logcat with tag "FCM_TOKEN"
        fetchAndLogFcmToken()

        // Handle navigation if opened from a notification click
        handleNotificationIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val notificationId = intent?.getStringExtra(Constants.EXTRA_NOTIFICATION_ID)
        if (!notificationId.isNullOrBlank()) {
            openDetailScreen(notificationId)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter { notification ->
            openDetailScreen(notification.id)
        }

        binding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = notificationAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupListeners() {
        binding.btnEmptyAddSample.setOnClickListener {
            viewModel.addSampleNotification()
        }
    }

    private fun observeData() {
        // Show initial loading
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe notifications list
                launch {
                    viewModel.notifications.collect { notifications ->
                        binding.progressBar.visibility = View.GONE
                        notificationAdapter.submitList(notifications)

                        if (notifications.isEmpty()) {
                            binding.layoutEmptyState.visibility = View.VISIBLE
                            binding.rvNotifications.visibility = View.GONE
                        } else {
                            binding.layoutEmptyState.visibility = View.GONE
                            binding.rvNotifications.visibility = View.VISIBLE
                        }
                    }
                }

                // Observe unread count
                launch {
                    viewModel.unreadCount.collect { count ->
                        updateUnreadUI(count)
                    }
                }
            }
        }
    }

    private fun updateUnreadUI(unreadCount: Int) {
        if (unreadCount > 0) {
            binding.tvUnreadBadge.text = getString(R.string.unread_count_format, unreadCount)
            binding.tvUnreadBadge.visibility = View.VISIBLE
            binding.tvSubtitle.text = getString(R.string.unread_count_format, unreadCount)
            binding.tvSubtitle.setTextColor(ContextCompat.getColor(this, R.color.primary))
        } else {
            binding.tvUnreadBadge.text = "0"
            binding.tvUnreadBadge.visibility = View.GONE
            binding.tvSubtitle.text = getString(R.string.no_unread)
            binding.tvSubtitle.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        }
    }

    private fun openDetailScreen(notificationId: String) {
        val intent = Intent(this, NotificationDetailActivity::class.java).apply {
            putExtra(Constants.EXTRA_NOTIFICATION_ID, notificationId)
        }
        startActivity(intent)
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Already granted
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    Toast.makeText(
                        this,
                        R.string.notification_permission_rationale,
                        Toast.LENGTH_LONG
                    ).show()
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun fetchAndLogFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM_TOKEN", token)
            } else {
                Log.e("FCM_TOKEN", "Failed to retrieve FCM token", task.exception)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_inbox, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_test -> {
                viewModel.addSampleNotification()
                Toast.makeText(this, "Sample notification added", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_mark_all_read -> {
                viewModel.markAllAsRead()
                Toast.makeText(this, R.string.mark_all_read, Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_clear_all -> {
                viewModel.clearAllNotifications()
                Toast.makeText(this, R.string.clear_all, Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
