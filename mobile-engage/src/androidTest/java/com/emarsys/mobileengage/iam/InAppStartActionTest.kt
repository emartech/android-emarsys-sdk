package com.emarsys.mobileengage.iam

import com.emarsys.core.concurrency.CoreSdkHandlerProvider
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.mobileengage.util.waitForTask
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class InAppStartActionTest {

    private lateinit var mockEventServiceInternal: EventServiceInternal
    private lateinit var mockContactTokenStorage: Storage<String?>
    private lateinit var startAction: InAppStartAction

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockEventServiceInternal = mock()
        mockContactTokenStorage = mock()

        DependencyInjection.setup(FakeMobileEngageDependencyContainer(coreSdkHandler = CoreSdkHandlerProvider().provideHandler()))

        startAction = InAppStartAction(mockEventServiceInternal, mockContactTokenStorage)
    }

    @After
    fun tearDown() {
        DependencyInjection.tearDown()
    }

    @Test
    fun testExecute_callsEventServiceInternal_whenContactTokenIsPresent() {
        whenever(mockContactTokenStorage.get()).thenReturn("contactToken")

        startAction.execute(null)
        waitForTask()

        verify(mockEventServiceInternal).trackInternalCustomEvent("app:start", null, null)
    }

    @Test
    fun testExecute_EventServiceInternal_shouldNotBeCalled_whenContactTokenIsNotPresent() {
        whenever(mockContactTokenStorage.get()).thenReturn(null)

        startAction.execute(null)
        waitForTask()

        verifyZeroInteractions(mockEventServiceInternal)
    }
}