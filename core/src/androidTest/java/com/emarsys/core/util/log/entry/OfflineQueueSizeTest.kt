package com.emarsys.core.util.log.entry

import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe


class OfflineQueueSizeTest : AnnotationSpec() {


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