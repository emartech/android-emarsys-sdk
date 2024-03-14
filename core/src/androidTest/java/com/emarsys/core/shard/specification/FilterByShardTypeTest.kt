package com.emarsys.core.shard.specification

import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.repository.specification.Everything
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.shard.ShardModelRepository
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking


class FilterByShardTypeTest : AnnotationSpec() {


    companion object {
        const val TYPE = "type1"
    }

    private lateinit var specification: FilterByShardType
    private lateinit var shardList: MutableList<ShardModel>
    private lateinit var repository: ShardModelRepository
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder

    @Before
    fun setUp() {
        DatabaseTestUtils.deleteCoreDatabase()
        specification = FilterByShardType(TYPE)
        val context = InstrumentationRegistry.getTargetContext().applicationContext
        val coreDbHelper = CoreDbHelper(context, mutableMapOf())
        concurrentHandlerHolder =
            ConcurrentHandlerHolderFactory.create()
        repository = ShardModelRepository(coreDbHelper, concurrentHandlerHolder)
        shardList = mutableListOf(
            ShardModel("a1", "button_click", mapOf(), 0, 0),
            ShardModel("a2", "button_click", mapOf(), 0, 0),
            ShardModel("a2", "button_click", mapOf("key" to 22, "key2" to "value"), 0, 0),
            ShardModel("a4", "not_button_click", mapOf("key" to 11, "key2" to "asdasd"), 0, 0)
        )
        runBlocking {
            shardList.forEach {
                repository.add(it)
            }
        }
    }

    @Test
    fun testConstructor_mustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            FilterByShardType(null)
        }
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
        runBlocking {
            repository.remove(FilterByShardType("button_click"))
        }

        val resultList = repository.query(Everything())

        resultList shouldBe expectedList
    }
}