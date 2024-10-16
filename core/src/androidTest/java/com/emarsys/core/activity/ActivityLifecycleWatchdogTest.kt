package com.emarsys.core.activity

import android.app.Activity
import com.emarsys.getCurrentActivity
import com.emarsys.testUtil.AnnotationSpec
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.util.concurrent.CountDownLatch

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityLifecycleWatchdogTest : AnnotationSpec() {
    private lateinit var watchdog: ActivityLifecycleWatchdog
    private lateinit var mockRegistry: ActivityLifecycleActionRegistry
    private lateinit var mockActivity: Activity

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockkStatic("com.emarsys.EmarsysSdkInitializerKt")
        mockRegistry = mockk(relaxed = true)
        mockActivity = mockk(relaxed = true)

        watchdog = ActivityLifecycleWatchdog(mockRegistry)
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun testOnCreate_shouldInvokeRegistry_withCreateLifecycle() {
        watchdog.onActivityCreated(mockActivity, null)

        verify {
            mockRegistry.execute(
                mockActivity,
                listOf(ActivityLifecycleAction.ActivityLifecycle.CREATE)
            )
        }
    }

    @Test
    fun testOnResume_shouldInvokeRegistry_withResumeLifecycle() {
        watchdog.onActivityResumed(mockActivity)

        verify {
            mockRegistry.execute(
                mockActivity,
                listOf(ActivityLifecycleAction.ActivityLifecycle.RESUME)
            )
        }
    }

    @Test
    fun testInit_shouldInvokeRegistry_withCreateAndResumeLifecycle() = runTest {
        val latch = CountDownLatch(2)
        coEvery { getCurrentActivity() } returns mockActivity
        every { mockRegistry.execute(any(), any()) } answers {
            latch.countDown()
            nothing
        }

        ActivityLifecycleWatchdog(mockRegistry)

        latch.await()

        verify(exactly = 1) {
            mockRegistry.execute(
                mockActivity,
                listOf(
                    ActivityLifecycleAction.ActivityLifecycle.CREATE
                )
            )
        }

        verify(exactly = 1) {
            mockRegistry.execute(
                mockActivity,
                listOf(
                    ActivityLifecycleAction.ActivityLifecycle.RESUME
                )
            )
        }
    }
}