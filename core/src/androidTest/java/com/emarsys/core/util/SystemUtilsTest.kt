package com.emarsys.core.util

import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule


class SystemUtilsTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

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