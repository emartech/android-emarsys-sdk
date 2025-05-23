package com.emarsys.deeplink


import android.app.Activity
import android.content.Intent
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.di.setupEmarsysComponent
import com.emarsys.mobileengage.deeplink.DeepLinkInternal
import com.emarsys.testUtil.IntegrationTestUtils
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class DeepLinkTest  {
    private lateinit var mockActivity: Activity
    private lateinit var mockCompletionListener: CompletionListener
    private lateinit var mockDeepLinkInternal: DeepLinkInternal
    private lateinit var deeplinkApi: DeepLink

    @Before
    fun setUp() {
        mockActivity = mockk(relaxed = true)
        mockCompletionListener = mockk(relaxed = true)
        mockDeepLinkInternal = mockk(relaxed = true)
        deeplinkApi = DeepLink()

        setupEmarsysComponent(FakeDependencyContainer(
                deepLinkInternal = mockDeepLinkInternal))
    }

    @After
    fun tearDown() {
        IntegrationTestUtils.tearDownEmarsys()
    }

    @Test
    fun testDeepLinkApi_delegatesToInternal() {
        val intent = Intent()
        deeplinkApi.trackDeepLinkOpen(mockActivity, intent, mockCompletionListener)
        verify { mockDeepLinkInternal.trackDeepLinkOpen(mockActivity, intent, mockCompletionListener) }
    }
}