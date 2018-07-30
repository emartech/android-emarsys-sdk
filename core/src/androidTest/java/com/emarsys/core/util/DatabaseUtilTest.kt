package com.emarsys.core.util

import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

class DatabaseUtilTest {

    @Test
    fun testGenerateInStatement_shouldGenerateValidStatementEnding() {
        val result = DatabaseUtil.generateInStatement("request_id", arrayOf("123", "12", "1"))
        result shouldBeEqualTo "request_id IN (?, ?, ?)"
    }
}