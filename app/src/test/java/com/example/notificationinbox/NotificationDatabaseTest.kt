package com.example.notificationinbox

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.notificationinbox.data.local.AppDatabase
import com.example.notificationinbox.data.local.NotificationDao
import com.example.notificationinbox.data.local.NotificationEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NotificationDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: NotificationDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.notificationDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInsertAndRetrieveNotification() = runBlocking {
        val notification = NotificationEntity(
            id = "test_1",
            title = "Order Shipped",
            description = "Your package is on its way",
            imageUrl = "https://example.com/package.jpg",
            timestamp = 1000L,
            isRead = false
        )

        dao.insertNotification(notification)

        val retrieved = dao.getNotificationByIdOnce("test_1")
        assertEquals("test_1", retrieved?.id)
        assertEquals("Order Shipped", retrieved?.title)
        assertEquals("Your package is on its way", retrieved?.description)
        assertFalse(retrieved!!.isRead)
    }

    @Test
    fun testDuplicatePrevention() = runBlocking {
        // Requirement: Prevent duplicate notifications using notification ID
        val notif1 = NotificationEntity(
            id = "unique_fcm_id_999",
            title = "Initial Title",
            description = "Initial Description",
            timestamp = 2000L,
            isRead = false
        )
        dao.insertNotification(notif1)

        val listAfterFirstInsert = dao.getAllNotifications().first()
        assertEquals(1, listAfterFirstInsert.size)

        // Insert notification with identical ID but updated title
        val duplicateNotif = NotificationEntity(
            id = "unique_fcm_id_999",
            title = "Updated Title After Resend",
            description = "Initial Description",
            timestamp = 2500L,
            isRead = false
        )
        dao.insertNotification(duplicateNotif)

        val listAfterSecondInsert = dao.getAllNotifications().first()
        // Size must still be 1 (no duplicates!)
        assertEquals(1, listAfterSecondInsert.size)
        assertEquals("Updated Title After Resend", listAfterSecondInsert[0].title)
    }

    @Test
    fun testReadStatusUpdate() = runBlocking {
        val notif = NotificationEntity(
            id = "read_test_1",
            title = "Unread Message",
            description = "Click to read",
            isRead = false
        )
        dao.insertNotification(notif)

        var unreadCount = dao.getUnreadCount().first()
        assertEquals(1, unreadCount)

        // Mark as read
        dao.updateReadStatus("read_test_1", true)

        val updated = dao.getNotificationByIdOnce("read_test_1")
        assertTrue(updated!!.isRead)

        unreadCount = dao.getUnreadCount().first()
        assertEquals(0, unreadCount)
    }

    @Test
    fun testMarkAllAsRead() = runBlocking {
        dao.insertNotification(NotificationEntity("id_1", "Title 1", "Body 1", isRead = false))
        dao.insertNotification(NotificationEntity("id_2", "Title 2", "Body 2", isRead = false))
        dao.insertNotification(NotificationEntity("id_3", "Title 3", "Body 3", isRead = false))

        assertEquals(3, dao.getUnreadCount().first())

        dao.markAllAsRead()

        assertEquals(0, dao.getUnreadCount().first())
        val all = dao.getAllNotifications().first()
        assertTrue(all.all { it.isRead })
    }

    @Test
    fun testDeleteNotification() = runBlocking {
        dao.insertNotification(NotificationEntity("id_del", "Delete Me", "Desc"))
        val beforeDelete = dao.getNotificationByIdOnce("id_del")
        assertEquals("id_del", beforeDelete?.id)

        dao.deleteNotificationById("id_del")

        val afterDelete = dao.getNotificationByIdOnce("id_del")
        assertNull(afterDelete)
    }

    @Test
    fun testDeleteAllNotifications() = runBlocking {
        dao.insertNotification(NotificationEntity("id_a", "A", "Desc"))
        dao.insertNotification(NotificationEntity("id_b", "B", "Desc"))

        assertEquals(2, dao.getAllNotifications().first().size)

        dao.deleteAllNotifications()

        assertEquals(0, dao.getAllNotifications().first().size)
    }

    @Test
    fun testQueryOrderingByTimestamp() = runBlocking {
        dao.insertNotification(NotificationEntity("old", "Old", "Desc", timestamp = 100L))
        dao.insertNotification(NotificationEntity("new", "New", "Desc", timestamp = 500L))
        dao.insertNotification(NotificationEntity("mid", "Mid", "Desc", timestamp = 300L))

        val list = dao.getAllNotifications().first()
        assertEquals(3, list.size)
        // Order must be newest first (500L, 300L, 100L)
        assertEquals("new", list[0].id)
        assertEquals("mid", list[1].id)
        assertEquals("old", list[2].id)
    }
}
