package com.emarsys.core.util.log.entry

import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class InAppLoadingTimeTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Test
    fun testTopic() {
        val result = InAppLoadingTime(0L, "", "").topic

        result shouldBe "log_inapp_loading_time"
    }

    @Test
    fun testData_when_requestIsNull() {
        val data = InAppLoadingTime(10L, "campaignId", null).data

        data shouldBe mapOf(
                "duration" to 10L,
                "campaign_id" to "campaignId",
                "source" to "push"
        )
    }

    @Test
    fun testData_when_requestIsAvailable() {
        val data = InAppLoadingTime(10L, "campaignId", "requestId").data

        data shouldBe mapOf(
                "duration" to 10L,
                "campaign_id" to "campaignId",
                "source" to "customEvent",
                "request_id" to "requestId"
        )
    }
}