package com.emarsys.mobileengage.notification.command

import com.emarsys.mobileengage.api.push.NotificationInformation
import com.emarsys.mobileengage.api.push.NotificationInformationListener
import com.emarsys.mobileengage.push.SilentNotificationInformationListenerProvider
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class SilentNotificationInformationCommandTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Test
    fun testRun() {
        val mockNotificationInformationListener: NotificationInformationListener = mock()
        val mockSilentNotificationInformationListenerProvider: SilentNotificationInformationListenerProvider = mock {
            on { silentNotificationInformationListener } doReturn mockNotificationInformationListener
        }
        val testNotificationInformation = NotificationInformation("testCampaignId")

        val silentNotificationInformationCommand =  SilentNotificationInformationCommand(mockSilentNotificationInformationListenerProvider, testNotificationInformation)

        silentNotificationInformationCommand.run()

        verify(mockSilentNotificationInformationListenerProvider).silentNotificationInformationListener
        verify(mockNotificationInformationListener).onNotificationInformationReceived(testNotificationInformation)
    }
}