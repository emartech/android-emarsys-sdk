package com.emarsys.mobileengage.notification.command

import com.emarsys.mobileengage.api.push.NotificationInformation
import com.emarsys.mobileengage.api.push.NotificationInformationListener
import com.emarsys.mobileengage.push.SilentNotificationInformationListenerProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class SilentNotificationInformationCommandTest {

    @Test
    fun testRun() {
        val mockNotificationInformationListener: NotificationInformationListener =
            mockk(relaxed = true)
        val mockSilentNotificationInformationListenerProvider: SilentNotificationInformationListenerProvider =
            mockk(relaxed = true)
        every { mockSilentNotificationInformationListenerProvider.silentNotificationInformationListener } returns mockNotificationInformationListener

        val testNotificationInformation = NotificationInformation("testCampaignId")

        val silentNotificationInformationCommand = SilentNotificationInformationCommand(
            mockSilentNotificationInformationListenerProvider,
            testNotificationInformation
        )

        silentNotificationInformationCommand.run()

        verify {
            mockSilentNotificationInformationListenerProvider.silentNotificationInformationListener
            mockNotificationInformationListener.onNotificationInformationReceived(
                testNotificationInformation
            )
        }
    }
}