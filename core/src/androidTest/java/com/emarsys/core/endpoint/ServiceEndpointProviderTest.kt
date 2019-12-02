package com.emarsys.core.endpoint

import com.emarsys.core.storage.Storage
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito

class ServiceEndpointProviderTest {
    companion object {
        const val ENDPOINT = "https://emarsys.com"
        const val DEFAULT_ENDPOINT = "https://default.emarsys.com"
    }

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var serviceEndpointProvider: ServiceEndpointProvider
    private lateinit var mockServiceUrlStorage: Storage<String>

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        mockServiceUrlStorage = Mockito.mock(Storage::class.java) as Storage<String>
        serviceEndpointProvider = ServiceEndpointProvider(mockServiceUrlStorage, DEFAULT_ENDPOINT)
    }

    @Test
    fun testProvideEndpoint_whenEndpointIsStored() {
        whenever(mockServiceUrlStorage.get()).thenReturn(ENDPOINT)

        val result = serviceEndpointProvider.provideEndpointHost()

        result shouldBe ENDPOINT
    }

    @Test
    fun testProvideEndpoint_whenEndpointIsNull() {
        val result = serviceEndpointProvider.provideEndpointHost()
        result shouldBe DEFAULT_ENDPOINT
    }
}