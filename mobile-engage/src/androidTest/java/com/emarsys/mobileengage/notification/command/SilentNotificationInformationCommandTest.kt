package com.emarsys.mobileengage.notification.command

import com.emarsys.mobileengage.api.push.NotificationInformation
import com.emarsys.mobileengage.api.push.NotificationInformationListener
import com.emarsys.mobileengage.push.SilentNotificationInformationListenerProvider
import com.emarsys.testUtil.TimeoutUtils
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

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