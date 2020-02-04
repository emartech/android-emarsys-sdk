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
                "loadingTimeStart" to startTime,
                "loadingTimeEnd" to endTime,
                "loadingTimeDuration" to duration,
                "onScreenTimeStart" to startScreenTime,
                "onScreenTimeEnd" to endScreenTime,
                "onScreenTimeDuration" to onScreenTime,
                "campaignId" to "campaignId"
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
                "requestId" to requestId,
                "loadingTimeStart" to startTime,
                "loadingTimeEnd" to endTime,
                "loadingTimeDuration" to duration,
                "onScreenTimeStart" to startScreenTime,
                "onScreenTimeEnd" to endScreenTime,
                "onScreenTimeDuration" to onScreenTime,
                "campaignId" to "campaignId"
        )
    }
}