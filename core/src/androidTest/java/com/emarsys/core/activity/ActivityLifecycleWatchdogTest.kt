package com.emarsys.core.activity

import android.app.Activity
import android.os.Bundle
import com.emarsys.core.concurrency.CoreSdkHandlerProvider
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.testUtil.ReflectionTestUtils
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.util.concurrent.CountDownLatch

class ActivityLifecycleWatchdogTest {
    companion object {
        init {
            Mockito.mock(Activity::class.java)
        }
    }

    private lateinit var watchdog: ActivityLifecycleWatchdog
    private lateinit var applicationStartActions: Array<ActivityLifecycleAction>
    private lateinit var activityCreatedActions: Array<ActivityLifecycleAction>
    private lateinit var initializationActions: Array<ActivityLifecycleAction?>

    private lateinit var coreSdkHandler: CoreSdkHandler
    private lateinit var fakeAction: ActivityLifecycleAction

    private lateinit var activity1: Activity
    private lateinit var activity2: Activity
    private lateinit var activity3: Activity

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        coreSdkHandler = CoreSdkHandlerProvider().provideHandler()
        activity1 = Mockito.mock(Activity::class.java)
        activity2 = Mockito.mock(Activity::class.java)
        activity3 = Mockito.mock(Activity::class.java)
        applicationStartActions = initActions()
        activityCreatedActions = initActions()
        initializationActions = arrayOf(mock(), mock(), mock())
        fakeAction = object : ActivityLifecycleAction {
            override fun execute(activity: Activity?) {
                Thread.currentThread().name.startsWith("CoreSDKHandlerThread") shouldBe true
            }
        }
        watchdog = ActivityLifecycleWatchdog(
                applicationStartActions,
                activityCreatedActions,
                initializationActions,
                coreSdkHandler
        )
    }

    @Test
    fun testConstructor_ApplicationStartActions_createEmptyArrayIfNull() {
        val watchdog = ActivityLifecycleWatchdog(activityCreatedActions = activityCreatedActions, coreSdkHandler = coreSdkHandler)
        Assert.assertArrayEquals(arrayOf<ActivityLifecycleAction>(), watchdog.applicationStartActions)
    }

    @Test
    fun testConstructor_activityCreatedActions_createEmptyArrayIfNull() {
        val watchdog = ActivityLifecycleWatchdog(applicationStartActions = applicationStartActions, coreSdkHandler = coreSdkHandler)
        Assert.assertArrayEquals(arrayOf<ActivityLifecycleAction>(), watchdog.activityCreatedActions)
    }

    @Test
    fun testConstructor_initializationActions_createEmptyArrayIfNull() {
        val watchdog = ActivityLifecycleWatchdog(coreSdkHandler = coreSdkHandler)
        Assert.assertArrayEquals(arrayOf<ActivityLifecycleAction>(), watchdog.initializationActions)
    }

    @Test
    fun testConstructor_fieldsInitialized_withConstructorArguments() {
        val watchdog = ActivityLifecycleWatchdog(
                applicationStartActions,
                activityCreatedActions,
                initializationActions,
                coreSdkHandler
        )
        Assert.assertArrayEquals(applicationStartActions, watchdog.applicationStartActions)
        Assert.assertArrayEquals(activityCreatedActions, watchdog.activityCreatedActions)
        Assert.assertArrayEquals(initializationActions, watchdog.initializationActions)
    }

    @Test
    fun testApplicationStart_onResume_shouldInvokeActions() {
        watchdog.onActivityResumed(activity1)
        waitForCoreSdkHandler()

        verifyExecuteCalled(applicationStartActions, 1)
        initializationActions.filterNotNull().forEach {
            verify(it, times(1)).execute(ArgumentMatchers.any(Activity::class.java))
        }
    }

    @Test
    fun testApplicationStart_onResume_schedulesToCoreSDKHandler() {
        initializationActions = listOf(fakeAction).toTypedArray()

        watchdog = ActivityLifecycleWatchdog(applicationStartActions, activityCreatedActions, initializationActions, coreSdkHandler)

        watchdog.onActivityResumed(activity1)
    }

    @Test
    fun testApplicationStart_onCreated_schedulesToCoreSDKHandler() {
        activityCreatedActions = listOf(fakeAction).toTypedArray()

        watchdog = ActivityLifecycleWatchdog(applicationStartActions, activityCreatedActions, initializationActions, coreSdkHandler)

        watchdog.onActivityCreated(activity1, null)
    }

    @Test
    fun testApplicationStart_addTriggerOnActivityAction_schedulesToCoreSDKHandler() {
        ReflectionTestUtils.setInstanceField(watchdog, "currentActivity", activity1)
        watchdog.addTriggerOnActivityAction(fakeAction)
    }

    @Test
    fun testApplicationStart_testOtherCallbacks_shouldNotInvokeActions() {
        watchdog.onActivityCreated(activity1, Bundle())
        watchdog.onActivityStarted(activity1)
        watchdog.onActivityPaused(activity1)
        watchdog.onActivityStopped(activity1)
        watchdog.onActivitySaveInstanceState(activity1, Bundle())
        watchdog.onActivityDestroyed(activity1)
        verifyExecuteCalled(applicationStartActions, 0)
        initializationActions.filterNotNull().forEach {
            verify(it, times(0)).execute(ArgumentMatchers.any(Activity::class.java))
        }
    }

    @Test
    fun testApplicationStart_onResume_actionsShouldBeInvoked_onlyOnce_inTheSameSession() {
        watchdog.onActivityCreated(activity1, Bundle())
        watchdog.onActivityStarted(activity1)
        watchdog.onActivityResumed(activity1)
        watchdog.onActivityPaused(activity1)
        watchdog.onActivityCreated(activity2, Bundle())
        watchdog.onActivityStarted(activity2)
        watchdog.onActivityResumed(activity2)
        watchdog.onActivityStopped(activity1)
        watchdog.onActivityPaused(activity2)
        watchdog.onActivityCreated(activity3, Bundle())
        watchdog.onActivityStarted(activity3)
        watchdog.onActivityResumed(activity3)
        watchdog.onActivityStopped(activity2)
        verifyExecuteCalled(applicationStartActions, 1)
        initializationActions.filterNotNull().forEach {
            verify(it, times(1)).execute(ArgumentMatchers.any(Activity::class.java))
        }
    }

    @Test
    fun testApplicationStart_onResume_actionsShouldBeInvoked_multipleTimes_forEachSession() {
        watchdog.onActivityCreated(activity1, Bundle())
        watchdog.onActivityStarted(activity1)
        watchdog.onActivityResumed(activity1)
        watchdog.onActivityPaused(activity1)
        watchdog.onActivityStopped(activity1)
        verifyExecuteCalled(applicationStartActions, 1)
        watchdog.onActivityCreated(activity2, Bundle())
        watchdog.onActivityStarted(activity2)
        watchdog.onActivityResumed(activity2)
        watchdog.onActivityPaused(activity2)
        watchdog.onActivityStopped(activity2)
        verifyExecuteCalled(applicationStartActions, 2)
        initializationActions.filterNotNull().forEach {
            verify(it, times(1)).execute(ArgumentMatchers.any(Activity::class.java))
        }
    }

    @Test
    fun testApplicationStart_onResume_actionsShouldBeInvoked_multipleTimes_forEachSession_withActivityTransitions() {
        watchdog.onActivityCreated(activity1, Bundle())
        watchdog.onActivityStarted(activity1)
        watchdog.onActivityResumed(activity1)
        watchdog.onActivityPaused(activity1)
        watchdog.onActivityCreated(activity2, Bundle())
        watchdog.onActivityStarted(activity2)
        watchdog.onActivityResumed(activity2)
        watchdog.onActivityStopped(activity1)
        watchdog.onActivityPaused(activity2)
        watchdog.onActivityStopped(activity2)
        verifyExecuteCalled(applicationStartActions, 1)
        watchdog.onActivityCreated(activity3, Bundle())
        watchdog.onActivityStarted(activity3)
        watchdog.onActivityResumed(activity3)
        verifyExecuteCalled(applicationStartActions, 2)
        initializationActions.filterNotNull().forEach {
            verify(it, times(1)).execute(ArgumentMatchers.any(Activity::class.java))
        }
    }

    @Test
    fun testActivityCreated_onActivityCreated_shouldInvokeActions() {
        watchdog.onActivityCreated(activity1, Bundle())
        verifyExecuteCalled(activityCreatedActions, 1)
        watchdog.onActivityCreated(activity2, Bundle())
        verifyExecuteCalled(activityCreatedActions, 2)
        watchdog.onActivityCreated(activity3, Bundle())
        verifyExecuteCalled(activityCreatedActions, 3)
    }

    @Test
    fun testActivityCreated_testOtherCallbacks_shouldNotInvokeActions() {
        watchdog.onActivityStarted(activity1)
        watchdog.onActivityResumed(activity1)
        watchdog.onActivityPaused(activity1)
        watchdog.onActivityStopped(activity1)
        watchdog.onActivitySaveInstanceState(activity1, Bundle())
        watchdog.onActivityDestroyed(activity1)
        verifyExecuteCalled(activityCreatedActions, 0)
    }

    @Test
    fun testTriggerOnActivity_shouldTriggerTheActionOnNewActivity_whenThereIsNoCurrentActivity() {
        val action = Mockito.mock(ActivityLifecycleAction::class.java)
        watchdog.addTriggerOnActivityAction(action)
        watchdog.onActivityCreated(activity1, Bundle())
        watchdog.onActivityStarted(activity1)
        watchdog.onActivityResumed(activity1)
        waitForCoreSdkHandler()
        Mockito.verify(action).execute(activity1)
    }

    @Test
    fun testTriggerOnActivity_shouldTriggerTheActionOnTheCurrentActivity() {
        val action = Mockito.mock(ActivityLifecycleAction::class.java)
        watchdog.onActivityCreated(activity1, Bundle())
        watchdog.onActivityStarted(activity1)
        watchdog.onActivityResumed(activity1)
        Mockito.verifyZeroInteractions(action)
        watchdog.addTriggerOnActivityAction(action)
        waitForCoreSdkHandler()
        Mockito.verify(action).execute(activity1)
    }

    @Test
    fun testTriggerOnActivity_shouldRemoveTriggeredActions() {
        val action = Mockito.mock(ActivityLifecycleAction::class.java)
        watchdog.onActivityCreated(activity1, Bundle())
        watchdog.onActivityStarted(activity1)
        watchdog.onActivityResumed(activity1)
        Mockito.verifyZeroInteractions(action)
        watchdog.addTriggerOnActivityAction(action)
        waitForCoreSdkHandler()
        Mockito.verify(action).execute(activity1)
        watchdog.triggerOnActivityActions.size shouldBe 0
    }

    private fun initActions(): Array<ActivityLifecycleAction> {
        val size = 5
        val activityLifecycleActions = arrayOfNulls<ActivityLifecycleAction>(size)
        for (i in 0..4) {
            activityLifecycleActions[i] = Mockito.mock(ActivityLifecycleAction::class.java)
        }
        return activityLifecycleActions.mapNotNull { it }.toTypedArray()
    }

    private fun verifyExecuteCalled(actions: Array<ActivityLifecycleAction>, times: Int) {
        waitForCoreSdkHandler()
        for (action in actions) {
            Mockito.verify(action, Mockito.times(times)).execute(ArgumentMatchers.any(Activity::class.java))
        }
    }

    private fun waitForCoreSdkHandler() {
        val latch = CountDownLatch(1)
        coreSdkHandler.post {
            latch.countDown()
        }
        latch.await()
    }

}