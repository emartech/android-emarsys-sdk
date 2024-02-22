package com.emarsys.core.util

import io.kotest.matchers.shouldBe

import org.junit.jupiter.api.Test


class SystemUtilsTest {


    @Test
    fun testIsClassFound_java() {
        val result = SystemUtils.isClassFound("java.util.ArrayList")
        result shouldBe true
    }

    @Test
    fun testIsClassFound_kotlin() {
        val result = SystemUtils.isClassFound("kotlin.Pair")
        result shouldBe true
    }

    @Test
    fun testIsClassFound_missingClass() {
        val result = SystemUtils.isClassFound("no.such.Class")
        result shouldBe false
    }

    @Test
    fun testGetCallerMethodName() {
        SystemUtils.getCallerMethodName() shouldBe "testGetCallerMethodName"
    }

}