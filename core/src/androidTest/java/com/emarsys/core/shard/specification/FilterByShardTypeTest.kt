package com.emarsys.core.shard.specification

import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.repository.specification.Everything
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.shard.ShardModelRepository
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
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
    private lateinit var shardList: MutableList<ShardModel>
    private lateinit var repository: ShardModelRepository
    @Before
    fun setUp() {
        DatabaseTestUtils.deleteCoreDatabase()
        specification = FilterByShardType(TYPE)
        val context = InstrumentationRegistry.getTargetContext().applicationContext
        val coreDbHelper = CoreDbHelper(context, mapOf())
        repository = ShardModelRepository(coreDbHelper)
        shardList = mutableListOf(
                ShardModel("a1", "button_click", mapOf(), 0, 0),
                ShardModel("a2", "button_click", mapOf(), 0, 0),
                ShardModel("a2", "button_click", mapOf("key" to 22, "key2" to "value"), 0, 0),
                ShardModel("a4", "not_button_click", mapOf("key" to 11, "key2" to "asdasd"), 0, 0)
        )
        shardList.forEach(repository::add)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_mustNotBeNull() {
        FilterByShardType(null)
    }

    @Test
    fun testSpecification() {
        with(FilterByShardType(TYPE)) {
            isDistinct shouldBe false
            columns shouldBe null
            selection shouldBe "type LIKE ?"
            selectionArgs shouldBe arrayOf(TYPE)
            groupBy shouldBe null
            having shouldBe null
            orderBy shouldBe "ROWID ASC"
            limit shouldBe null
        }
    }

    @Test
    fun testQueryUsingFilterByShardType() {
        val expectedList = shardList.filter { x -> (x.type == "button_click") }

        val resultList = repository.query(FilterByShardType("button_click"))

        resultList shouldBe expectedList
    }

    @Test
    fun testDeleteUsingFilterByShardType() {
        val expectedList = shardList.filterNot { x -> (x.type == "button_click") }

        repository.remove(FilterByShardType("button_click"))

        val resultList = repository.query(Everything())

        resultList shouldBe expectedList
    }
}