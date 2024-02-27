package com.emarsys.core.util.log.entry

import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe


class StatusLogTest : AnnotationSpec() {
    private companion object {
        const val testCallerMethodName = "testCallerMethodName"
        val testParameters = mapOf(
            "parameter1" to "value1",
            "parameter2" to "value2"
        )
        val testStatus = mapOf(
            "key1" to "value1",
            "key2" to mapOf(
                "statusMapKey1" to "statusValue1",
                "statusMapKey2" to "statusValue2"
            )
        )
    }


    @Test
    fun testTopic() {
        val result = StatusLog(StatusLogTest::class.java, testCallerMethodName, testParameters)

        result.topic shouldBe "log_status"
    }

    @Test
    fun testGetData_dataDoesNotContainsParameters_whenParametersInConstructorInNull() {
        val expectedResult = mapOf(
                "className" to "StatusLogTest",
                "methodName" to testCallerMethodName
        )
        StatusLog(StatusLogTest::class.java, testCallerMethodName, null).data shouldBe expectedResult
    }

    @Test
    fun testGetData() {
        val expectedResult = mapOf(
                "className" to "StatusLogTest",
                "methodName" to testCallerMethodName,
                "parameters" to testParameters
        )

        StatusLog(StatusLogTest::class.java, testCallerMethodName, testParameters).data shouldBe expectedResult
    }

    @Test
    fun testGetData_dataDoesNotContainsStatus_whenItsNull() {
        val expectedResult = mapOf(
                "className" to "StatusLogTest",
                "methodName" to testCallerMethodName,
                "parameters" to testParameters
        )

        StatusLog(StatusLogTest::class.java, testCallerMethodName, testParameters).data shouldBe expectedResult
    }

    @Test
    fun testGetData_containsStatus() {
        val expectedResult = mapOf(
                "className" to "StatusLogTest",
                "methodName" to testCallerMethodName,
                "parameters" to testParameters,
                "status" to testStatus
        )

        StatusLog(StatusLogTest::class.java, testCallerMethodName, testParameters, testStatus).data shouldBe expectedResult
    }
}