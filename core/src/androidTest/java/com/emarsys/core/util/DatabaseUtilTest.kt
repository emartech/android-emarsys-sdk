package com.emarsys.core.util

import org.junit.Assert
import org.junit.Test

class DatabaseUtilTest {

    @Test
    fun testGenerateInStatement_shouldGenerateValidStatementEnding() {
        val result = DatabaseUtil.generateInStatement("request_id", arrayOf("123", "12", "1"))
        Assert.assertEquals("request_id IN (?, ?, ?)", result)
    }
}