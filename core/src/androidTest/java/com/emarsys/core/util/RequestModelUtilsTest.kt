package com.emarsys.core.util

import com.emarsys.core.request.model.CompositeRequestModel
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class RequestModelUtilsTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Test
    fun testExtractIdsFromCompositeRequestModel() {
        val ids = arrayOf("id1", "id2", "id3")

        val requestModel = CompositeRequestModel(
                "0",
                "https://emarsys.com",
                RequestMethod.POST,
                null,
                emptyMap(),
                100,
                900000,
                ids)

        val result = RequestModelUtils.extractIdsFromCompositeRequestModel(requestModel)

        result shouldBe ids
    }

    @Test
    fun testExtractIdsFromCompositeRequestModel_requestModel() {
        val requestModel = createTestRequestModel("https://emarsys.com", RequestMethod.POST)

        val result = RequestModelUtils.extractIdsFromCompositeRequestModel(requestModel)

        result shouldBe listOf("requestModelId")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testExtractQueryParameters_requestModelMustNotBeNull() {
        RequestModelUtils.extractQueryParameters(null)
    }

    @Test
    fun testExtractQueryParameters_returnsEmptyMap_whenNoQueryParametersFound() {
        val requestModel = createTestRequestModel(
                "https://recommender.scarabresearch.com/merchants/", RequestMethod.GET)

        val result = RequestModelUtils.extractQueryParameters(requestModel)
        val expected = emptyMap<String, String>()

        result shouldBe expected
    }

    @Test
    fun testExtractExtractQueryParameters_resultMapContainsParameterFromQuery() {
        val requestModel = createTestRequestModel(
                "https://recommender.scarabresearch.com/merchants/merchantId?cp=1",
                RequestMethod.GET)

        val result = RequestModelUtils.extractQueryParameters(requestModel)
        val expected = mapOf("cp" to "1")

        result shouldBe expected
    }

    @Test
    fun testExtractExtractQueryParameters_withMultipleQueryParameters() {
        val requestModel = createTestRequestModel(
                "https://recommender.scarabresearch.com/merchants/merchantId?cp=1&vi=888999888&ci=12345&q3=c",
                RequestMethod.GET)

        val result = RequestModelUtils.extractQueryParameters(requestModel)
        val expected = mapOf(
                "cp" to "1",
                "vi" to "888999888",
                "ci" to "12345",
                "q3" to "c")

        result shouldBe expected
    }

    private fun createTestRequestModel(url: String, method: RequestMethod) = RequestModel(
            url,
            method,
            null,
            emptyMap(),
            100,
            90000,
            "requestModelId"
    )
}