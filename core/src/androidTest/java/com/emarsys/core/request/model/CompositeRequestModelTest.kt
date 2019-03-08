package com.emarsys.core.request.model

import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class CompositeRequestModelTest {

    companion object {
        const val TIMESTAMP = 800
        const val TTL = 1000
    }

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Test
    fun testEquals_withEqualModels() {
        val model1 = CompositeRequestModel(
                "https://google.com",
                RequestMethod.GET,
                mapOf("payload_key1" to 6,
                        "payload_key2" to false,
                        "payload_key3" to "value"
                ),
                mapOf("header_key1" to "value1",
                        "header_key2" to "value2",
                        "header_key3" to "value3"
                ),
                TIMESTAMP.toLong(),
                TTL.toLong(),
                arrayOf("child_id1", "child_id2", "child_id3", "child_id4"))

        val model2 = CompositeRequestModel(
                "https://google.com",
                RequestMethod.GET,
                mapOf(
                        "payload_key1" to 6,
                        "payload_key2" to false,
                        "payload_key3" to "value"
                ),
                mapOf(
                        "header_key1" to "value1",
                        "header_key2" to "value2",
                        "header_key3" to "value3"
                ),
                TIMESTAMP.toLong(),
                TTL.toLong(),
                arrayOf("child_id1", "child_id2", "child_id3", "child_id4"))

        model1 shouldBe model2
    }

    @Test
    fun testEquals_withDifferentChildIds() {
        val url = "https://google.com"
        val method = RequestMethod.GET
        val payload = mapOf(
                "payload_key1" to 6,
                "payload_key2" to false,
                "payload_key3" to "value"
        )
        val headers = mapOf(
                "header_key1" to "value1",
                "header_key2" to "value2",
                "header_key3" to "value3"
        )
        val model1 = CompositeRequestModel(
                url,
                method,
                payload,
                headers,
                TIMESTAMP.toLong(),
                TTL.toLong(),
                arrayOf("child_id4"))

        val model2 = CompositeRequestModel(
                url,
                method,
                payload,
                headers,
                TIMESTAMP.toLong(),
                TTL.toLong(),
                arrayOf("child_id1", "child_id2", "child_id3"))
        model1 shouldNotBe model2
    }
}