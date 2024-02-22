package com.emarsys.mobileengage.notification.command

import com.emarsys.mobileengage.api.push.NotificationInformation
import com.emarsys.mobileengage.api.push.NotificationInformationListener
import com.emarsys.mobileengage.push.NotificationInformationListenerProvider
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

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