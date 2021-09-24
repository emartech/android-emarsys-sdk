package com.emarsys.core.activity

import android.app.Activity
import com.emarsys.core.activity.ActivityLifecycleAction.ActivityLifecycle.CREATE
import com.emarsys.core.activity.ActivityLifecycleAction.ActivityLifecycle.RESUME
import com.emarsys.core.concurrency.CoreSdkHandlerProvider
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.core.provider.activity.CurrentActivityProvider
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.util.concurrent.CountDownLatch

class ActivityLifecycleActionRegistryTest {

    private lateinit var activityLifecycleActionRegistry: ActivityLifecycleActionRegistry
    private lateinit var coreSdkHandler: CoreSdkHandler
    private lateinit var mockCurrentActivityProvider: CurrentActivityProvider
    private lateinit var mockAction1: ActivityLifecycleAction
    private lateinit var mockAction2: ActivityLifecycleAction
    private lateinit var mockAction3: ActivityLifecycleAction
    private lateinit var mockActions: MutableList<ActivityLifecycleAction>
    private lateinit var mockActivity: Activity

    @Before
    fun setup() {
        mockActivity = mock()
        mockAction1 = mock {
            on { triggeringLifecycle } doReturn RESUME
        }
        mockAction2 = mock {
            on { triggeringLifecycle } doReturn RESUME
        }
        mockAction3 = mock {
            on { triggeringLifecycle } doReturn RESUME
        }
        mockCurrentActivityProvider = mock {
            on { get() } doReturn mockActivity
        }
        mockActions = mutableListOf(mockAction1, mockAction2, mockAction3)
        coreSdkHandler = CoreSdkHandlerProvider().provideHandler()

        activityLifecycleActionRegistry = ActivityLifecycleActionRegistry(
            coreSdkHandler, mockCurrentActivityProvider, mockActions
        )
    }

    @Test
    fun testConstructor_createEmptyArrays() {
        activityLifecycleActionRegistry.triggerOnActivityActions.size shouldBe 0
        activityLifecycleActionRegistry.lifecycleActions.size shouldBe mockActions.size
    }

    @Test
    fun testConstructor_createEmptyArraysByDefault() {
        val emptyActivityLifecycleActionRegistry = ActivityLifecycleActionRegistry(coreSdkHandler, mockCurrentActivityProvider)
        emptyActivityLifecycleActionRegistry.triggerOnActivityActions.size shouldBe 0
        emptyActivityLifecycleActionRegistry.lifecycleActions.size shouldBe 0
    }

    @Test
    fun testExecute_shouldExecuteInputActions() {
        activityLifecycleActionRegistry.execute(mockActivity, listOf(RESUME))

        waitForCoreSDKThread()
        verify(mockAction1).execute(mockActivity)
        verify(mockAction2).execute(mockActivity)
        verify(mockAction3).execute(mockActivity)
    }

    @Test
    fun testExecute_shouldExecuteInputActionsCorrectNumberOfTimes() {
        whenever(mockAction1.repeatable).doReturn(false)
        whenever(mockAction2.repeatable).doReturn(true)
        whenever(mockAction3.repeatable).doReturn(false)

        activityLifecycleActionRegistry.execute(mock(), listOf(RESUME))
        activityLifecycleActionRegistry.execute(mock(), listOf(RESUME))

        waitForCoreSDKThread()
        verify(mockAction1, times(1)).execute(any())
        verify(mockAction2, times(2)).execute(any())
        verify(mockAction3, times(1)).execute(any())

        mockActions.contains(mockAction1) shouldBe false
        mockActions.contains(mockAction2) shouldBe true
        mockActions.contains(mockAction3) shouldBe false
    }

    @Test
    fun testExecute_shouldExecuteInputActionsInCorrectOrder() {
        whenever(mockAction1.priority).doReturn(1)
        whenever(mockAction2.priority).doReturn(65)
        whenever(mockAction3.priority).doReturn(10)

        activityLifecycleActionRegistry.execute(mock(), listOf(RESUME))
        waitForCoreSDKThread()

        inOrder(mockAction1, mockAction2, mockAction3).apply {
            verify(mockAction1).execute(any())
            verify(mockAction3).execute(any())
            verify(mockAction2).execute(any())
        }
    }

    @Test
    fun testExecute_shouldExecuteInputActionsInCorrectOrder_whenDifferentLifecycle() {
        val mockAction4 = mock<ActivityLifecycleAction> {
            on { priority } doReturn 100
            on { triggeringLifecycle } doReturn CREATE
        }
        mockActions.add(mockAction4)
        whenever(mockAction1.priority).doReturn(1)
        whenever(mockAction2.priority).doReturn(65)
        whenever(mockAction3.priority).doReturn(10)

        activityLifecycleActionRegistry.execute(mock(), listOf(RESUME, CREATE))
        waitForCoreSDKThread()

        inOrder(mockAction1, mockAction2, mockAction3, mockAction4).apply {
            verify(mockAction4).execute(any())
            verify(mockAction1).execute(any())
            verify(mockAction3).execute(any())
            verify(mockAction2).execute(any())
        }
    }

