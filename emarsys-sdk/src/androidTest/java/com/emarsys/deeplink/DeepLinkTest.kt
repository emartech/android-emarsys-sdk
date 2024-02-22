package com.emarsys.deeplink

import android.app.Activity
import android.content.Intent
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.di.setupEmarsysComponent
import com.emarsys.mobileengage.deeplink.DeepLinkInternal
import com.emarsys.testUtil.IntegrationTestUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class DeepLinkTest {
    private lateinit var mockActivity: Activity
    private lateinit var mockCompletionListener: CompletionListener
    private lateinit var mockDeepLinkInternal: DeepLinkInternal
    private lateinit var deeplinkApi: DeepLink


    @BeforeEach
    fun setUp() {
        mockActivity = mock()
        mockCompletionListener = mock()
        mockDeepLinkInternal = mock()
        deeplinkApi = DeepLink()

        setupEmarsysComponent(FakeDependencyContainer(
                deepLinkInternal = mockDeepLinkInternal))
    }

    @AfterEach
    fun tearDown() {
        IntegrationTestUtils.tearDownEmarsys()
    }

    @Test
    fun testDeepLinkApi_delegatesToInternal() {
        val intent = Intent()
        deeplinkApi.trackDeepLinkOpen(mockActivity, intent, mockCompletionListener)
        verify(mockDeepLinkInternal).trackDeepLinkOpen(mockActivity, intent, mockCompletionListener)
    }
}