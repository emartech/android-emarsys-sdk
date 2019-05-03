package com.emarsys.core.util.log.entry

import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class InAppLoadingTimeTest {
    private companion object {
        const val endTime = 10L
        const val startTime = 0L
        const val duration = 10L
    }

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Test
    fun testTopic() {
        val result = InAppLoadingTime(startTime, endTime, "", "").topic

        result shouldBe "log_inapp_loading_time"
    }

    @Test
    fun testData_when_requestIsNull() {
        val data = InAppLoadingTime(startTime, endTime, "campaignId", null).data

        data shouldBe mapOf(
                "duration" to duration,
                "start" to startTime,
                "end" to endTime,
                "campaign_id" to "campaignId",
                "source" to "push"
        )
    }

    @Test
    fun testData_when_requestIsAvailable() {
        val data = InAppLoadingTime(startTime, endTime, "campaignId", "requestId").data

        data shouldBe mapOf(
                "duration" to duration,
                "start" to startTime,
                "end" to endTime,
                "campaign_id" to "campaignId",
                "source" to "customEvent",
                "request_id" to "requestId"
        )
    }
}