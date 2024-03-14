package com.emarsys.core.util

import com.emarsys.testUtil.AnnotationSpec
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

class HeaderUtilsInstrumentationTest : AnnotationSpec() {
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