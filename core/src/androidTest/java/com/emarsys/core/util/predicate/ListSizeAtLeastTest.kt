package com.emarsys.core.util.predicate

import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class ListSizeAtLeastTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var predicate: ListSizeAtLeast<Int>

    @Before
    fun init() {
        predicate = ListSizeAtLeast(5)
    }

    @Test
    fun testEvaluate_5_returnsTrue_forAListWithSize_5() {
        predicate.evaluate(listOf(1, 2, 3, 4, 5)) shouldBe true
    }

    @Test
    fun testEvaluate_5_returnsTrue_forAListLargerThan_5() {
        predicate.evaluate(listOf(1, 2, 3, 4, 5, 6, 7, 8)) shouldBe true
    }

    @Test
    fun testEvaluate_5_returnsFalse_forAListLessThan_5() {
        predicate.evaluate(listOf(1, 2, 3, 4)) shouldBe false
    }
}