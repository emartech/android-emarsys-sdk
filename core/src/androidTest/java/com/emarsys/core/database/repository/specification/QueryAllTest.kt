package com.emarsys.core.database.repository.specification

import android.support.test.InstrumentationRegistry
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.shard.ShardModelRepository
import com.emarsys.testUtil.DatabaseTestUtils
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
        DatabaseTestUtils.deleteCoreDatabase()
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

    @Test
    fun testQueryUsingQueryAll() {
        specification = QueryAll("shard")

        val context = InstrumentationRegistry.getTargetContext().applicationContext
        val coreDbHelper = CoreDbHelper(context, mapOf())
        val repository = ShardModelRepository(coreDbHelper)
        val expectedList = mutableListOf(
                ShardModel("a1", "button_click", mapOf(), 0, 0),
                ShardModel("a2", "button_click", mapOf(), 0, 0),
                ShardModel("a2", "button_click", mapOf("key" to 22, "key2" to "value"), 0, 0),
                ShardModel("a4", "not_button_click", mapOf("key" to 11, "key2" to "asdasd"), 0, 0)
        )
        expectedList.forEach(repository::add)

        val resultList = repository.query(specification)

        assertEquals(expectedList, resultList)
    }

}