package com.emarsys.core.util.log.entry

import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Rule
import org.junit.Test

class OnScreenTimeTest {

    @Rule
    @JvmField
    var timeout = TimeoutUtils.timeoutRule

    @Test
    fun testTopic() {
        val result = OnScreenTime(1, "campaignId", "requestId")

        result.topic shouldBe "log_inapp_on_screen_time"
    }

    @Test
    fun testData_when_requestIsNull() {
        val actual = OnScreenTime(30L, "campaignId", null).data
        val expected = mapOf(
                "campaign_id" to "campaignId",
                "duration" to 30L,
                "source" to "push"
        )

        actual shouldBe expected
    }

    @Test
    fun testData_when_requestIsAvailable() {
        val actual = OnScreenTime(130L, "realCampaignId", "requestId").data
        val expected = mapOf(
                "campaign_id" to "realCampaignId",
                "duration" to 130L,
                "source" to "customEvent",
                "request_id" to "requestId"
        )

        actual shouldBe expected
    }
}