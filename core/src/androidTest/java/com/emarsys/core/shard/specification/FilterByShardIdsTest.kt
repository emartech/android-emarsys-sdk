package com.emarsys.core.shard.specification

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.repository.specification.Everything
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.shard.ShardModelRepository
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class FilterByShardIdsTest {

    private lateinit var context: Context
    private lateinit var originalShardList: List<ShardModel>
    private lateinit var shardModelRepository: ShardModelRepository
    private lateinit var concurrentHadlerHolder: ConcurrentHandlerHolder

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun init() {
        DatabaseTestUtils.deleteCoreDatabase()
        context = InstrumentationRegistry.getTargetContext().applicationContext
        concurrentHadlerHolder =
            ConcurrentHandlerHolderFactory(Handler(Looper.getMainLooper())).create()

        originalShardList = listOf(
            ShardModel("id1", "type1", mapOf(), 0, 0),
            ShardModel("id2", "type2", mapOf(), 1, 10),
            ShardModel("id3", "type3", mapOf("key1" to "value1", "key2" to 333), 2, 20),
            ShardModel("id4", "type4", mapOf(), 3, 30)
        )

        val coreDbHelper = CoreDbHelper(context, mutableMapOf())
        shardModelRepository = ShardModelRepository(coreDbHelper, concurrentHadlerHolder)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDeleteRow_withInvalidArgument() {
        runBlocking {
            shardModelRepository.remove(FilterByShardIds(null))
        }
    }

    @Test
    fun testDeleteRows() {
        val deletionShardList = originalShardList.subList(1, 3)
        val expectedShardList = originalShardList - deletionShardList
        runBlocking {
            originalShardList.forEach {
                shardModelRepository.add(it)
            }

            shardModelRepository.remove(FilterByShardIds(deletionShardList))
        }
        val resultShardList = shardModelRepository.query(Everything())

        resultShardList shouldBe expectedShardList
    }

    @Test
    fun testDeleteRows_deletesNothingWhenThereIsNoMatch() {
        runBlocking {
            originalShardList.forEach {
                shardModelRepository.add(it)
            }
            shardModelRepository.remove(FilterByShardIds(listOf()))
        }
        val resultShardList = shardModelRepository.query(Everything())

        resultShardList shouldBe originalShardList
    }

    @Test
    fun testQueryRows() {
        val deletionShardList = originalShardList.subList(2, 4)
        val expectedShardList = originalShardList - deletionShardList
        runBlocking {
            originalShardList.forEach {
                shardModelRepository.add(it)
            }
        }

        val result = shardModelRepository.query(
            FilterByShardIds(
                listOf(
                    originalShardList[0],
                    originalShardList[1]
                )
            )
        )

        result shouldBe expectedShardList
    }

}