package com.emarsys.mobileengage.deeplink

import android.app.Activity
import android.content.Intent
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.mobileengage.di.tearDownMobileEngageComponent
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.mobileengage.util.waitForTask
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class DeepLinkActionTest  {
    private lateinit var mockDeepLinkInternal: DeepLinkInternal
    private lateinit var action: DeepLinkAction

    @Before
    fun setUp() {

        setupMobileEngageComponent(FakeMobileEngageDependencyContainer())

        mockDeepLinkInternal = mockk(relaxed = true)
        action = DeepLinkAction(mockDeepLinkInternal)
    }

    @After
    fun tearDown() {
        tearDownMobileEngageComponent()
    }

    @Test
    fun testExecute_callsMobileEngageInternal() {
        val intent: Intent = mockk(relaxed = true)
        val mockActivity: Activity = mockk(relaxed = true)
        every { mockActivity.intent } returns intent

        action.execute(mockActivity)

        verify { mockDeepLinkInternal.trackDeepLinkOpen(mockActivity, intent, null) }
    }

    @Test
    fun testExecute_neverCallsMobileEngageInternal_whenIntentFromActivityIsNull() {
        val mockActivity: Activity = mockk(relaxed = true)
        every { mockActivity.intent } returns null

        action.execute(mockActivity)
        waitForTask()

        verify(exactly = 0) {
            (mockDeepLinkInternal.trackDeepLinkOpen(
                eq(mockActivity),
                any(),
                any()
            ))
        }
    }

    @Test
    fun testExecute_neverCallsMobileEngageInternal_whenActivityIsNull() {
        action.execute(null)
        waitForTask()

        verify(exactly = 0) { (mockDeepLinkInternal.trackDeepLinkOpen(any(), any(), any())) }
    }
}