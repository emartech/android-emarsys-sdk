package com.emarsys.core.validate

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.json.JSONObject

import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test

import org.mockito.Mockito.mock

class JsonObjectValidatorTest {


    lateinit var json: JSONObject

    @BeforeEach
    fun init() {
        json = JSONObject()
            .put("key1", "value1")
            .put("key2", 3.14)
    }

    @Test
    fun testFrom_mustNotAcceptNull() {
        shouldThrow<IllegalArgumentException> {
            JsonObjectValidator.from(null)
        }
    }

    @Test
    fun testFrom_shouldReturnValidator() {
        val validator = JsonObjectValidator.from(mock(JSONObject::class.java))
        validator shouldNotBe null
    }

    @Test
    fun testHasField_mustNotAcceptNull() {
        shouldThrow<IllegalArgumentException> {
            JsonObjectValidator.from(mock(JSONObject::class.java)).hasField(null)
        }
    }

    @Test
    fun testHasField_returnsTheSameInstance() {
        val validator1 = JsonObjectValidator.from(mock(JSONObject::class.java))
        val validator2 = validator1.hasField("field")
        validator1 shouldBeSameInstanceAs validator2
    }

    @Test
    fun testHasField_mustReturnWithError_whenFieldIsMissing() {
        val errors = JsonObjectValidator
            .from(JSONObject())
            .hasField("timestamp")
            .validate()
        errors shouldBe listOf("Missing field: 'timestamp'")
    }

    @Test
    fun testHasField_mustReturnWithError_whenCertainFieldAreMissing() {
        val errors = JsonObjectValidator.from(json)
            .hasField("timestamp")
            .hasField("title")
            .hasField("key1")
            .hasField("key2")
            .validate()
        errors shouldBe listOf(
            "Missing field: 'timestamp'",
            "Missing field: 'title'"
        )
    }

    @Test
    fun testHasFieldAndType_mustNotAcceptNullFieldName() {
        shouldThrow<IllegalArgumentException> {
            JsonObjectValidator.from(mock(JSONObject::class.java))
                .hasFieldWithType(null, Any::class.java)
        }
    }

    @Test
    fun testHasFieldAndType_mustNotAcceptNullFieldType() {
        shouldThrow<IllegalArgumentException> {
            JsonObjectValidator.from(mock(JSONObject::class.java)).hasFieldWithType("field", null)
        }
    }

    @Test
    fun testHasFieldAndType_returnsTheSameInstance() {
        val validator1 = JsonObjectValidator.from(mock(JSONObject::class.java))
        val validator2 = validator1.hasFieldWithType("field", Any::class.java)
        validator2 shouldBeSameInstanceAs validator1
    }

    @Test
    fun testHasFieldAndType_mustReturnWithError_whenFieldIsMissing() {
        val type = Long::class.java
        val errors = JsonObjectValidator
            .from(JSONObject())
            .hasFieldWithType("timestamp", type)
            .validate()

        errors shouldBe listOf("Missing field: 'timestamp' with type: $type")
    }

    @Test
    fun testHasFieldAndType_mustReturnWithError_whenFieldExists_butTypeIsNotTheExpected() {
        val type = java.lang.String::class.java
        val errors = JsonObjectValidator
            .from(json)
            .hasFieldWithType("key2", type)
            .validate()
        errors.size shouldBe 1
        errors shouldBe listOf("Type mismatch for key: 'key2', expected type: $type, but was: ${java.lang.Double::class.java}")
    }

    @Test
    fun testValidate_withMultipleValidationsX() {
        val errors = JsonObjectValidator
            .from(json)
            .hasFieldWithType("key2", java.lang.Double::class.java)
            .hasField("key1")
            .hasFieldWithType("key1", java.lang.Integer::class.java)
            .hasField("title")
            .hasFieldWithType("body", java.lang.String::class.java)
            .validate()
        errors shouldBe listOf(
            "Type mismatch for key: 'key1', expected type: ${java.lang.Integer::class.java}, but was: ${java.lang.String::class.java}",
            "Missing field: 'title'",
            "Missing field: 'body' with type: ${java.lang.String::class.java}"
        )
    }
}