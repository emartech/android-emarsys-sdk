package com.emarsys.core.util.log.entry

import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class CrashLogTest {

    private lateinit var crashLog: CrashLog
    private lateinit var exception: Exception

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun init() {
        exception = ConcurrentModificationException("cause of the exception")
        crashLog = CrashLog(exception)
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
                "stack_trace" to exception.stackTrace.map(StackTraceElement::toString)
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