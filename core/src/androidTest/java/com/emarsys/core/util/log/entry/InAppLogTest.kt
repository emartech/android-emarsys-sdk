package com.emarsys.core.util.log.entry

import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class InAppLogTest {
    private companion object {
        const val endTime = 10L
        const val startTime = 5L
        const val duration = 5L
        const val onScreenTime = 10L
        const val campaignId = "campaignId"
        const val requestId = "requestId"
        const val startScreenTime = 5L
        const val endScreenTime = 10L
    }

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Test
    fun testTopic() {
        val result = InAppLog(
                InAppLoadingTime(startTime, endTime),
                OnScreenTime(onScreenTime, startScreenTime, endScreenTime),
                campaignId,
                requestId
        )

        result.topic shouldBe "log_inapp_metrics"
    }

    @Test
    fun testData_when_requestIsNull() {
        val result = InAppLog(
                InAppLoadingTime(startTime, endTime),
                OnScreenTime(onScreenTime, startScreenTime, endScreenTime),
                campaignId,
                null
        )

        result.data shouldBe mapOf(
                "source" to "push",
                "loading_time_start" to startTime,
                "loading_time_end" to endTime,
                "loading_time_duration" to duration,
                "on_screen_time_start" to startScreenTime,
                "on_screen_time_end" to endScreenTime,
                "on_screen_time_duration" to onScreenTime,
                "campaign_id" to "campaignId"
        )
    }

    @Test
    fun testData_when_requestIsAvailable() {
        val result = InAppLog(
                InAppLoadingTime(startTime, endTime),
                OnScreenTime(onScreenTime, startScreenTime, endScreenTime),
                campaignId,
                requestId
        )

        result.data shouldBe mapOf(
                "source" to "customEvent",
                "request_id" to requestId,
                "loading_time_start" to startTime,
                "loading_time_end" to endTime,
                "loading_time_duration" to duration,
                "on_screen_time_start" to startScreenTime,
                "on_screen_time_end" to endScreenTime,
                "on_screen_time_duration" to onScreenTime,
                "campaign_id" to "campaignId"
        )
    }
}