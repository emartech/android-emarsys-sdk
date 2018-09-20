package com.emarsys.core.shard.specification

import android.support.test.InstrumentationRegistry
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.shard.ShardModelRepository
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.TimeoutUtils
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FilterByShardTypeTest {

    @Rule
    @JvmField
    val timeout = TimeoutUtils.timeoutRule

    companion object {
        const val TYPE = "type1"
    }

    private lateinit var specification: FilterByShardType

    @Before
    fun setUp() {
        DatabaseTestUtils.deleteCoreDatabase()
        specification = FilterByShardType(TYPE)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_mustNotBeNull() {
        FilterByShardType(null)
    }

    @Test
    fun testGetSqlString() {
        assertEquals("SELECT * FROM shard WHERE type LIKE ? ORDER BY ROWID ASC;", specification.sql)
    }

    @Test
    fun testGetArgsString() {
        assertArrayEquals(arrayOf(TYPE), specification.args)
    }

    @Test
    fun testQueryUsingFilterByShardType() {
        val context = InstrumentationRegistry.getTargetContext().applicationContext
        val coreDbHelper = CoreDbHelper(context, mapOf())
        val repository = ShardModelRepository(coreDbHelper)
        val shardList = mutableListOf(
                ShardModel("a1", "button_click", mapOf(), 0, 0),
                ShardModel("a2", "button_click", mapOf(), 0, 0),
                ShardModel("a2", "button_click", mapOf("key" to 22, "key2" to "value"), 0, 0),
                ShardModel("a4", "not_button_click", mapOf("key" to 11, "key2" to "asdasd"), 0, 0)
        )
        shardList.forEach(repository::add)

        val resultList = repository.query(FilterByShardType("button_click"))

        val expectedList = shardList.filter { x -> (x.type == "button_click") }

        assertEquals(expectedList, resultList)
    }
}