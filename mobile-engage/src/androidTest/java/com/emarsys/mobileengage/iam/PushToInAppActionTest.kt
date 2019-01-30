package com.emarsys.mobileengage.iam

import com.emarsys.core.provider.timestamp.TimestampProvider
import org.junit.Test
import org.mockito.Mockito.mock

class PushToInAppActionTest {

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_inAppPresenter_mustNotBeNull() {
        PushToInAppAction(null, "", "", TimestampProvider())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_campaignId_mustNotBeNull() {
        PushToInAppAction(mock(InAppPresenter::class.java), null, "", mock(TimestampProvider::class.java))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_html_mustNotBeNull() {
        PushToInAppAction(mock(InAppPresenter::class.java), "", null, mock(TimestampProvider::class.java))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_timestampProvider_mustNotBeNull() {
        PushToInAppAction(mock(InAppPresenter::class.java), "", "", null)
    }
}