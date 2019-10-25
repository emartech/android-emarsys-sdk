package com.emarsys.mobileengage.iam

import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.testUtil.TimeoutUtils
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock

class PushToInAppActionTest {
    
    companion object {
        val mockTimestampProvider: TimestampProvider = mock(TimestampProvider::class.java)
    }

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_inAppPresenter_mustNotBeNull() {
        PushToInAppAction(null, "", "", "","", mockTimestampProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_campaignId_mustNotBeNull() {
        PushToInAppAction(mock(InAppPresenter::class.java), null, "", "","", mockTimestampProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_html_mustNotBeNull() {
        PushToInAppAction(mock(InAppPresenter::class.java), "", null, "","", mockTimestampProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_timestampProvider_mustNotBeNull() {
        PushToInAppAction(mock(InAppPresenter::class.java), "", "", "","", null)
    }
}