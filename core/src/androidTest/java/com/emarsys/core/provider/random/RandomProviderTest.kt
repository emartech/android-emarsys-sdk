package com.emarsys.core.provider.random

import io.kotest.matchers.doubles.shouldBeGreaterThanOrEqual
import io.kotest.matchers.doubles.shouldBeLessThanOrEqual
import org.junit.jupiter.api.Test

class RandomProviderTest {

    @Test
    fun testProvideRandomDouble() {
        val randomProvider = RandomProvider()

        randomProvider.provideDouble(1.0) shouldBeGreaterThanOrEqual 0.0
        randomProvider.provideDouble(1.0) shouldBeLessThanOrEqual  1.0
    }
}