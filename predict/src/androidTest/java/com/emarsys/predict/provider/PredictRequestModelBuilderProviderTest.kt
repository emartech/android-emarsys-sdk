package com.emarsys.predict.provider

import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.predict.request.PredictHeaderFactory
import com.emarsys.predict.request.PredictRequestContext
import com.emarsys.predict.request.PredictRequestModelBuilder
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock

class PredictRequestModelBuilderProviderTest {

    private lateinit var mockRequestContext: PredictRequestContext
    private lateinit var mockHeaderFactory: PredictHeaderFactory
    private lateinit var mockServiceProvider: ServiceEndpointProvider

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockRequestContext = mock(PredictRequestContext::class.java)
        mockHeaderFactory = mock(PredictHeaderFactory::class.java)
        mockServiceProvider = mock(ServiceEndpointProvider::class.java).apply {
            whenever(provideEndpointHost()).thenReturn("https://emarsys.com")
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_predictRequestContext_shouldNotBeNull() {
        PredictRequestModelBuilderProvider(null, mockHeaderFactory, mockServiceProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_headerFactory_shouldNotBeNull() {
        PredictRequestModelBuilderProvider(mockRequestContext, null, mockServiceProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_predictServiceProvider_shouldNotBeNull() {
        PredictRequestModelBuilderProvider(mockRequestContext, mockHeaderFactory, null)
    }

    @Test
    fun testProvidePredictRequestModelBuilder_shouldProvideNewRequestModelBuilder() {
        val expected = PredictRequestModelBuilder(mockRequestContext, mockHeaderFactory, mockServiceProvider)

        val provider = PredictRequestModelBuilderProvider(mockRequestContext, mockHeaderFactory, mockServiceProvider)

        val result = provider.providePredictRequestModelBuilder()

        result.javaClass shouldBe expected.javaClass
    }

    @Test
    fun testProvidePredictRequestModelBuilder_shouldProvideDifferentRequestModelBuilder() {
        val provider = PredictRequestModelBuilderProvider(mockRequestContext, mockHeaderFactory, mockServiceProvider)
        val result1 = provider.providePredictRequestModelBuilder()
        val result2 = provider.providePredictRequestModelBuilder()

        result1 shouldNotBe result2
    }
}