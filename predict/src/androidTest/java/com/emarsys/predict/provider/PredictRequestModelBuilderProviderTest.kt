package com.emarsys.predict.provider

import com.emarsys.predict.request.PredictHeaderFactory
import com.emarsys.predict.request.PredictRequestContext
import com.emarsys.predict.request.PredictRequestModelBuilder
import com.emarsys.testUtil.TimeoutUtils
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

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockRequestContext = mock(PredictRequestContext::class.java)
        mockHeaderFactory = mock(PredictHeaderFactory::class.java)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_predictRequestContext_shouldNotBeNull() {
        PredictRequestModelBuilderProvider(null, mockHeaderFactory)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_headerFactory_shouldNotBeNull() {
        PredictRequestModelBuilderProvider(mockRequestContext, null)
    }

    @Test
    fun testProvidePredictRequestModelBuilder_shouldProvideNewRequestModelBuilder() {
        val expected = PredictRequestModelBuilder(mockRequestContext, mockHeaderFactory)

        val provider = PredictRequestModelBuilderProvider(mockRequestContext, mockHeaderFactory)

        val result = provider.providePredictRequestModelBuilder()

        result.javaClass shouldBe expected.javaClass
    }

    @Test
    fun testProvidePredictRequestModelBuilder_shouldProvideDifferentRequestModelBuilder() {
        val provider = PredictRequestModelBuilderProvider(mockRequestContext, mockHeaderFactory)
        val result1 = provider.providePredictRequestModelBuilder()
        val result2 = provider.providePredictRequestModelBuilder()

        result1 shouldNotBe result2
    }
}