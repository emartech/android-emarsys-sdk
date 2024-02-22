package com.emarsys.common.feature

import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test


class InnerFeatureTest {

    @Test
    fun testValues_shouldReturnCorrectValues() {
        val expectedValues =
            arrayOf("MOBILE_ENGAGE", "PREDICT", "EVENT_SERVICE_V4", "APP_EVENT_CACHE")
        InnerFeature.values().map { it.toString() } shouldBe expectedValues
    }

    @Test
    fun testGetName_shouldReturnCorrectValue() = runBlocking {
        InnerFeature.values().map {
            "inner_feature_${it.name.lowercase()}"
        }.zip(InnerFeature.values()) { stringValue, enum ->
            row(enum, stringValue)
        }.let {
            forAll(*it.toTypedArray()) { input, expected ->
                input.featureName shouldBe expected
            }
        }
    }
}