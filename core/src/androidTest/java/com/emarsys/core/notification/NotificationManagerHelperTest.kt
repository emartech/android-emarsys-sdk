package com.emarsys.core.notification

import com.emarsys.core.api.notification.ChannelSettings
import com.emarsys.testUtil.mockito.whenever
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class NotificationManagerHelperTest  {

    private companion object {
        const val CHANNEL_ID_1 = "channelId1"
        const val CHANNEL_ID_2 = "channelId2"
        const val IMPORTANCE = 1
    }

    private lateinit var mockNotificationManagerProxy: NotificationManagerProxy
    private lateinit var notificationManagerHelper: NotificationManagerHelper


    @Before
    fun setUp() {
        mockNotificationManagerProxy = mock(NotificationManagerProxy::class.java)
        notificationManagerHelper = NotificationManagerHelper(mockNotificationManagerProxy)
    }

    @Test
    fun testAreNotificationsEnabled_shouldReturnTrue_whenNotificationsAreEnabled() {
        whenever(mockNotificationManagerProxy.areNotificationsEnabled).thenReturn(true)
        val result = notificationManagerHelper.areNotificationsEnabled

        result shouldBe true
    }

    @Test
    fun testAreNotificationsEnabled_shouldReturnFalse_whenNotificationsAreDisabled() {
        val result = notificationManagerHelper.areNotificationsEnabled
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
            ChannelSettings(
                CHANNEL_ID_1, IMPORTANCE,
                isCanBypassDnd = true,
                isCanShowBadge = true,
                isShouldVibrate = true,
                isShouldShowLights = true
            ),
            ChannelSettings(
                CHANNEL_ID_2, IMPORTANCE,
                isCanBypassDnd = false,
                isCanShowBadge = true,
                isShouldVibrate = false,
                isShouldShowLights = true
            )
        )

        val notificationChannel1 = ChannelSettings(
            CHANNEL_ID_1,
            IMPORTANCE,
            isCanBypassDnd = true,
            isCanShowBadge = true,
            isShouldVibrate = true,
            isShouldShowLights = true
        )

        val notificationChannel2 = ChannelSettings(
            CHANNEL_ID_2,
            IMPORTANCE,
            isCanBypassDnd = false,
            isCanShowBadge = true,
            isShouldVibrate = false,
            isShouldShowLights = true
        )

        whenever(mockNotificationManagerProxy.notificationChannels).thenReturn(
            listOf(
                notificationChannel1,
                notificationChannel2
            )
        )

        val result = notificationManagerHelper.channelSettings

        result shouldBe expected
    }
}