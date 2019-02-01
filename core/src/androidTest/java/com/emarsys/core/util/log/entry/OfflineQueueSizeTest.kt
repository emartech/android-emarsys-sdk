package com.emarsys.core.util.log.entry

import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class OfflineQueueSizeTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

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