package com.example.notificationinbox

import com.example.notificationinbox.data.local.NotificationEntity
import com.example.notificationinbox.data.model.NotificationItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationModelTest {

    @Test
    fun testEntityToItemMapping() {
        val entity = NotificationEntity(
            id = "notif_1001",
            title = "Test Notification",
            description = "Test Description",
            imageUrl = "https://example.com/banner.jpg",
            timestamp = 1700000000000L,
            isRead = false
        )

        val item = NotificationItem.fromEntity(entity)

        assertEquals("notif_1001", item.id)
        assertEquals("Test Notification", item.title)
        assertEquals("Test Description", item.description)
        assertEquals("https://example.com/banner.jpg", item.imageUrl)
        assertEquals(1700000000000L, item.timestamp)
        assertFalse(item.isRead)

        val mappedBack = item.toEntity()
        assertEquals(entity, mappedBack)
    }

    @Test
    fun testReadStatusToggle() {
        val entity = NotificationEntity(
            id = "notif_1002",
            title = "Promo",
            description = "Get 20% off",
            isRead = true
        )

        assertTrue(entity.isRead)
        val unreadCopy = entity.copy(isRead = false)
        assertFalse(unreadCopy.isRead)
    }
}
