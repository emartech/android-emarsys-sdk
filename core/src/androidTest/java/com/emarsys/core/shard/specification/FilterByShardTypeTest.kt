package com.emarsys.core.shard.specification

import com.emarsys.core.testUtil.TimeoutUtils
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldEqual

import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FilterByShardTypeTest {

    @Rule
    @JvmField
    val timeout = TimeoutUtils.getTimeoutRule()

    companion object {
        const val TYPE = "type1"
    }

    private lateinit var specification: FilterByShardType

    @Before
    fun setUp() {
        specification = FilterByShardType(TYPE)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_mustNotBeNull() {
        FilterByShardType(null)
    }

    @Test
    fun testGetSql() {
        specification.sql shouldBeEqualTo "SELECT * FROM shard WHERE type LIKE ? ORDER BY ROWID ASC;"
    }

    @Test
    fun testGetArgs() {
        specification.args shouldEqual arrayOf(TYPE)
    }
}