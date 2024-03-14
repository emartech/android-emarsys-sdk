package com.emarsys.core.util


import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe


class DatabaseUtilTest : AnnotationSpec() {


    @Test
    fun testGenerateInStatement_shouldGenerateValidStatementEnding() {
        val result = DatabaseUtil.generateInStatement("request_id", arrayOf("123", "12", "1"))
        result shouldBe "request_id IN (?, ?, ?)"
    }
}