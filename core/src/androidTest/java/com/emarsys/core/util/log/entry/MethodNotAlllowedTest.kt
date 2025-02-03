package com.emarsys.core.util.log.entry

import io.kotest.matchers.shouldBe
import org.junit.Test

class MethodNotAllowedTest  {

    private companion object {
        const val testCallerMethodName = "testCallerMethodName"
        val testParameters = mapOf(
            "parameter1" to "value1",
            "parameter2" to "value2"
        )
    }


    @Test
    fun testGetTopic() {
        MethodNotAllowed(MethodNotAllowedTest::class.java, testCallerMethodName, null).topic shouldBe "log_method_not_allowed"
    }

    @Test
    fun testGetData_dataDoesNotContainsParameters_whenParametersInConstructorInNull() {
        val expectedResult = mapOf(
                "className" to "MethodNotAllowedTest",
                "methodName" to testCallerMethodName
        )

        MethodNotAllowed(MethodNotAllowedTest::class.java, testCallerMethodName, null).data shouldBe expectedResult
    }

    @Test
    fun testGetData() {
        val expectedResult = mapOf(
                "className" to "MethodNotAllowedTest",
                "methodName" to testCallerMethodName,
                "parameters" to testParameters
        )

        MethodNotAllowed(MethodNotAllowedTest::class.java, testCallerMethodName, testParameters).data shouldBe expectedResult
    }
}