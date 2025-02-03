package com.emarsys.core.activity

import android.app.Activity
import com.emarsys.core.activity.ActivityLifecycleAction.ActivityLifecycle.CREATE
import com.emarsys.core.activity.ActivityLifecycleAction.ActivityLifecycle.RESUME
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.activity.CurrentActivityProvider
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch

class ActivityLifecycleActionRegistryTest  {

    private lateinit var activityLifecycleActionRegistry: ActivityLifecycleActionRegistry
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var mockCurrentActivityProvider: CurrentActivityProvider
    private lateinit var mockAction1: ActivityLifecycleAction
    private lateinit var mockAction2: ActivityLifecycleAction
    private lateinit var mockAction3: ActivityLifecycleAction
    private lateinit var mockActions: MutableList<ActivityLifecycleAction>
    private lateinit var mockActivity: Activity

    @Before
    fun setup() {
        mockActivity = mockk(relaxed = true)
        mockAction1 = mockk(relaxed = true)
        every { mockAction1.triggeringLifecycle } returns RESUME

        mockAction2 = mockk(relaxed = true)
        every { mockAction2.triggeringLifecycle } returns RESUME

        mockAction3 = mockk(relaxed = true)
        every { mockAction3.triggeringLifecycle } returns RESUME

        mockCurrentActivityProvider = mockk(relaxed = true)
        every { mockCurrentActivityProvider.get() } returns mockActivity

        mockActions = mutableListOf(mockAction1, mockAction2, mockAction3)
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()

        activityLifecycleActionRegistry = ActivityLifecycleActionRegistry(
            concurrentHandlerHolder, mockCurrentActivityProvider, mockActions
        )
    }

    @Test
    fun testConstructor_createEmptyArrays() {
        activityLifecycleActionRegistry.triggerOnActivityActions.size shouldBe 0
        activityLifecycleActionRegistry.lifecycleActions.size shouldBe mockActions.size
    }

    @Test
    fun testConstructor_createEmptyArraysByDefault() {
        val emptyActivityLifecycleActionRegistry =
            ActivityLifecycleActionRegistry(concurrentHandlerHolder, mockCurrentActivityProvider)
        emptyActivityLifecycleActionRegistry.triggerOnActivityActions.size shouldBe 0
        emptyActivityLifecycleActionRegistry.lifecycleActions.size shouldBe 0
    }

    @Test
    fun testExecute_shouldExecuteInputActions() {
        activityLifecycleActionRegistry.execute(mockActivity, listOf(RESUME))

        waitForCoreSDKThread()
        verify { mockAction1.execute(mockActivity) }
        verify { mockAction2.execute(mockActivity) }
        verify { mockAction3.execute(mockActivity) }
    }

    @Test
    fun testExecute_shouldExecuteInputActionsCorrectNumberOfTimes() {
        every { mockAction1.repeatable } returns false
        every { mockAction2.repeatable } returns true
        every { mockAction3.repeatable } returns false

        activityLifecycleActionRegistry.execute(mockk(relaxed = true), listOf(RESUME))
        activityLifecycleActionRegistry.execute(mockk(relaxed = true), listOf(RESUME))

        waitForCoreSDKThread()
        verify(exactly = 1) { mockAction1.execute(any()) }
        verify(exactly = 2) { mockAction2.execute(any()) }
        verify(exactly = 1) { mockAction3.execute(any()) }

        mockActions.contains(mockAction1) shouldBe false
        mockActions.contains(mockAction2) shouldBe true
        mockActions.contains(mockAction3) shouldBe false
    }

    @Test
    fun testExecute_shouldExecuteInputActionsInCorrectOrder() {
        every { mockAction1.priority } returns 1
        every { mockAction2.priority } returns 65
        every { mockAction3.priority } returns 10

        activityLifecycleActionRegistry.execute(mockk(relaxed = true), listOf(RESUME))
        waitForCoreSDKThread()

        verifyOrder {
            mockAction1.execute(any())
            mockAction3.execute(any())
            mockAction2.execute(any())
        }
    }

    @Test
    fun testExecute_shouldExecuteInputActionsInCorrectOrder_whenDifferentLifecycle() {
        val mockAction4: ActivityLifecycleAction = mockk(relaxed = true)
        every { mockAction4.priority } returns 100
        every { mockAction4.triggeringLifecycle } returns CREATE

        mockActions.add(mockAction4)
        every { mockAction1.priority } returns 1
        every { mockAction2.priority } returns 65
        every { mockAction3.priority } returns 10

        activityLifecycleActionRegistry.execute(mockk(relaxed = true), listOf(RESUME, CREATE))
        waitForCoreSDKThread()

        verifyOrder {
            mockAction4.execute(any())
            mockAction1.execute(any())
            mockAction3.execute(any())
            mockAction2.execute(any())
        }
    }

