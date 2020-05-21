package com.emarsys.mobileengage.push

import com.emarsys.core.storage.Storage
import com.emarsys.core.storage.StringStorage
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock

class DefaultPushTokenProviderTest {
    private companion object {
        const val PUSH_TOKEN = "pushToken"
    }

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Test
    fun testProvidePushToken() {
        val mockPushTokenStorage: Storage<String?> = (mock(StringStorage::class.java)).apply {
            whenever(get()).thenReturn(PUSH_TOKEN)
        }

        val pushTokenProvider = DefaultPushTokenProvider(mockPushTokenStorage)

        val result = pushTokenProvider.providePushToken()

        result shouldBe PUSH_TOKEN
    }
}