package com.emarsys.core.activity

import android.app.Activity
import android.os.Bundle
import com.emarsys.core.provider.Property
import com.emarsys.core.provider.activity.CurrentActivityProvider
import com.emarsys.core.provider.activity.FallbackActivityProvider
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class CurrentActivityWatchdogTest {
    private lateinit var watchdog: CurrentActivityWatchdog
    private lateinit var activity: Activity
    private lateinit var nextActivity: Activity
    private lateinit var activityProvider: Property<Activity?>


    @BeforeEach
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
        activityProvider =
            CurrentActivityProvider(fallbackActivityProvider = FallbackActivityProvider())
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
        verifyNoInteractions(activityProvider)
    }
}