    @Test
    fun testExecute_shouldRunAppropriateActions() {
        whenever(mockAction1.triggeringLifecycle).doReturn(CREATE)
        whenever(mockAction2.triggeringLifecycle).doReturn(RESUME)
        whenever(mockAction3.triggeringLifecycle).doReturn(CREATE)

        activityLifecycleActionRegistry.execute(mock(), listOf(CREATE))

        waitForCoreSDKThread()
        verify(mockAction1).execute(any())
        verify(mockAction2, times(0)).execute(any())
        verify(mockAction3).execute(any())
    }

    @Test
    fun testExecute_shouldExecuteAppropriateActionsFromList() {
        whenever(mockAction1.triggeringLifecycle).doReturn(CREATE)
        whenever(mockAction2.triggeringLifecycle).doReturn(RESUME)
        whenever(mockAction3.triggeringLifecycle).doReturn(CREATE)

        activityLifecycleActionRegistry.execute(mock(), listOf(CREATE, RESUME))

        waitForCoreSDKThread()
        verify(mockAction1).execute(any())
        verify(mockAction2).execute(any())
        verify(mockAction3).execute(any())
    }

    @Test
    fun testExecute_shouldExecuteInputActionsInCorrectOrder_whenOnActivityActionsArePresent() {
        val mockAction4 = mock<ActivityLifecycleAction> {
            on { priority } doReturn 900
            on { triggeringLifecycle } doReturn RESUME
        }.also { activityLifecycleActionRegistry.triggerOnActivityActions.add(it) }
        val mockAction5 = mock<ActivityLifecycleAction> {
            on { priority } doReturn 900
            on { triggeringLifecycle } doReturn RESUME
        }.also { activityLifecycleActionRegistry.triggerOnActivityActions.add(it) }

        whenever(mockAction1.priority).doReturn(1)
        whenever(mockAction2.priority).doReturn(500)
        whenever(mockAction3.priority).doReturn(10)

        activityLifecycleActionRegistry.execute(mock(), listOf(RESUME))

        waitForCoreSDKThread()
        inOrder(mockAction1, mockAction2, mockAction3, mockAction4, mockAction5).apply {
            verify(mockAction1).execute(any())
            verify(mockAction3).execute(any())
            verify(mockAction2).execute(any())
            verify(mockAction4).execute(any())
            verify(mockAction5).execute(any())
        }
    }

    @Test
    fun testAddTriggerOnActivityAction_shouldTrigger_whenActivityIsPresent() {
        val latch = CountDownLatch(1)

        whenever(mockAction1.execute(any())).doAnswer {
            latch.countDown()
        }

        activityLifecycleActionRegistry.addTriggerOnActivityAction(mockAction1)
        latch.await()

        verify(mockAction1).execute(mockActivity)
    }

    @Test
    fun testAddTriggerOnActivityAction_shouldAddActionToTriggerOnActivityList_whenActivityIsNotPresent() {
        whenever(mockCurrentActivityProvider.get()).doReturn(null)

        activityLifecycleActionRegistry.addTriggerOnActivityAction(mockAction1)

        waitForCoreSDKThread()

        activityLifecycleActionRegistry.triggerOnActivityActions.size shouldBe 1
        activityLifecycleActionRegistry.triggerOnActivityActions[0] shouldBe mockAction1
        verify(mockAction1, times(0)).execute(any())
    }

    @Test
    fun testAddTriggerOnActivityAction_shouldTriggerOnActivityList_whenActivityIsNotPresent_thenPresent() {
        whenever(mockCurrentActivityProvider.get()).doReturn(null)

        activityLifecycleActionRegistry.addTriggerOnActivityAction(mockAction1)

        whenever(mockCurrentActivityProvider.get()).doReturn(mockActivity)

        activityLifecycleActionRegistry.addTriggerOnActivityAction(mockAction2)

        waitForCoreSDKThread()

        activityLifecycleActionRegistry.triggerOnActivityActions.size shouldBe 0
        inOrder(mockAction1, mockAction2).apply {
            verify(mockAction1).execute(any())
            verify(mockAction2).execute(any())
        }
    }

    @Test
    fun testAddTriggerOnActivity_shouldBeDelegatedToTheCoreThread() {
        whenever(mockAction1.execute(any())).doAnswer {
            Thread.currentThread().name.startsWith("CoreSDKHandlerThread") shouldBe true
        }

        activityLifecycleActionRegistry.addTriggerOnActivityAction(mockAction1)
    }

    @Test
    fun testActivityLifecycleActionRegistryExecute_shouldBeDelegatedToTheCoreThread() {
        whenever(mockAction1.execute(any())).doAnswer {
            Thread.currentThread().name.startsWith("CoreSDKHandlerThread") shouldBe true
        }

        activityLifecycleActionRegistry.execute(mockActivity, listOf(RESUME))
    }

    private fun waitForCoreSDKThread() {
        val latch = CountDownLatch(1)
        coreSdkHandler.post {
            latch.countDown()
        }
        latch.await()
    }
}