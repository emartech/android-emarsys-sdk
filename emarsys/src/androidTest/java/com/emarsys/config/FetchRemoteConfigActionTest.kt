package com.emarsys.config

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.fake.FakeEmarsysDependencyContainer
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.mobileengage.di.tearDownMobileEngageComponent


import com.emarsys.testUtil.AnnotationSpec
import org.mockito.Mockito.verify
import org.mockito.kotlin.mock
import org.mockito.kotlin.timeout
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever

class FetchRemoteConfigActionTest : AnnotationSpec() {

    private lateinit var fetchAction: FetchRemoteConfigAction
    private lateinit var mockConfigInternal: ConfigInternal
    private lateinit var mockCompletionListener: CompletionListener


    @Before
    fun setup() {
        mockConfigInternal = mock()
        mockCompletionListener = mock()

        setupMobileEngageComponent(FakeEmarsysDependencyContainer())

        fetchAction =
            FetchRemoteConfigAction(mockConfigInternal, completionListener = mockCompletionListener)
    }

    @After
    fun tearDown() {
        tearDownMobileEngageComponent()
    }

    @Test
    fun testExecute_invokesConfigInternalsRefreshRemoteConfigMethod() {
        fetchAction.execute(null)
        verify(mockConfigInternal, timeout(100)).refreshRemoteConfig(mockCompletionListener)
    }

    @Test
    fun testExecute_shouldNotInvokeRefreshConfig_when_ApplicationCode_is_Invalid() {
        whenever(mockConfigInternal.applicationCode).thenReturn("Nil")

        fetchAction.execute(null)

        verify(mockConfigInternal, times(0)).refreshRemoteConfig(mockCompletionListener)
    }

}