package com.emarsys.config

import com.emarsys.fake.FakeEmarsysDependencyContainer
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.mobileengage.di.tearDownMobileEngageComponent
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.timeout
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.verify

class FetchRemoteConfigActionTest {

    private lateinit var fetchAction: FetchRemoteConfigAction
    private lateinit var mockConfigInternal: ConfigInternal

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setup() {
        mockConfigInternal = mock()

        setupMobileEngageComponent(FakeEmarsysDependencyContainer())

        fetchAction = FetchRemoteConfigAction(mockConfigInternal)
    }

    @After
    fun tearDown() {
        tearDownMobileEngageComponent()
    }

    @Test
    fun testExecute_invokesConfigInternalsRefreshRemoteConfigMethod() {
        fetchAction.execute(null)
        verify(mockConfigInternal, timeout(100)).refreshRemoteConfig(null)
    }

}