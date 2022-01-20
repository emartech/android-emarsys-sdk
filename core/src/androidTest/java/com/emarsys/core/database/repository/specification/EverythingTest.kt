package com.emarsys.core.database.repository.specification

import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.shard.ShardModelRepository
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.matchers.beEmpty
import io.kotlintest.should
import io.kotlintest.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class EverythingTest {

    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun init() {
        DatabaseTestUtils.deleteCoreDatabase()
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
    }

    @Test
    fun testSpecification() {
        with(Everything()) {
            isDistinct shouldBe false
            columns shouldBe null
            selection shouldBe null
            selectionArgs shouldBe null
            groupBy shouldBe null
            having shouldBe null
            orderBy shouldBe null
            limit shouldBe null
        }
    }

    @Test
    fun testQueryAll() {
        val context = InstrumentationRegistry.getTargetContext().applicationContext
        val coreDbHelper = CoreDbHelper(context, mutableMapOf())
        val repository = ShardModelRepository(coreDbHelper, concurrentHandlerHolder)
        val expectedList = mutableListOf(
            ShardModel("a1", "button_click", mapOf(), 0, 0),
            ShardModel("a2", "button_click", mapOf(), 0, 0),
            ShardModel("a2", "button_click", mapOf("key" to 22, "key2" to "value"), 0, 0),
            ShardModel("a4", "not_button_click", mapOf("key" to 11, "key2" to "asdasd"), 0, 0)
        )
        runBlocking {
            expectedList.forEach {
                repository.add(it)
            }
        }

        val resultList = repository.query(Everything())

        resultList shouldBe expectedList
    }

    @Test
    fun testDeleteAll() {
        val context = InstrumentationRegistry.getTargetContext().applicationContext
        val coreDbHelper = CoreDbHelper(context, mutableMapOf())
        val repository = ShardModelRepository(coreDbHelper, concurrentHandlerHolder)
        runBlocking {
            mutableListOf(
                ShardModel("a1", "button_click", mapOf(), 0, 0),
                ShardModel("a2", "button_click", mapOf(), 0, 0),
                ShardModel("a2", "button_click", mapOf("key" to 22, "key2" to "value"), 0, 0),
                ShardModel("a4", "not_button_click", mapOf("key" to 11, "key2" to "asdasd"), 0, 0)
            ).forEach {
                repository.add(it)
            }

            repository.remove(Everything())
        }
        val resultList = repository.query(Everything())

        resultList should beEmpty()
    }

}