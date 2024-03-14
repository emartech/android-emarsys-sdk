package com.emarsys.core.provider.timestamp

import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe

class TimestampProviderTest : AnnotationSpec() {
    @Test
    fun testProvideTimestamp_returnsTheCurrentTimestamp() {
        val before = System.currentTimeMillis()
        val actual = TimestampProvider().provideTimestamp()
        val after = System.currentTimeMillis()
        (before <= actual) shouldBe true
        (actual <= after) shouldBe true
    }
}