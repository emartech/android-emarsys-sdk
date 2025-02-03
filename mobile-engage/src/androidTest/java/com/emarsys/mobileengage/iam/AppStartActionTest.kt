package com.emarsys.mobileengage.iam


import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.mobileengage.di.tearDownMobileEngageComponent
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.mobileengage.util.waitForTask
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class AppStartActionTest  {

    private lateinit var mockEventServiceInternal: EventServiceInternal
    private lateinit var mockContactTokenStorage: Storage<String?>
    private lateinit var startAction: AppStartAction

    @Before
    fun setUp() {
        mockEventServiceInternal = mockk(relaxed = true)
        mockContactTokenStorage = mockk(relaxed = true)

        setupMobileEngageComponent(FakeMobileEngageDependencyContainer())

        startAction = AppStartAction(mockEventServiceInternal, mockContactTokenStorage)
    }

    @After
    fun tearDown() {
        tearDownMobileEngageComponent()
    }

    @Test
    fun testExecute_callsEventServiceInternal_whenContactTokenIsPresent() {
        every { mockContactTokenStorage.get() } returns "contactToken"

        startAction.execute(null)
        waitForTask()

        verify { (mockEventServiceInternal).trackInternalCustomEventAsync("app:start", null, null) }
    }

    @Test
    fun testExecute_EventServiceInternal_shouldNotBeCalled_whenContactTokenIsNotPresent() {
        every { mockContactTokenStorage.get() } returns null

        startAction.execute(null)
        waitForTask()

        verify(exactly = 0) {
            mockEventServiceInternal.trackInternalCustomEvent(
                any(),
                any(),
                any()
            )
        }
        verify(exactly = 0) {
            mockEventServiceInternal.trackInternalCustomEventAsync(
                any(),
                any(),
                any()
            )
        }
        verify(exactly = 0) { mockEventServiceInternal.trackCustomEvent(any(), any(), any()) }
        verify(exactly = 0) { mockEventServiceInternal.trackCustomEventAsync(any(), any(), any()) }

    }
}