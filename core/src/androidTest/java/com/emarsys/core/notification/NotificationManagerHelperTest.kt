package com.emarsys.core.notification

import com.emarsys.core.api.notification.ChannelSettings
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock

class NotificationManagerHelperTest {

    private companion object {
        const val CHANNEL_ID_1 = "channelId1"
        const val CHANNEL_ID_2 = "channelId2"
        const val IMPORTANCE = 1
    }

    private lateinit var mockNotificationManagerProxy: NotificationManagerProxy
    private lateinit var notificationManagerHelper: NotificationManagerHelper

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

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

        val notificationChannel1 = ChannelSettings(
                CHANNEL_ID_1,
                IMPORTANCE,
                true,
                true,
                true,
                true
        )

        val notificationChannel2 = ChannelSettings(
                CHANNEL_ID_2,
                IMPORTANCE,
                false,
                true,
                false,
                true
        )

        whenever(mockNotificationManagerProxy.notificationChannels).thenReturn(listOf(notificationChannel1, notificationChannel2))

        val result = notificationManagerHelper.channelSettings

        result shouldBe expected
    }
}