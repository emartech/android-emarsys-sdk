package com.emarsys.mobileengage.notification.command

import com.emarsys.mobileengage.api.push.NotificationInformation
import com.emarsys.mobileengage.api.push.NotificationInformationListener
import com.emarsys.mobileengage.push.NotificationInformationListenerProvider
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

class NotificationInformationCommandTest {
    @Test
    fun testRun() {
        val mockNotificationInformationListener: NotificationInformationListener = mock()
        val mockNotificationInformationListenerProvider: NotificationInformationListenerProvider = mock {
            on { notificationInformationListener } doReturn mockNotificationInformationListener
        }
        val notificationInformation = NotificationInformation("campaignId")
        val notificationInformationCommand = NotificationInformationCommand(mockNotificationInformationListenerProvider, notificationInformation)
        notificationInformationCommand.run()

        verify(mockNotificationInformationListenerProvider).notificationInformationListener
        verify(mockNotificationInformationListener).onNotificationInformationReceived(notificationInformation)
    }
}