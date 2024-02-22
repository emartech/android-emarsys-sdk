package com.emarsys.core.request.model

import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

class CompositeRequestModelTest {

    private companion object {
        const val TIMESTAMP = 800L
        const val TTL = 1000L
        const val URL = "https://emarsys.com"
        const val ID = "id"
        val METHOD = RequestMethod.PUT
        val ORIGINAL_IDS = arrayOf("uuid")
    }



    private lateinit var timestampProvider: TimestampProvider
    private lateinit var uuidProvider: UUIDProvider
    private lateinit var payload: Map<String, Any>
    private lateinit var headers: Map<String, String>

    @BeforeEach
    fun setUp() {
        payload = createPayload()
        headers = createHeaders()
        timestampProvider = TimestampProvider()
        uuidProvider = UUIDProvider()
    }

    @Test
    fun testEquals_withEqualModels() {
        val model1 = CompositeRequestModel(
                ID,
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
                TIMESTAMP,
                TTL,
                arrayOf("child_id1", "child_id2", "child_id3", "child_id4"))

        val model2 = CompositeRequestModel(
                ID,
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
                TIMESTAMP,
                TTL,
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
                ID,
                url,
                method,
                payload,
                headers,
                TIMESTAMP,
                TTL,
                arrayOf("child_id4"))

        val model2 = CompositeRequestModel(
                ID,
                url,
                method,
                payload,
                headers,
                TIMESTAMP,
                TTL,
                arrayOf("child_id1", "child_id2", "child_id3"))
        model1 shouldNotBe model2
    }

    @Test
    fun testBuilder_mandatoryArgumentsInitialized() {
        val result = CompositeRequestModel.Builder(timestampProvider, uuidProvider)
                .url(URL)
                .build()

        result.url shouldBe URL(URL)
    }

    @Test
    fun testBuilder_originalIdsCorrectlySet() {
        val result = CompositeRequestModel.Builder(timestampProvider, uuidProvider)
                .url(URL)
                .originalRequestIds(arrayOf("1"))
                .build()

        result.originalRequestIds shouldBe arrayOf("1")
    }

    @Test
    fun testBuilder_from() {
        val expected = CompositeRequestModel(ID, URL, METHOD, payload, headers, TIMESTAMP, TTL, ORIGINAL_IDS)

        val result = CompositeRequestModel.Builder(expected).build()

        result shouldBe expected
    }

    private fun createPayload(): Map<String, Any> {
        return mutableMapOf("key1" to "value1", "key2" to mapOf("key3" to "value3", "key4" to 5))
    }

    private fun createHeaders(): Map<String, String> {
        val result = HashMap<String, String>()
        result["content"] = "application/x-www-form-urlencoded"
        result["accept"] = "application/json"
        return result
    }

}