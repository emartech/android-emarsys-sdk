package com.emarsys.mobileengage.util


import io.kotlintest.shouldBe
import org.junit.Test

class RequestPayloadUtilsTest {
    companion object {
        const val PUSH_TOKEN = "pushToken"
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateSetPushTokenPayload_pushToken_mustNotBeNull() {
        RequestPayloadUtils.createSetPushTokenPayload(null)
    }

    @Test
    fun testCreateSetPushTokenPayload() {
        val url = RequestPayloadUtils.createSetPushTokenPayload(PUSH_TOKEN)
        url shouldBe mapOf(
                "pushToken" to PUSH_TOKEN
        )
    }
}