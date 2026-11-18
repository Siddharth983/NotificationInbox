package com.example.notificationinbox.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.notificationinbox.MainActivity
import com.example.notificationinbox.R
import com.example.notificationinbox.ui.details.NotificationDetailActivity

object NotificationHelper {

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = Constants.NOTIFICATION_CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showSystemNotification(
        context: Context,
        notificationId: String,
        title: String,
        message: String,
        imageUrl: String? = null
    ) {
        // Ensure channel exists
        createNotificationChannel(context)

        // Check permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val targetIntent = Intent(context, NotificationDetailActivity::class.java).apply {
            putExtra(Constants.EXTRA_NOTIFICATION_ID, notificationId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val requestCode = notificationId.hashCode()
        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            targetIntent,
            pendingIntentFlags
        )

        val builder = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        if (!imageUrl.isNullOrBlank()) {
            // Attempt to load banner bitmap for BigPictureStyle
            try {
                Glide.with(context.applicationContext)
                    .asBitmap()
                    .load(imageUrl)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            builder.setLargeIcon(resource)
                            builder.setStyle(
                                NotificationCompat.BigPictureStyle()
                                    .bigPicture(resource)
                                    .bigLargeIcon(null as Bitmap?)
                                    .setSummaryText(message)
                            )
                            notify(context, requestCode, builder)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            // No-op
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            // Fallback to text-only notification
                            notify(context, requestCode, builder)
                        }
                    })
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        notify(context, requestCode, builder)
    }

    private fun notify(context: Context, id: Int, builder: NotificationCompat.Builder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(id, builder.build())
    }
}
