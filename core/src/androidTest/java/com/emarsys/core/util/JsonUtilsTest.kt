package com.emarsys.core.util


import com.emarsys.core.util.JsonUtils.fromList
import com.emarsys.core.util.JsonUtils.fromMap
import com.emarsys.core.util.JsonUtils.merge
import com.emarsys.core.util.JsonUtils.toFlatMap
import com.emarsys.core.util.JsonUtils.toFlatMapIncludingNulls
import com.emarsys.testUtil.AnnotationSpec
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class JsonUtilsTest : AnnotationSpec() {

    private companion object {
        const val KEY_1 = "key1"
        const val KEY_2 = "key2"
        const val KEY_3 = "key3"
        const val KEY_4 = "key4"
        const val KEY_5 = "key5"
        const val KEY_6 = "key6"
        const val KEY_7 = "key7"
        const val KEY_8 = "key8"
        const val KEY_9 = "key9"
        const val KEY_10 = "key10"
        const val KEY_11 = "key11"
        const val KEY_12 = "key12"
    }


    @Test
    fun testFromMap_emptyMap() {
        val input = mapOf<String, Any>()
        val expected = JSONObject()
        val result = fromMap(input)

        result.toString() shouldBe expected.toString()
    }

    @Test
    @Throws(JSONException::class)
    fun testFromMap_basicValues() {
        val input = mapOf(
            KEY_1 to 2,
            KEY_2 to 200.toShort(),
            KEY_3 to 212.toByte(),
            KEY_4 to 4L,
            KEY_5 to 4.0f,
            KEY_6 to 0.7,
            KEY_7 to true,
            KEY_8 to false,
            KEY_9 to "string",
            KEY_10 to 'c',
            KEY_11 to JSONArray(listOf("a", 1, "b")),
            KEY_12 to JSONObject().put("a", "a").put("b", 2)
        )
        val expected: JSONObject = JSONObject()
            .put(KEY_1, 2)
            .put(KEY_2, 200.toShort())
            .put(KEY_3, 212.toByte())
            .put(KEY_4, 4L)
            .put(KEY_5, 4.0)
            .put(KEY_6, 0.7)
            .put(KEY_7, true)
            .put(KEY_8, false)
            .put(KEY_9, "string")
            .put(KEY_10, "c")
            .put(KEY_11, JSONArray(listOf("a", 1, "b")))
            .put(KEY_12, JSONObject().put("a", "a").put("b", 2))
        val result = fromMap(input)

        result.toString() shouldBe expected.toString()
    }

    @Test
    @Throws(JSONException::class)
    fun testFromMap_nestedMap() {
        val input = mapOf(
            KEY_1 to mapOf(
                "a" to "a",
                "b" to 2,
                "nested1" to mapOf(
                    "c" to "cc",
                    "d" to 20
                ),
                "nested2" to mapOf(
                    "e" to "ee",
                    "f" to "ff"
                )
            ),
            KEY_2 to false
        )
        val expected = JSONObject()
            .put(
                KEY_1, JSONObject()
                    .put("a", "a")
                    .put("b", 2)
                    .put(
                        "nested1", JSONObject()
                            .put("c", "cc")
                            .put("d", 20)
                    )
                    .put(
                        "nested2", JSONObject()
                            .put("e", "ee")
                            .put("f", "ff")
                    )
            )
            .put(KEY_2, false)

        val result = fromMap(input)

        result.toString() shouldBe expected.toString()
    }

    @Test
    @Throws(JSONException::class)
    fun testFromMap_nestedArray_withInnerObjects() {
        val deeplyNested = mapOf(
            KEY_5 to "value"
        )
        val nested1 = mapOf(
            KEY_2 to 3,
            KEY_3 to false,
            KEY_4 to deeplyNested
        )
        val nested2 = mapOf(
            KEY_6 to 4,
            KEY_7 to "meid"
        )
        val list = listOf(
            nested1,
            nested2
        )
        val input = mapOf(
            KEY_1 to list
        )
        val expected = JSONObject()
            .put(
                KEY_1,
                JSONArray()
                    .put(
                        JSONObject()
                            .put(KEY_2, 3)
                            .put(KEY_3, false)
                            .put(
                                KEY_4, JSONObject()
                                    .put(KEY_5, "value")
                            )
                    )
                    .put(
                        JSONObject()
                            .put(KEY_6, 4)
                            .put(KEY_7, "meid")
                    )
            )
        val result = fromMap(input)

        result.toString() shouldBe expected.toString()
    }

    @Test
    @Throws(JSONException::class)
    fun testFromList_basicValues() {
        val input = listOf(
            2,
            200.toShort(),
            212.toByte(),
            4L,
            4.0f,
            0.7,
            true,
            false,
            "string",
            'c',
            JSONArray(listOf("a", 1, "b")),
            JSONObject().put("a", "a").put("b", 2)
        )
        val expected: JSONArray = JSONArray()
            .put(2)
            .put(200.toShort())
            .put(212.toByte())
            .put(4L)
            .put(4.0)
            .put(0.7)
            .put(true)
            .put(false)
            .put("string")
            .put("c")
            .put(JSONArray(listOf("a", 1, "b")))
            .put(JSONObject().put("a", "a").put("b", 2))
        val result = fromList(input)

        result.toString() shouldBe expected.toString()
    }

    @Test
    fun testFromList_nestedArray() {
        val nested = listOf(
            "a",
            "b",
            "c"
        )
        val input = listOf(
            1,
            4,
            false,
            nested,
            "end"
        )
        val expected = JSONArray()
            .put(1)
            .put(4)
            .put(false)
            .put(
                JSONArray()
                    .put("a")
                    .put("b")
                    .put("c")
            )
            .put("end")
        val result = fromList(input)

        result.toString() shouldBe expected.toString()
    }

    @Test
    @Throws(JSONException::class)
    fun testFromList_withNestedObjects() {
        val nested1 = mapOf(
            KEY_1 to "a",
            KEY_2 to 543
        )
        val deeplyNested1 = mapOf(
            KEY_4 to false,
            KEY_5 to 0.4
        )
        val deeplyNested2 = mapOf(
            KEY_7 to "id",
            KEY_8 to 123456789
        )
        val nested2 = mapOf(
            KEY_3 to deeplyNested1,
            KEY_6 to deeplyNested2
        )
        val input = listOf(
            nested1,
            "___",
            nested2
        )
        val expected = JSONArray()
            .put(
                JSONObject()
                    .put(KEY_1, "a")
                    .put(KEY_2, 543)
            )
            .put("___")
            .put(
                JSONObject()
                    .put(
                        KEY_3, JSONObject()
                            .put(KEY_4, false)
                            .put(KEY_5, 0.4)
                    )
                    .put(
                        KEY_6, JSONObject()
                            .put(KEY_7, "id")
                            .put(KEY_8, 123456789)
                    )
            )
        val result = fromList(input)

        result.toString() shouldBe expected.toString()
    }

    @Test
    fun testMerge_throwException_ifArgumentListIsEmpty() {
        shouldThrow<IllegalArgumentException> {
            merge()
        }
    }

    @Test
    @Throws(JSONException::class)
    fun testMerge_withOnlyOneElement() {
        val expected = JSONObject().put("key", "value")
        val actual = merge(expected)
        actual.toString() shouldBe expected.toString()
    }

    @Test
    @Throws(JSONException::class)
    fun testMerge_withMultipleEmptyJsonElements() {
        val json1 = JSONObject()
        val json2 = JSONObject()
        val json3 = JSONObject()
        val actual = merge(json1, json2, json3)
        val expected = JSONObject()

        actual.toString() shouldBe expected.toString()
    }

    @Test
    @Throws(JSONException::class)
    fun testMerge_withMultipleElements() {
        val json1 = JSONObject()
            .put("key1", "value1")
            .put("key2", "value2")
        val json2 = JSONObject()
            .put("key2", true)
        val json3 = JSONObject()
        val json4 = JSONObject()
            .put(
                "key3",
                JSONObject().put("nestedKey", 567)
            )
        val expected = JSONObject()
            .put("key1", "value1")
            .put("key2", true)
            .put(
                "key3",
                JSONObject().put("nestedKey", 567)
            )
        val actual = merge(json1, json2, json3, json4)

        actual.toString() shouldBe expected.toString()
    }

    @Test
    fun testToFlatMapIncludingNulls_withEmptyJsonObject() {
        val result = toFlatMapIncludingNulls(JSONObject())
        val expected: Map<String, String?> = mapOf()

        result shouldBe expected
    }

    @Test
    fun testToFlatMapIncludingNulls_withJsonObjectOfStringAndNullValues() {
        val input = JSONObject()
            .put("key1", "value1")
            .put("key2", "null")
            .put("key3", "value3")
        val result = toFlatMapIncludingNulls(input)
        val expected: MutableMap<String, String?> = mutableMapOf()
        expected["key1"] = "value1"
        expected["key2"] = null
        expected["key3"] = "value3"

        result shouldBe expected
    }

    @Test
    fun testToFlatMapIncludingNulls_withJsonObjectOfMixedValues() {
        val input = JSONObject()
            .put("key1", "value1")
            .put("key2", 3.14)
            .put("key3", false)
            .put("keyWithNull", "null")
            .put(
                "key4", JSONObject()
                    .put("nestedKey1", "nestedValue1")
                    .put("nestedKey2", 900)
                    .put("nestedKey3", "null")
            )
        val result = toFlatMapIncludingNulls(input)
        val expected: Map<String, String?> = mapOf(
            "key1" to "value1",
            "key2" to "3.14",
            "key3" to "false",
            "keyWithNull" to null,
            "key4" to """{"nestedKey1":"nestedValue1","nestedKey2":900,"nestedKey3":"null"}"""
        )

        result shouldBe expected
    }

    @Test
    fun testToFlatMap_withEmptyJsonObject() {
        val result = toFlatMap(JSONObject())
        val expected: Map<String, String> = HashMap()
        result shouldBe expected
    }

    @Test
    @Throws(JSONException::class)
    fun testToFlatMap_withJsonObjectOfStringValues() {
        val input = JSONObject()
            .put("key1", "value1")
            .put("key2", "value2")
            .put("key3", "value3")
        val result = toFlatMap(input)
        val expected: MutableMap<String, String> = HashMap()
        expected["key1"] = "value1"
        expected["key2"] = "value2"
        expected["key3"] = "value3"

        result shouldBe expected
    }

    @Test
    @Throws(JSONException::class)
    fun testToFlatMap_withJsonObjectOfStringValuesAndNull() {
        val input = JSONObject()
            .put("key1", "value1")
            .put("key2", "value2")
            .put("key3", null)
            .put(
                "key4", JSONObject()
                    .put("nestedKey1", "nestedValue1")
                    .put("nestedKey2", null)
            )
        val result = toFlatMap(input)
        val expected: MutableMap<String, String> = HashMap()
        expected["key1"] = "value1"
        expected["key2"] = "value2"
        expected["key4"] = "{\"nestedKey1\":\"nestedValue1\"}"

        result shouldBe expected
    }

    @Test
    @Throws(JSONException::class)
    fun testToFlatMap_withJsonObjectOfMixedValues() {
        val input = JSONObject()
            .put("key1", "value1")
            .put("key2", 3.14)
            .put("key3", false)
            .put(
                "key4", JSONObject()
                    .put("nestedKey1", "nestedValue1")
                    .put("nestedKey2", 900)
            )
        val result = toFlatMap(input)
        val expected: MutableMap<String, String> = HashMap()
        expected["key1"] = "value1"
        expected["key2"] = "3.14"
        expected["key3"] = "false"
        expected["key4"] = """{"nestedKey1":"nestedValue1","nestedKey2":900}"""

        result shouldBe expected
    }

    @Test
    fun testToMap() {
        val input = JSONObject(
            mapOf(
                "key" to "value",
                "key2" to 3,
                "key2.5" to true,
                "key2.6" to null,
                "key3" to JSONArray(listOf("value1", "value2", "value3")),
                "key4" to JSONArray(listOf(1, 2, 3)),
                "key5" to JSONArray(listOf(true, false)),
                "key6" to JSONArray(listOf(JSONArray(listOf("value1")), JSONArray(listOf(1, 2)))),
                "key7" to JSONArray(listOf(JSONObject(mapOf("key" to "value")))),
                "key8" to JSONObject(),
                "key9" to JSONObject(
                    mapOf(
                        "key" to 1,
                        "key2" to "value2",
                        "key2.5" to true,
                        "key2.6" to null,
                        "key3" to JSONArray(listOf("value1", "value2", "value3"))
                    )
                ),
            )
        )

        val expected = mapOf(
            "key" to "value",
            "key2" to 3,
            "key2.5" to true,
            "key2.6" to null,
            "key3" to listOf("value1", "value2", "value3"),
            "key4" to listOf(1, 2, 3),
            "key5" to listOf(true, false),
            "key6" to listOf(listOf("value1"), listOf(1, 2)),
            "key7" to listOf(mapOf("key" to "value")),
            "key8" to mapOf<String, Any>(),
            "key9" to mapOf(
                "key" to 1,
                "key2" to "value2",
                "key2.5" to true,
                "key2.6" to null,
                "key3" to listOf("value1", "value2", "value3")
            )
        )

        JsonUtils.toMap(input) shouldBe expected
    }
}