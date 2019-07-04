package com.emarsys.core.util.log.entry

import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.matchers.maps.shouldContainExactly
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

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_classNameMustNotBeNull() {
        MethodNotAllowed(null, testCallerMethodName, null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_callerMethodNameMustNotBeNull() {
        MethodNotAllowed(MethodNotAllowedTest::class.java, null, null)
    }

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

        MethodNotAllowed(MethodNotAllowedTest::class.java, testCallerMethodName, null).data shouldContainExactly expectedResult
    }

    @Test
    fun testGetData() {
        val expectedResult = mapOf(
                "class_name" to "MethodNotAllowedTest",
                "method_name" to testCallerMethodName,
                "parameters" to testParameters
        )

        MethodNotAllowed(MethodNotAllowedTest::class.java, testCallerMethodName, testParameters).data shouldContainExactly expectedResult
    }
}