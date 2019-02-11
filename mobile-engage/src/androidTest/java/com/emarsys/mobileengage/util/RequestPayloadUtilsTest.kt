package com.emarsys.mobileengage.util


import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class RequestPayloadUtilsTest {
    companion object {
        const val PUSH_TOKEN = "pushToken"
    }

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

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