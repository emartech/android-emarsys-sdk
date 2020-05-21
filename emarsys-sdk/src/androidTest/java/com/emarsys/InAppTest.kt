package com.emarsys

import com.emarsys.core.di.DependencyInjection
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.inapp.InApp
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.iam.InAppInternal
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class InAppTest {
    private lateinit var inApp: InApp
    private lateinit var mockInAppInternal: InAppInternal

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockInAppInternal = mock()
        val dependencyContainer = FakeDependencyContainer(inAppInternal = mockInAppInternal)

        DependencyInjection.setup(dependencyContainer)
        inApp = InApp()
    }

    @After
    fun tearDown() {
        DependencyInjection.tearDown()
    }

    @Test
    fun testInApp_pause_delegatesToInternal() {
        inApp.pause()
        verify(mockInAppInternal).pause()
    }

    @Test
    fun testInApp_resume_delegatesToInternal() {
        inApp.resume()
        verify(mockInAppInternal).resume()
    }

    @Test
    fun testInApp_isPaused_delegatesToInternal() {
        whenever(mockInAppInternal.isPaused).thenReturn(true)
        val result = inApp.isPaused
        verify(mockInAppInternal).isPaused
        result shouldBe true
    }

    @Test
    fun testInApp_setEventHandler_delegatesToInternal() {
        val eventHandler: EventHandler = mock()
        inApp.setEventHandler(eventHandler)
        verify(mockInAppInternal).eventHandler = eventHandler
    }
}