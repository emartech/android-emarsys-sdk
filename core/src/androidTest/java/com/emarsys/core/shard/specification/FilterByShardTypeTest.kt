package com.emarsys.core.shard.specification

import android.support.test.InstrumentationRegistry
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.shard.ShardModelRepository
import com.emarsys.core.testUtil.DatabaseTestUtils
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
        DatabaseTestUtils.deleteCoreDatabase()
        specification = FilterByShardType(TYPE)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_mustNotBeNull() {
        FilterByShardType(null)
    }

    @Test
    fun testGetSqlString() {
        specification.sql shouldBeEqualTo "SELECT * FROM shard WHERE type LIKE ? ORDER BY ROWID ASC;"
    }

    @Test
    fun testGetArgsString() {
        specification.args shouldEqual arrayOf(TYPE)
    }

    @Test
    fun testQueryUsingFilterByShardType() {
        val context = InstrumentationRegistry.getTargetContext().applicationContext
        val repository = ShardModelRepository(context)
        val shardList = mutableListOf(
                ShardModel("a1", "button_click", mapOf(), 0, 0),
                ShardModel("a2", "button_click", mapOf(), 0, 0),
                ShardModel("a2", "button_click", mapOf("key" to 22, "key2" to "value"), 0, 0),
                ShardModel("a4", "not_button_click", mapOf("key" to 11, "key2" to "asdasd"), 0, 0)
        )
        shardList.forEach(repository::add)

        val resultList = repository.query(FilterByShardType("button_click"))

        val expectedList = shardList.filter { x -> (x.type == "button_click") }

        resultList shouldEqual expectedList
    }
}