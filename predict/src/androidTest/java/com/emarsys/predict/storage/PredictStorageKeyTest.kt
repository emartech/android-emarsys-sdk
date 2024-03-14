package com.emarsys.predict.storage

import com.emarsys.testUtil.AnnotationSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking


class PredictStorageKeyTest : AnnotationSpec() {


    @Test
    fun testGetKey() = runBlocking {
        PredictStorageKey.values().map {
            "predict_${it.name.lowercase()}"
        }.zip(PredictStorageKey.values()) { stringValue, enum ->
            row(enum, stringValue)
        }.let {
            forAll(*it.toTypedArray()) { input, expected ->
                input.key shouldBe expected
            }
        }
    }
}
