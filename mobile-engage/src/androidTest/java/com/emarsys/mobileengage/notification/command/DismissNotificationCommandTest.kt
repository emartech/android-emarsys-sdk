package com.emarsys.mobileengage.notification.command

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.MockitoTestUtils.whenever
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.*

class DismissNotificationCommandTest {

    @Rule
    @JvmField
    var timeout: TestRule = TimeoutUtils.timeoutRule

    @Test(expected = IllegalArgumentException::class)
    fun testDismissNotification_context_mustNotBeNull() {
        DismissNotificationCommand(null, Intent())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDismissNotification_intent_mustNotBeNull() {
        DismissNotificationCommand(mock(Context::class.java), null)
    }

    @Test
    fun testDismissNotification_callsNotificationManager() {
        val notificationId = 987

        val notificationManagerMock = mock(NotificationManager::class.java)

        val mockContext = mock(Context::class.java)
        whenever(mockContext.getSystemService(Context.NOTIFICATION_SERVICE)).thenReturn(notificationManagerMock)

        val intent = Intent()
        val bundle = Bundle()
        bundle.putInt("notification_id", notificationId)
        intent.putExtra("payload", bundle)

        DismissNotificationCommand(mockContext, intent).run()

        verify(notificationManagerMock).cancel(notificationId)
    }

    @Test
    fun testDismissNotification_doesNotCallNotificationManager_ifBundleIsMissing() {
        val notificationManagerMock = mock(NotificationManager::class.java)

        val mockContext = mock(Context::class.java)
        whenever(mockContext.getSystemService(Context.NOTIFICATION_SERVICE)).thenReturn(notificationManagerMock)

        val intent = Intent()

        DismissNotificationCommand(mockContext, intent).run()

        verifyZeroInteractions(notificationManagerMock)
    }

    @Test
    fun testDismissNotification_doesNotCallNotificationManager_ifNotificationIdIsMissing() {
        val notificationManagerMock = mock(NotificationManager::class.java)

        val mockContext = mock(Context::class.java)
        whenever(mockContext.getSystemService(Context.NOTIFICATION_SERVICE)).thenReturn(notificationManagerMock)

        val intent = Intent()
        val bundle = Bundle()
        intent.putExtra("payload", bundle)

        DismissNotificationCommand(mockContext, intent).run()

        verifyZeroInteractions(notificationManagerMock)
    }

}