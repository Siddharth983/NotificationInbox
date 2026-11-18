package com.example.notificationinbox.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {

    private val fullDateTimeFormat = SimpleDateFormat("EEEE, MMM d, yyyy • h:mm a", Locale.getDefault())
    private val timeOnlyFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val monthDayFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    private val fullDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    fun formatRelativeTime(timestamp: Long): String {
        if (timestamp <= 0L) return ""

        val now = System.currentTimeMillis()
        val diffMillis = now - timestamp

        if (diffMillis < 0) {
            // Future or clock skew
            return fullDateTimeFormat.format(Date(timestamp))
        }

        val diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
        val diffHours = TimeUnit.MILLISECONDS.toHours(diffMillis)

        if (diffMinutes < 1) {
            return "Just now"
        } else if (diffMinutes < 60) {
            return "${diffMinutes}m ago"
        } else if (diffHours < 24) {
            val notificationCal = Calendar.getInstance().apply { timeInMillis = timestamp }
            val nowCal = Calendar.getInstance().apply { timeInMillis = now }
            if (notificationCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR) &&
                notificationCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
            ) {
                return timeOnlyFormat.format(Date(timestamp))
            }
        }

        val notificationCal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val yesterdayCal = Calendar.getInstance().apply {
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, -1)
        }

        if (notificationCal.get(Calendar.DAY_OF_YEAR) == yesterdayCal.get(Calendar.DAY_OF_YEAR) &&
            notificationCal.get(Calendar.YEAR) == yesterdayCal.get(Calendar.YEAR)
        ) {
            return "Yesterday"
        }

        val nowCal = Calendar.getInstance().apply { timeInMillis = now }
        return if (notificationCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)) {
            monthDayFormat.format(Date(timestamp))
        } else {
            fullDateFormat.format(Date(timestamp))
        }
    }

    fun formatFullDateTime(timestamp: Long): String {
        if (timestamp <= 0L) return ""
        return fullDateTimeFormat.format(Date(timestamp))
    }
}
