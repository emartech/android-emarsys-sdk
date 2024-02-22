package com.emarsys.core.util

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class HeaderUtilsInstrumentationTest {
    private val username = "user"

    @Test
    fun testCreateBasicAuth_usernameShouldNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            HeaderUtils.createBasicAuth(null)
        }
    }

    @Test
    fun testCreateBasicAuth_shouldCreateCorrectBasicAuthString() {
        val expected = "Basic dXNlcjo="
        val result = HeaderUtils.createBasicAuth(username)
        result shouldBe expected
    }
}