package com.emarsys.core.database.repository.specification

import com.emarsys.testUtil.TimeoutUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class QueryAllTest {

    @Rule
    @JvmField
    val timeout = TimeoutUtils.timeoutRule

    private lateinit var specification: QueryAll

    @Before
    fun init() {
        specification = QueryAll("table")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_tableNameMustNotBeNull() {
        QueryAll(null)
    }

    @Test
    fun testGetSql() {
        val expected = "SELECT * FROM table;"
        val result = specification.sql

        assertEquals(expected, result)
    }

    @Test
    fun testGetArgs() {
        assertNull(specification.args)
    }

}