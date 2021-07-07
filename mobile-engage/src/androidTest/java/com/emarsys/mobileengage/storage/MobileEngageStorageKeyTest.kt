package com.emarsys.mobileengage.storage

import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.tables.row
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class MobileEngageStorageKeyTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Test
    fun testGetKey() {
        MobileEngageStorageKey.values().map {
            "mobile_engage_${it.name.lowercase()}"
        }.zip(MobileEngageStorageKey.values()) { stringValue, enum ->
            row(enum, stringValue)
        }.let {
            forall(*it.toTypedArray()) { input, expected ->
                input.key shouldBe expected
            }
        }
    }
}