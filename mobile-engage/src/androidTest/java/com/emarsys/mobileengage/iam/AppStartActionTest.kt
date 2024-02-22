package com.emarsys.mobileengage.iam


import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.mobileengage.di.tearDownMobileEngageComponent
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.mobileengage.util.waitForTask
import com.emarsys.testUtil.mockito.whenever
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class AppStartActionTest {

    private lateinit var mockEventServiceInternal: EventServiceInternal
    private lateinit var mockContactTokenStorage: Storage<String?>
    private lateinit var startAction: AppStartAction


    @BeforeEach
    fun setUp() {
        mockEventServiceInternal = mock()
        mockContactTokenStorage = mock()

        setupMobileEngageComponent(FakeMobileEngageDependencyContainer())

        startAction = AppStartAction(mockEventServiceInternal, mockContactTokenStorage)
    }

    @AfterEach
    fun tearDown() {
        tearDownMobileEngageComponent()
    }

    @Test
    fun testExecute_callsEventServiceInternal_whenContactTokenIsPresent() {
        whenever(mockContactTokenStorage.get()).thenReturn("contactToken")

        startAction.execute(null)
        waitForTask()

        verify(mockEventServiceInternal).trackInternalCustomEventAsync("app:start", null, null)
    }

    @Test
    fun testExecute_EventServiceInternal_shouldNotBeCalled_whenContactTokenIsNotPresent() {
        whenever(mockContactTokenStorage.get()).thenReturn(null)

        startAction.execute(null)
        waitForTask()

        verifyNoInteractions(mockEventServiceInternal)
    }
}