package com.emarsys.mobileengage.iam

import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.*

class InAppStartActionTest {

    private lateinit var mockEventServiceInternal: EventServiceInternal
    private lateinit var mockContactTokenStorage: Storage<String>
    private lateinit var startAction: InAppStartAction

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun init() {
        mockEventServiceInternal = mock(EventServiceInternal::class.java)
        mockContactTokenStorage = mock(Storage::class.java) as Storage<String>

        startAction = InAppStartAction(mockEventServiceInternal, mockContactTokenStorage)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_eventServiceInternal_mustNotBeNull() {
        InAppStartAction(null, mockContactTokenStorage)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_contactTokenStorage_mustNotBeNull() {
        InAppStartAction(mockEventServiceInternal, null)
    }

    @Test
    fun testExecute_callsEventServiceInternal_whenContactTokenIsPresent() {
        whenever(mockContactTokenStorage.get()).thenReturn("contactToken")

        startAction.execute(null)

        verify(mockEventServiceInternal).trackInternalCustomEvent("app:start", null, null)
    }

    @Test
    fun testExecute_EventServiceInternal_shouldNotBeCalled_whenContactTokenIsNotPresent() {
        whenever(mockContactTokenStorage.get()).thenReturn(null)

        startAction.execute(null)

        verifyZeroInteractions(mockEventServiceInternal)
    }
}