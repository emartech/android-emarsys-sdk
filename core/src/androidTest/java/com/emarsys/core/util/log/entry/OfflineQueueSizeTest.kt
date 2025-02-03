package com.emarsys.core.util.log.entry

import io.kotest.matchers.shouldBe
import org.junit.Test


class OfflineQueueSizeTest  {


    @Test
    fun testTopic() {
        val result = OfflineQueueSize(0)

        result.topic shouldBe "log_offline_queue_size"
    }

    @Test
    fun testData() {
        val result = OfflineQueueSize(3)

        result.data shouldBe mapOf("queueSize" to 3)
    }
}