package com.emarsys.core.util

import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.util.serialization.SerializationUtils
import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe

class SerializationUtilsTest : AnnotationSpec() {
    @Test
    fun testSerialization() {
        val payload = HashMap<String, Any?>()
        payload["key"] = "value"
        val nested = HashMap<String, Any>()
        nested["key2"] = "value2"
        nested["key3"] = true
        payload["nested"] = nested
        val headers = HashMap<String, String>()
        headers["header1"] = "header-value1"
        headers["header2"] = "header-value2"
        val expected = RequestModel(
            "https://www.google.com",
            RequestMethod.GET,
            payload,
            headers,
            999,
            101,
            "id"
        )
        val blob = SerializationUtils.serializableToBlob(expected)
        val result = SerializationUtils.blobToSerializable(blob) as RequestModel
        result shouldBe expected
    }

    @Test
    fun testSerialization_serializesNullCorrectly() {
        val requestModel: RequestModel? = null
        val bytes = SerializationUtils.serializableToBlob(requestModel)
        val result = SerializationUtils.blobToSerializable(bytes) as RequestModel?
        result shouldBe null
    }
}