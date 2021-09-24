package com.emarsys.core.activity

import android.app.Activity
import com.emarsys.core.concurrency.CoreSdkHandlerProvider
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.testUtil.TimeoutUtils
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class ActivityLifecycleWatchdogTest {
    private lateinit var watchdog: ActivityLifecycleWatchdog
    private lateinit var mockRegistry: ActivityLifecycleActionRegistry
    private lateinit var mockActivity: Activity

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
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