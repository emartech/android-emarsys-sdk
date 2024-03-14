package com.emarsys.mobileengage.storage

import com.emarsys.testUtil.AnnotationSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking


class MobileEngageStorageKeyTest : AnnotationSpec() {


    @Test
    fun testGetKey() = runBlocking {
        MobileEngageStorageKey.values().map {
            "mobile_engage_${it.name.lowercase()}"
        }.zip(MobileEngageStorageKey.values()) { stringValue, enum ->
            row(enum, stringValue)
        }.let {
            forAll(*it.toTypedArray()) { input, expected ->
                input.key shouldBe expected
            }
        }
    }
}