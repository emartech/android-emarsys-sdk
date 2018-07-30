package com.emarsys.core.shard.specification

import com.emarsys.core.testUtil.TimeoutUtils

import org.junit.Before
import org.junit.Rule
import org.junit.Test

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals

open class FilterByTypeTest {

    @Rule
    @JvmField
    val timeout = TimeoutUtils.getTimeoutRule()

    companion object {
        const val TYPE = "type1"
    }

    private lateinit var specification: FilterByType

    @Before
    fun setUp() {
        specification = FilterByType(TYPE)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_mustNotBeNull() {
        FilterByType(null)
    }

    @Test
    fun testGetSql() {
        assertEquals("SELECT * FROM shard WHERE type LIKE ? ORDER BY ROWID ASC;", specification.sql)
    }

    @Test
    fun testGetArgs() {
        assertArrayEquals(arrayOf(TYPE), specification.args)
    }
}