package com.emarsys.config

import android.app.Activity
import com.emarsys.config.model.RemoteConfig
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.anyNotNull
import com.emarsys.testUtil.mockito.whenever
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
    fun testExecute_invokesConfigInternalsFetchRemoteConfigMethod() {
        fetchAction.execute(mock(Activity::class.java))

        verify(mockConfigInternal).fetchRemoteConfig(anyNotNull())
    }

    @Test
    fun testExecute_verifyApplyRemoteConfigCalled_onSuccess() {
        val expectedRemoteConfig = RemoteConfig(eventServiceUrl = "https://test.emarsys.com")
        whenever(mockConfigInternal.fetchRemoteConfig(anyNotNull())).thenAnswer {
            val result: Try<RemoteConfig> = Try.success(expectedRemoteConfig)
            (it.arguments[0] as ResultListener<Try<RemoteConfig>>).onResult(result)
        }

        fetchAction.execute(mock(Activity::class.java))
        verify(mockConfigInternal).applyRemoteConfig(expectedRemoteConfig)
    }

    @Test
    fun testExecute_verifyApplyRemoteConfigCalled_onFailure() {
        val expectedException: Exception = mock(Exception::class.java)
        whenever(mockConfigInternal.fetchRemoteConfig(anyNotNull())).thenAnswer {
            val result = Try.failure<Exception>(expectedException)
            (it.arguments[0] as ResultListener<Try<Exception>>).onResult(result)
        }

        fetchAction.execute(mock(Activity::class.java))
        verify(mockConfigInternal).applyRemoteConfig(RemoteConfig())
    }
}