    @Test
    fun testExecute_shouldRunAppropriateActions() {
        every { mockAction1.triggeringLifecycle } returns CREATE
        every { mockAction2.triggeringLifecycle } returns RESUME
        every { mockAction3.triggeringLifecycle } returns CREATE

        activityLifecycleActionRegistry.execute(mockk(relaxed = true), listOf(CREATE))

        waitForCoreSDKThread()
        verify { mockAction1.execute(any()) }
        verify(exactly = 0) { mockAction2.execute(any()) }
        verify { mockAction3.execute(any()) }
    }

    @Test
    fun testExecute_shouldExecuteAppropriateActionsFromList() {
        every { mockAction1.triggeringLifecycle } returns CREATE
        every { mockAction2.triggeringLifecycle } returns RESUME
        every { mockAction3.triggeringLifecycle } returns CREATE

        activityLifecycleActionRegistry.execute(mockk(relaxed = true), listOf(CREATE, RESUME))

        waitForCoreSDKThread()
        verify { mockAction1.execute(any()) }
        verify { mockAction2.execute(any()) }
        verify { mockAction3.execute(any()) }
    }

    @Test
    fun testExecute_shouldExecuteInputActionsInCorrectOrder_whenOnActivityActionsArePresent() {
        val mockAction4: ActivityLifecycleAction = mockk(relaxed = true)
        every { mockAction4.priority } returns 900
        every { mockAction4.triggeringLifecycle } returns RESUME

        activityLifecycleActionRegistry.triggerOnActivityActions.add(mockAction4)
        val mockAction5: ActivityLifecycleAction = mockk(relaxed = true)
        every { mockAction5.priority } returns 900
        every { mockAction5.triggeringLifecycle } returns RESUME

        activityLifecycleActionRegistry.triggerOnActivityActions.add(mockAction5)

        every { mockAction1.priority } returns 1
        every { mockAction2.priority } returns 500
        every { mockAction3.priority } returns 10

        activityLifecycleActionRegistry.execute(mockk(relaxed = true), listOf(RESUME))

        waitForCoreSDKThread()
        verifyOrder {
            mockAction1.execute(any())
            mockAction3.execute(any())
            mockAction2.execute(any())
            mockAction4.execute(any())
            mockAction5.execute(any())
        }
    }

    @Test
    fun testAddTriggerOnActivityAction_shouldTrigger_whenActivityIsPresent() {
        val latch = CountDownLatch(1)

        every { mockAction1.execute(any()) } answers {
            latch.countDown()
        }

        activityLifecycleActionRegistry.addTriggerOnActivityAction(mockAction1)
        latch.await()

        verify { mockAction1.execute(mockActivity) }
    }

    @Test
    fun testAddTriggerOnActivityAction_shouldAddActionToTriggerOnActivityList_whenActivityIsNotPresent() {
        every { mockCurrentActivityProvider.get() } returns null

        activityLifecycleActionRegistry.addTriggerOnActivityAction(mockAction1)

        waitForCoreSDKThread()

        activityLifecycleActionRegistry.triggerOnActivityActions.size shouldBe 1
        activityLifecycleActionRegistry.triggerOnActivityActions[0] shouldBe mockAction1
        verify(exactly = 0) { mockAction1.execute(any()) }
    }

    @Test
    fun testAddTriggerOnActivityAction_shouldTriggerOnActivityList_whenActivityIsNotPresent_thenPresent() {
        every { mockCurrentActivityProvider.get() } returns null

        activityLifecycleActionRegistry.addTriggerOnActivityAction(mockAction1)

        every { mockCurrentActivityProvider.get() } returns mockActivity

        activityLifecycleActionRegistry.addTriggerOnActivityAction(mockAction2)

        waitForCoreSDKThread()

        activityLifecycleActionRegistry.triggerOnActivityActions.size shouldBe 0
        verifyOrder {
            mockAction1.execute(any())
            mockAction2.execute(any())
        }
    }

    @Test
    fun testAddTriggerOnActivity_shouldBeDelegatedToTheCoreThread() {
        every { mockAction1.execute(any()) } answers {
            Thread.currentThread().name.startsWith("CoreSDKHandlerThread") shouldBe true
            Unit
        }

        activityLifecycleActionRegistry.addTriggerOnActivityAction(mockAction1)
    }

    @Test
    fun testActivityLifecycleActionRegistryExecute_shouldBeDelegatedToTheCoreThread() {
        every { mockAction1.execute(any()) } answers {
            Thread.currentThread().name.startsWith("CoreSDKHandlerThread") shouldBe true
            Unit
        }

        activityLifecycleActionRegistry.execute(mockActivity, listOf(RESUME))
    }

    private fun waitForCoreSDKThread() {
        val latch = CountDownLatch(1)
        concurrentHandlerHolder.coreHandler.post {
            latch.countDown()
        }
        latch.await()
    }
}