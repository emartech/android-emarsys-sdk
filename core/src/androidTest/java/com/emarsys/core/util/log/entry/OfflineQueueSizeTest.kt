package com.emarsys.core.util.log.entry

import io.kotlintest.shouldBe
import org.junit.Test

class OfflineQueueSizeTest {

    @Test
    fun testTopic() {
        val result = OfflineQueueSize(0)

        result.topic shouldBe "log_offline_queue_size"
    }

    @Test
    fun testData() {
        val result = OfflineQueueSize(3)

        result.data shouldBe mapOf("queue_size" to 3)
    }
}