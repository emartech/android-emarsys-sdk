package com.emarsys.core.util.log.entry

import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe


class CrashLogTest : AnnotationSpec() {

    private lateinit var crashLog: CrashLog
    private lateinit var exception: Exception


    @Before
    fun init() {
        exception = ConcurrentModificationException("cause of the exception")
        crashLog = CrashLog(exception, "testInfo")
    }

    @Test
    fun testTopic() {
        crashLog.topic shouldBe "log_crash"
    }

    @Test
    fun testGetData() {
        val result = crashLog.data
        val expected = mapOf(
                "exception" to exception::class.java.name,
                "reason" to exception.message,
                "additionalInformation" to "testInfo",
                "stackTrace" to exception.stackTrace.map(StackTraceElement::toString)
        )
        result shouldBe expected
    }

    @Test
    fun testGetData_withoutAdditionalInformation() {
        crashLog = CrashLog(exception)

        val result = crashLog.data
        val expected = mapOf(
                "exception" to exception::class.java.name,
                "reason" to exception.message,
                "stackTrace" to exception.stackTrace.map(StackTraceElement::toString)
        )
        result shouldBe expected
    }

    @Test
    fun testGetData_throwableIsNull() {
        val result = CrashLog(null).data
        val expected = mapOf<String, String>()
        result shouldBe expected
    }
}