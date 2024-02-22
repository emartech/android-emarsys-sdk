package com.emarsys.core.activity

import android.app.Activity
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class ActivityLifecycleWatchdogTest {
    private lateinit var watchdog: ActivityLifecycleWatchdog
    private lateinit var mockRegistry: ActivityLifecycleActionRegistry
    private lateinit var mockActivity: Activity


    @BeforeEach
    fun setUp() {
        mockRegistry = mock()
        mockActivity = mock()

        watchdog = ActivityLifecycleWatchdog(mockRegistry)
    }

    @Test
    fun testOnCreate_shouldInvokeRegistry_withCreateLifecycle() {
        watchdog.onActivityCreated(mockActivity, null)

        verify(mockRegistry).execute(
            mockActivity,
            listOf(ActivityLifecycleAction.ActivityLifecycle.CREATE)
        )
    }

    @Test
    fun testOnResume_shouldInvokeRegistry_withResumeLifecycle() {
        watchdog.onActivityResumed(mockActivity)

        verify(mockRegistry).execute(
            mockActivity,
            listOf(ActivityLifecycleAction.ActivityLifecycle.RESUME)
        )
    }
}