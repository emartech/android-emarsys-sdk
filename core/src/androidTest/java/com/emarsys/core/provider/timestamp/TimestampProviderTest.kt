package com.emarsys.core.provider.timestamp

import io.kotest.matchers.shouldBe
import org.junit.Test

class TimestampProviderTest  {
    @Test
    fun testProvideTimestamp_returnsTheCurrentTimestamp() {
        val before = System.currentTimeMillis()
        val actual = TimestampProvider().provideTimestamp()
        val after = System.currentTimeMillis()
        (before <= actual) shouldBe true
        (actual <= after) shouldBe true
    }
}