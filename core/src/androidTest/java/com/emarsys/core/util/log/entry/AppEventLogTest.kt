package com.emarsys.core.util.log.entry

import io.kotest.matchers.shouldBe

import org.junit.jupiter.api.Test


class AppEventLogTest {


    @Test
    fun testTopic() {
        val result = AppEventLog("")

        result.topic shouldBe "log_app_event"
    }

    @Test
    fun testData() {
        val testAttributes = mapOf(
                "testKey1" to "testValue1",
                "testKey2" to "testValue2"
        )

        val expected = mapOf(
                "eventName" to "testEventName",
                "eventAttributes" to testAttributes
        )

        val result = AppEventLog("testEventName", testAttributes)

        result.data shouldBe expected
    }

    @Test
    fun testData_withoutAttributes() {
        val expected = mapOf(
                "eventName" to "testEventName"
        )

        val result = AppEventLog("testEventName")

        result.data shouldBe expected
    }
}