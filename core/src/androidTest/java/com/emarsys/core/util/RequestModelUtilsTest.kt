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
        val requestModel = RequestModel(
                "https://emarsys.com",
                RequestMethod.POST,
                null,
                emptyMap(),
                100,
                900000,
                "requestModelId")

        val result = RequestModelUtils.extractIdsFromCompositeRequestModel(requestModel)

        result shouldBe listOf("requestModelId")
    }
}