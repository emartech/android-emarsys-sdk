package com.emarsys.core.util


import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test


class DatabaseUtilTest {


    @Test
    fun testGenerateInStatement_shouldGenerateValidStatementEnding() {
        val result = DatabaseUtil.generateInStatement("request_id", arrayOf("123", "12", "1"))
        result shouldBe "request_id IN (?, ?, ?)"
    }
}