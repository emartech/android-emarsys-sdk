package com.emarsys.common.feature

import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.tables.row
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class InnerFeatureTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Test
    fun testValues_shouldReturnCorrectValues() {
        val expectedValues = arrayOf("MOBILE_ENGAGE", "PREDICT", "EVENT_SERVICE_V4")
        InnerFeature.values().map { it.toString() } shouldBe expectedValues
    }

    @Test
    fun testGetName_shouldReturnCorrectValue() {
        InnerFeature.values().map {
            "inner_feature_${it.name.toLowerCase()}"
        }.zip(InnerFeature.values()) { stringValue, enum ->
            row(enum, stringValue)
        }.let {
            forall(*it.toTypedArray()) { input, expected ->
                input.getName() shouldBe expected
            }
        }
    }


}