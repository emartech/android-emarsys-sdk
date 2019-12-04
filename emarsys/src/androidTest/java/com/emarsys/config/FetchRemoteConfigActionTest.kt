package com.emarsys.config

import android.app.Activity
import com.emarsys.testUtil.TimeoutUtils
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class FetchRemoteConfigActionTest {

    private lateinit var fetchAction: FetchRemoteConfigAction
    private lateinit var mockConfigInternal: DefaultConfigInternal

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setup() {
        mockConfigInternal = mock(DefaultConfigInternal::class.java)

        fetchAction = FetchRemoteConfigAction(mockConfigInternal)
    }

    @Test
    fun testExecute_invokesConfigInternalsRefreshRemoteConfigMethod() {
        fetchAction.execute(mock(Activity::class.java))

        verify(mockConfigInternal).refreshRemoteConfig()
    }
}