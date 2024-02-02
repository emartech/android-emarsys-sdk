package com.emarsys.mobileengage.notification.command

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import com.emarsys.mobileengage.service.NotificationData
import com.emarsys.mobileengage.service.NotificationOperation
import com.emarsys.testUtil.TimeoutUtils
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

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
            inapp = null
        )
    }

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Test
    fun testDismissNotification_callsNotificationManager() {
        val notificationManagerMock: NotificationManager = mock()

        val mockContext: Context = mock {
            on { getSystemService(Context.NOTIFICATION_SERVICE) } doReturn notificationManagerMock
        }

        val intent = Intent()
        intent.putExtra("payload", notificationData)

        DismissNotificationCommand(mockContext, notificationData).run()

        verify(notificationManagerMock).cancel(COLLAPSE_ID, COLLAPSE_ID.hashCode())
    }

    @Test
    fun testDismissNotification_doesNotCallNotificationManager_ifBundleIsMissing() {
        val notificationManagerMock: NotificationManager = mock()

        val mockContext: Context = mock {
            on { getSystemService(Context.NOTIFICATION_SERVICE) } doReturn notificationManagerMock
        }

        DismissNotificationCommand(mockContext, null).run()

        verifyNoInteractions(notificationManagerMock)
    }

    @Test
    fun testDismissNotification_doesNotCallNotificationManager_ifNotificationIdIsMissing() {
        val notificationManagerMock: NotificationManager = mock()

        val mockContext: Context = mock {
            on { getSystemService(Context.NOTIFICATION_SERVICE) } doReturn notificationManagerMock
        }

        DismissNotificationCommand(mockContext, null).run()

        verifyNoInteractions(notificationManagerMock)
    }

}