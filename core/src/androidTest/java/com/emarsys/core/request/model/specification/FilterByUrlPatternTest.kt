package com.emarsys.core.request.model.specification

import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FilterByUrlPatternTest {

    @Rule
    @JvmField
    val timeout = TimeoutUtils.timeoutRule

    private lateinit var specification: FilterByUrlPattern
    private lateinit var pattern: String

    @Before
    fun init() {
        pattern = "root/___/_%/event"
        specification = FilterByUrlPattern(pattern)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_patternMustNotBeNull() {
        FilterByUrlPattern(null)
    }

    @Test
    fun testGetSql() {
        specification.sql shouldBe "SELECT * FROM request WHERE url LIKE ?;"
    }

    @Test
    fun testGetArs() {
        specification.args shouldBe arrayOf(pattern)
    }
}