package com.emarsys


import android.app.Application
import android.os.Looper
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.di.emarsys
import com.emarsys.di.setupEmarsysComponent
import com.emarsys.di.tearDownEmarsysComponent
import com.emarsys.inapp.InApp
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.iam.InAppInternal
import com.emarsys.testUtil.InstrumentationRegistry
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class InAppTest  {
    private lateinit var inApp: InApp
    private lateinit var mockInAppInternal: InAppInternal
    private val application: Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as Application


    @Before
    fun setUp() {
        mockInAppInternal = mockk(relaxed = true)
        val dependencyContainer = FakeDependencyContainer(inAppInternal = mockInAppInternal)

        setupEmarsysComponent(dependencyContainer)
        inApp = InApp()
    }

    @After
    fun tearDown() {
        application.unregisterActivityLifecycleCallbacks(mobileEngage().activityLifecycleWatchdog)
        application.unregisterActivityLifecycleCallbacks(emarsys().transitionSafeCurrentActivityWatchdog)
        try {
            val looper: Looper = emarsys().concurrentHandlerHolder.coreLooper
            looper.quitSafely()
            tearDownEmarsysComponent()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun testInApp_pause_delegatesToInternal() {
        inApp.pause()
        verify { mockInAppInternal.pause() }
    }

    @Test
    fun testInApp_resume_delegatesToInternal() {
        inApp.resume()
        verify { mockInAppInternal.resume() }
    }

    @Test
    fun testInApp_isPaused_delegatesToInternal() {
        every { mockInAppInternal.isPaused } returns true
        val result = inApp.isPaused
        verify { mockInAppInternal.isPaused }
        result shouldBe true
    }

    @Test
    fun testInApp_setEventHandler_delegatesToInternal() {
        val eventHandler: EventHandler = mockk(relaxed = true)
        inApp.setEventHandler(eventHandler)
        verify {mockInAppInternal.eventHandler = eventHandler }
    }
}