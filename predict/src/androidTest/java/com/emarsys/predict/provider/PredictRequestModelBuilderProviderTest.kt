package com.emarsys.predict.provider

import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.predict.request.PredictHeaderFactory
import com.emarsys.predict.request.PredictRequestContext
import com.emarsys.predict.request.PredictRequestModelBuilder
import com.emarsys.testUtil.mockito.whenever
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test

import org.mockito.Mockito.mock

class PredictRequestModelBuilderProviderTest {

    private lateinit var mockRequestContext: PredictRequestContext
    private lateinit var mockHeaderFactory: PredictHeaderFactory
    private lateinit var mockServiceProvider: ServiceEndpointProvider


    @BeforeEach
    fun setUp() {
        mockRequestContext = mock(PredictRequestContext::class.java)
        mockHeaderFactory = mock(PredictHeaderFactory::class.java)
        mockServiceProvider = mock(ServiceEndpointProvider::class.java).apply {
            whenever(provideEndpointHost()).thenReturn("https://emarsys.com")
        }
    }

    @Test
    fun testConstructor_predictRequestContext_shouldNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            PredictRequestModelBuilderProvider(null, mockHeaderFactory, mockServiceProvider)
        }
    }

    @Test
    fun testConstructor_headerFactory_shouldNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            PredictRequestModelBuilderProvider(mockRequestContext, null, mockServiceProvider)
        }
    }

    @Test
    fun testConstructor_predictServiceProvider_shouldNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            PredictRequestModelBuilderProvider(mockRequestContext, mockHeaderFactory, null)
        }
    }

    @Test
    fun testProvidePredictRequestModelBuilder_shouldProvideNewRequestModelBuilder() {
        val expected =
            PredictRequestModelBuilder(mockRequestContext, mockHeaderFactory, mockServiceProvider)

        val provider = PredictRequestModelBuilderProvider(
            mockRequestContext,
            mockHeaderFactory,
            mockServiceProvider
        )

        val result = provider.providePredictRequestModelBuilder()

        result.javaClass shouldBe expected.javaClass
    }

    @Test
    fun testProvidePredictRequestModelBuilder_shouldProvideDifferentRequestModelBuilder() {
        val provider = PredictRequestModelBuilderProvider(
            mockRequestContext,
            mockHeaderFactory,
            mockServiceProvider
        )
        val result1 = provider.providePredictRequestModelBuilder()
        val result2 = provider.providePredictRequestModelBuilder()

        result1 shouldNotBe result2
    }
}