package com.emarsys.mobileengage.storage

import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.tables.row
import org.junit.Test

class MobileEngageStorageKeyTest {

    @Test
    fun testGetKey() {
        MobileEngageStorageKey.values().map {
            "mobile_engage_${it.name.toLowerCase()}"
        }.zip(MobileEngageStorageKey.values()) { stringValue, enum ->
            row(enum, stringValue)
        }.let {
            forall(*it.toTypedArray()) { input, expected ->
                input.key shouldBe expected
            }
        }
    }
}