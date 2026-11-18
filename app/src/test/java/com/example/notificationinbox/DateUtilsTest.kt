package com.example.notificationinbox

import com.example.notificationinbox.utils.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DateUtilsTest {

    @Test
    fun testFormatRelativeTime_justNow() {
        val now = System.currentTimeMillis()
        val result = DateUtils.formatRelativeTime(now - 10_000) // 10 seconds ago
        assertEquals("Just now", result)
    }

    @Test
    fun testFormatRelativeTime_minutesAgo() {
        val now = System.currentTimeMillis()
        val result = DateUtils.formatRelativeTime(now - 5 * 60 * 1000) // 5 minutes ago
        assertEquals("5m ago", result)
    }

    @Test
    fun testFormatRelativeTime_emptyForZero() {
        val result = DateUtils.formatRelativeTime(0L)
        assertEquals("", result)
    }

    @Test
    fun testFormatFullDateTime_notEmpty() {
        val now = System.currentTimeMillis()
        val result = DateUtils.formatFullDateTime(now)
        assertTrue(result.isNotEmpty())
    }
}
