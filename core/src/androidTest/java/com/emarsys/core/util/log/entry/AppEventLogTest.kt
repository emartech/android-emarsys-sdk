package com.emarsys.core.util.log.entry

import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class AppEventLogTest {
    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

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