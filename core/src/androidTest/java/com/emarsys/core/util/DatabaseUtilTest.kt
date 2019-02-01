package com.emarsys.core.util

import com.emarsys.testUtil.TimeoutUtils
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class DatabaseUtilTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Test
    fun testGenerateInStatement_shouldGenerateValidStatementEnding() {
        val result = DatabaseUtil.generateInStatement("request_id", arrayOf("123", "12", "1"))
        Assert.assertEquals("request_id IN (?, ?, ?)", result)
    }
}