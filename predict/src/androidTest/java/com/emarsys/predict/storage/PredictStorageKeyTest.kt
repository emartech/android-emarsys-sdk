package com.emarsys.predict.storage

import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.tables.row
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class PredictStorageKeyTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Test
    fun testGetKey() {
        PredictStorageKey.values().map {
            "predict_${it.name.toLowerCase()}"
        }.zip(PredictStorageKey.values()) { stringValue, enum ->
            row(enum, stringValue)
        }.let {
            forall(*it.toTypedArray()) { input, expected ->
                input.key shouldBe expected
            }
        }
    }
}
