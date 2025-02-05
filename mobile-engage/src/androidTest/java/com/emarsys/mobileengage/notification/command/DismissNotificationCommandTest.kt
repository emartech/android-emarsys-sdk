package com.emarsys.mobileengage.notification.command

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import com.emarsys.mobileengage.service.NotificationData
import com.emarsys.mobileengage.service.NotificationOperation
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class DismissNotificationCommandTest {
    private companion object {
        const val TITLE = "title"
        const val BODY = "body"
        const val CHANNEL_ID = "channelId"
        const val COLLAPSE_ID = "testCollapseId"
        const val MULTICHANNEL_ID = "test multiChannel id"
        const val SID = "test sid"
        const val SMALL_RESOURCE_ID = 123
        const val COLOR_RESOURCE_ID = 456
        val OPERATION = NotificationOperation.INIT.name
        val notificationData = NotificationData(
            null,
            null,
            null,
            TITLE,
            BODY,
            CHANNEL_ID,
            campaignId = MULTICHANNEL_ID,
            sid = SID,
            smallIconResourceId = SMALL_RESOURCE_ID,
            colorResourceId = COLOR_RESOURCE_ID,
            collapseId = COLLAPSE_ID,
            operation = OPERATION,
            actions = null,
            inapp = null,
            u = "{\"customField\":\"customValue\"}",
            message_id = "messageId"
        )
    }

    private lateinit var mockNotificationManager: NotificationManager
    private lateinit var mockContext: Context

    @Before
    fun setup() {
        mockNotificationManager = mockk(relaxed = true)
        mockContext = mockk(relaxed = true)
        every { mockContext.getSystemService(Context.NOTIFICATION_SERVICE) } returns mockNotificationManager
    }

    @Test
    fun testDismissNotification_callsNotificationManager() {
        val intent = Intent()
        intent.putExtra("payload", notificationData)

        DismissNotificationCommand(mockContext, notificationData).run()

        verify { mockNotificationManager.cancel(COLLAPSE_ID, COLLAPSE_ID.hashCode()) }
    }

    @Test
    fun testDismissNotification_doesNotCallNotificationManager_ifBundleIsMissing() {
        DismissNotificationCommand(mockContext, null).run()

        confirmVerified(mockNotificationManager)
    }

    @Test
    fun testDismissNotification_doesNotCallNotificationManager_ifNotificationIdIsMissing() {
        DismissNotificationCommand(mockContext, null).run()

        confirmVerified(mockNotificationManager)
    }
}