package com.emarsys.mobileengage.notification.command

import com.emarsys.mobileengage.api.push.NotificationInformation
import com.emarsys.mobileengage.api.push.NotificationInformationListener
import com.emarsys.mobileengage.push.NotificationInformationListenerProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class NotificationInformationCommandTest {
    @Test
    fun testRun() {
        val mockNotificationInformationListener: NotificationInformationListener =
            mockk(relaxed = true)
        val mockNotificationInformationListenerProvider: NotificationInformationListenerProvider =
            mockk(relaxed = true)
        every { mockNotificationInformationListenerProvider.notificationInformationListener } returns mockNotificationInformationListener

        val notificationInformation = NotificationInformation("campaignId")
        val notificationInformationCommand = NotificationInformationCommand(
            mockNotificationInformationListenerProvider,
            notificationInformation
        )

        notificationInformationCommand.run()

        verify { mockNotificationInformationListenerProvider.notificationInformationListener }
        verify {
            mockNotificationInformationListener.onNotificationInformationReceived(
                notificationInformation
            )
        }
    }
}