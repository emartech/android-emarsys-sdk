package com.emarsys.core.util

import io.kotlintest.shouldBe
import org.junit.Test

class MapExtensionsKtTest {

    @Test
    fun testGetCaseInsensitive() {
        val map: Map<String?, String?> = mapOf("KeY0" to "Value0", "KeY1" to "Value1", "key2" to "Value2")
        val result = map.getCaseInsensitive("key1")

        result shouldBe "Value1"
    }

    @Test
    fun testGetCaseInsensitive_mustWorkWhenNoResult() {
        val map: Map<String?, String?> = mapOf("KeY0" to "Value0", "KeY1" to "Value1", "key2" to "Value2")
        val result = map.getCaseInsensitive(null)

        result shouldBe null
    }
}