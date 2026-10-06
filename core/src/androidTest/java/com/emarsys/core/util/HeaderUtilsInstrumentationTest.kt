package com.emarsys.core.util

import io.kotest.matchers.shouldBe
import org.junit.Test

class HeaderUtilsInstrumentationTest  {
    private val username = "user"

    @Test
    fun testCreateBasicAuth_shouldCreateCorrectBasicAuthString() {
        val expected = "Basic dXNlcjo="
        val result = HeaderUtils.createBasicAuth(username)
        result shouldBe expected
    }
}