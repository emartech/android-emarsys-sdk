package com.emarsys.core.shard.specification

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.repository.specification.QueryAll
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.shard.ShardModelRepository
import com.emarsys.core.testUtil.DatabaseTestUtils
import com.emarsys.core.testUtil.TimeoutUtils
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FilterByShardIdsTest {

    lateinit var context: Context
    lateinit var originalShardList: List<ShardModel>
    lateinit var shardModelRepository: ShardModelRepository

    @Rule
    @JvmField
    val timeout = TimeoutUtils.getTimeoutRule()

    @Before
    fun init() {
        DatabaseTestUtils.deleteCoreDatabase()
        context = InstrumentationRegistry.getTargetContext().applicationContext

        originalShardList = listOf(
                ShardModel("id1", "type1", mapOf(), 0, 0),
                ShardModel("id2", "type2", mapOf(), 1, 10),
                ShardModel("id3", "type3", mapOf("key1" to "value1", "key2" to 333), 2, 20),
                ShardModel("id4", "type4", mapOf(), 3, 30)
        )

        val coreDbHelper = CoreDbHelper(context, mapOf())
        shardModelRepository = ShardModelRepository(coreDbHelper)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDeleteRow_withInvalidArgument() {
        shardModelRepository.remove(FilterByShardIds(null))
    }

    @Test
    fun testDeleteRows() {
        val deletionShardList = originalShardList.subList(1, 3)
        val expectedShardList = originalShardList - deletionShardList
        originalShardList.forEach(shardModelRepository::add)

        shardModelRepository.remove(FilterByShardIds(deletionShardList))

        val resultShardList = shardModelRepository.query(QueryAll("shard"))
        resultShardList shouldEqual expectedShardList
    }

    @Test
    fun testDeleteRows_deletesNothingWhenThereIsNoMatch() {
        originalShardList.forEach(shardModelRepository::add)
        shardModelRepository.remove(FilterByShardIds(listOf()))

        val resultShardList = shardModelRepository.query(QueryAll("shard"))
        resultShardList shouldEqual originalShardList
    }

}