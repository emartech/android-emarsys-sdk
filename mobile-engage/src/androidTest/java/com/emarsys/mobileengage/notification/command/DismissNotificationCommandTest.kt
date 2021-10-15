package com.emarsys.mobileengage.notification.command

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.emarsys.testUtil.TimeoutUtils
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyZeroInteractions

class DismissNotificationCommandTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Test(expected = IllegalArgumentException::class)
    fun testDismissNotification_context_mustNotBeNull() {
        DismissNotificationCommand(null, Intent())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDismissNotification_intent_mustNotBeNull() {
        DismissNotificationCommand(mock(), null)
    }

    @Test
    fun testDismissNotification_callsNotificationManager() {
        val notificationId = 987

        val notificationManagerMock: NotificationManager = mock()

        val mockContext: Context = mock {
            on { getSystemService(Context.NOTIFICATION_SERVICE) } doReturn notificationManagerMock
        }

        val intent = Intent()
        val bundle = Bundle()
        bundle.putInt("notification_id", notificationId)
        intent.putExtra("payload", bundle)

        DismissNotificationCommand(mockContext, intent).run()

        verify(notificationManagerMock).cancel(notificationId)
    }

    @Test
    fun testDismissNotification_doesNotCallNotificationManager_ifBundleIsMissing() {
        val notificationManagerMock: NotificationManager = mock()

        val mockContext: Context = mock {
            on { getSystemService(Context.NOTIFICATION_SERVICE) } doReturn notificationManagerMock
        }

        val intent = Intent()

        DismissNotificationCommand(mockContext, intent).run()

        verifyZeroInteractions(notificationManagerMock)
    }

    @Test
    fun testDismissNotification_doesNotCallNotificationManager_ifNotificationIdIsMissing() {
        val notificationManagerMock: NotificationManager = mock()

        val mockContext: Context = mock {
            on { getSystemService(Context.NOTIFICATION_SERVICE) } doReturn notificationManagerMock
        }

        val intent = Intent()
        val bundle = Bundle()
        intent.putExtra("payload", bundle)

        DismissNotificationCommand(mockContext, intent).run()

        verifyZeroInteractions(notificationManagerMock)
    }

}