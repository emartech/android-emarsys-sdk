package com.emarsys.core.activity

import com.emarsys.testUtil.TimeoutUtils.timeoutRule
import android.app.Activity
import android.os.Bundle
import com.emarsys.core.provider.Property
import com.emarsys.core.provider.activity.CurrentActivityProvider
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyZeroInteractions
import java.lang.IllegalArgumentException

class CurrentActivityWatchdogTest {
    private lateinit var watchdog: CurrentActivityWatchdog
    private lateinit var activity: Activity
    private lateinit var nextActivity: Activity
    private lateinit var activityProvider: Property<Activity?>

    @Rule
    @JvmField
    var timeout: TestRule = timeoutRule

    @Before
    fun setUp() {
        activityProvider = mock()
        watchdog = CurrentActivityWatchdog(activityProvider)
        activity = mock()
        nextActivity = mock()
    }

    @Test
    fun testGetCurrentActivity_shouldStoreTheActivity_whenCallingOnResumed() {
        watchdog.onActivityResumed(activity)
        verify(activityProvider).set(activity)
    }

    @Test
    fun testGetCurrentActivity_newerActivity_shouldOverride_thePrevious() {
        watchdog.onActivityResumed(activity)
        watchdog.onActivityResumed(nextActivity)
        watchdog.onActivityPaused(activity)
        inOrder(activityProvider).apply {
            verify(activityProvider).set(activity)
            verify(activityProvider).set(nextActivity)
        }
    }

    @Test
    fun testGetCurrentActivity_shouldReturnNull_whenCurrentActivityPauses_andThereIsNoNextActivity() {
        activityProvider = CurrentActivityProvider()
        watchdog = CurrentActivityWatchdog(activityProvider)
        watchdog.onActivityResumed(activity)
        watchdog.onActivityPaused(activity)
        activityProvider.get() shouldBe null
    }

    @Test
    fun testGetCurrentActivity_otherLifecycleCallbacks_shouldBeIgnored() {
        val bundle = Bundle()
        watchdog.onActivityCreated(activity, bundle)
        watchdog.onActivityStarted(activity)
        watchdog.onActivityStopped(activity)
        watchdog.onActivitySaveInstanceState(activity, bundle)
        watchdog.onActivityDestroyed(activity)
        verifyZeroInteractions(activityProvider)
    }
}