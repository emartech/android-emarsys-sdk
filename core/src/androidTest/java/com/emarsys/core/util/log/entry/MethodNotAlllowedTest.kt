package com.emarsys.core.util.log.entry

import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class MethodNotAllowedTest {

    private companion object {
        const val testCallerMethodName = "testCallerMethodName"
        val testParameters = mapOf(
                "parameter1" to "value1",
                "parameter2" to "value2"
        )
    }

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Test
    fun testGetTopic() {
        MethodNotAllowed(MethodNotAllowedTest::class.java, testCallerMethodName, null).topic shouldBe "log_method_not_allowed"
    }

    @Test
    fun testGetData_dataDoesNotContainsParameters_whenParametersInConstructorInNull() {
        val expectedResult = mapOf(
                "class_name" to "MethodNotAllowedTest",
                "method_name" to testCallerMethodName
        )

        MethodNotAllowed(MethodNotAllowedTest::class.java, testCallerMethodName, null).data shouldBe expectedResult
    }

    @Test
    fun testGetData() {
        val expectedResult = mapOf(
                "class_name" to "MethodNotAllowedTest",
                "method_name" to testCallerMethodName,
                "parameters" to testParameters
        )

        MethodNotAllowed(MethodNotAllowedTest::class.java, testCallerMethodName, testParameters).data shouldBe expectedResult
    }
}