package com.emarsys.mobileengage.iam


import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.mobileengage.di.tearDownMobileEngageComponent
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.mobileengage.util.waitForTask
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyZeroInteractions

class AppStartActionTest {

    private lateinit var mockEventServiceInternal: EventServiceInternal
    private lateinit var mockContactTokenStorage: Storage<String?>
    private lateinit var startAction: AppStartAction

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockEventServiceInternal = mock()
        mockContactTokenStorage = mock()

        setupMobileEngageComponent(FakeMobileEngageDependencyContainer())

        startAction = AppStartAction(mockEventServiceInternal, mockContactTokenStorage)
    }

    @After
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

        verifyZeroInteractions(mockEventServiceInternal)
    }
}