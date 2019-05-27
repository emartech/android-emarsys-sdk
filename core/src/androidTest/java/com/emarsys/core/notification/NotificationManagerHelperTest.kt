package com.emarsys.core.notification

import android.app.NotificationChannel
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class NotificationManagerHelperTest {

    private lateinit var mockNotificationManagerProxy: NotificationManagerProxy
    private lateinit var notificationManagerHelper: NotificationManagerHelper

    private companion object {
        const val CHANNEL_ID_1 = "channelId1"
        const val CHANNEL_ID_2 = "channelId2"
        const val CHANNEL_NAME_1 = "channelName1"
        const val CHANNEL_NAME_2 = "channelName2"
        const val IMPORTANCE = 1
    }

    @Before
    fun setUp() {
        mockNotificationManagerProxy = mock(NotificationManagerProxy::class.java)
        notificationManagerHelper = NotificationManagerHelper(mockNotificationManagerProxy)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_notificationManagerProxy_mustNotBeNull() {
        NotificationManagerHelper(null)
    }

    @Test
    fun testAreNotificationsEnabled_shouldReturnTrue_whenNotificationsAreEnabled() {
        whenever(mockNotificationManagerProxy.areNotificationsEnabled()).thenReturn(true)
        val result = notificationManagerHelper.areNotificationsEnabled()

        result shouldBe true
    }

    @Test
    fun testAreNotificationsEnabled_shouldReturnFalse_whenNotificationsAreDisabled() {
        val result = notificationManagerHelper.areNotificationsEnabled()
        result shouldBe false
    }

    @Test
    fun testGetImportance_shouldReturnGivenImportance() {
        whenever(mockNotificationManagerProxy.importance).thenReturn(1)
        val result = notificationManagerHelper.importance

        result shouldBe 1
    }

    @Test
    fun testGetChannelSettings() {
        val expected = listOf(
                ChannelSettings(CHANNEL_ID_1, IMPORTANCE, true, true, true, true),
                ChannelSettings(CHANNEL_ID_2, IMPORTANCE, false, true, false, true))

        val notificationChannel1 = NotificationChannel(CHANNEL_ID_1, CHANNEL_NAME_1, 1).apply {
            setBypassDnd(true)
            setShowBadge(true)
            enableVibration(true)
            enableLights(true)
        }

        val notificationChannel2 = NotificationChannel(CHANNEL_ID_2, CHANNEL_NAME_2, 1).apply {
            setBypassDnd(false)
            setShowBadge(true)
            enableVibration(false)
            enableLights(true)
        }

        whenever(mockNotificationManagerProxy.notificationChannels).thenReturn(listOf(notificationChannel1, notificationChannel2))

        val result = notificationManagerHelper.channelSettings

        result shouldBe expected

    }
}