package com.emarsys.core.util.log.entry

import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class OnScreenTimeTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Test
    fun testTopic() {
        val result = OnScreenTime(1, 0, 0, "campaignId", "requestId")

        result.topic shouldBe "log_inapp_on_screen_time"
    }

    @Test
    fun testData_when_requestIsNull() {
        val actual = OnScreenTime(30L, 0L, 30L, "campaignId", null).data
        val expected = mapOf(
                "campaign_id" to "campaignId",
                "duration" to 30L,
                "start" to 0L,
                "end" to 30L,
                "source" to "push"
        )

        actual shouldBe expected
    }

    @Test
    fun testData_when_requestIsAvailable() {
        val actual = OnScreenTime(130L, 0L, 130L, "realCampaignId", "requestId").data
        val expected = mapOf(
                "campaign_id" to "realCampaignId",
                "duration" to 130L,
                "start" to 0L,
                "end" to 130L,
                "source" to "customEvent",
                "request_id" to "requestId"
        )

        actual shouldBe expected
    }
}