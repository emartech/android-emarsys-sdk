package com.emarsys.predict.storage

import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.Test


class PredictStorageKeyTest  {


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
