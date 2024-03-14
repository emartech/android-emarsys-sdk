package com.emarsys.core.util.log.entry

import com.emarsys.core.util.log.LogLevel
import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class LogEntryKtTest : AnnotationSpec() {

    @Test
    fun testToData() {
        val logEntry: LogEntry = mock {
            on { data } doReturn mapOf(
                "testKey" to "testValue",
                "testKey2" to "testValue2"
            )
        }
        val result = logEntry.toData(LogLevel.INFO, "testThreadName", "flutter")

        result shouldBe mapOf(
            "testKey" to "testValue",
            "testKey2" to "testValue2",
            "level" to "INFO",
            "thread" to "testThreadName",
            "wrapper" to "flutter"
        )
    }

    @Test
    fun testToData_shouldExcludeWrapper_whenWrapperInfo_isNull() {
        val logEntry: LogEntry = mock {
            on { data } doReturn mapOf(
                "testKey" to "testValue",
                "testKey2" to "testValue2"
            )
        }
        val result = logEntry.toData(LogLevel.INFO, "testThreadName", null)

        result shouldBe mapOf(
            "testKey" to "testValue",
            "testKey2" to "testValue2",
            "level" to "INFO",
            "thread" to "testThreadName"
        )
    }
}