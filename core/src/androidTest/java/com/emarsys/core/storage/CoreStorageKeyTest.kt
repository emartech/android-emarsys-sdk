package com.emarsys.core.storage

import com.emarsys.testUtil.AnnotationSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking


class CoreStorageKeyTest : AnnotationSpec() {


    @Test
    fun testGetKey() = runBlocking {
        CoreStorageKey.values().map {
            "core_${it.name.lowercase()}"
        }.zip(CoreStorageKey.values()) { stringValue, enum ->
            row(enum, stringValue)
        }.let {
            forAll(*it.toTypedArray()) { input, expected ->
                input.key shouldBe expected
            }
        }
    }
}