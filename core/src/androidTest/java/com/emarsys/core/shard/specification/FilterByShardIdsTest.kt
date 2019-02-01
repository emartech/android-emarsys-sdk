package com.emarsys.core.shard.specification

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FilterByShardIdsTest {

    private lateinit var context: Context
    private lateinit var originalShardList: List<ShardModel>
    private lateinit var shardModelRepository: ShardModelRepository

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

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

        val resultShardList = shardModelRepository.query(Everything())

        resultShardList shouldBe expectedShardList
    }

    @Test
    fun testDeleteRows_deletesNothingWhenThereIsNoMatch() {
        originalShardList.forEach(shardModelRepository::add)
        shardModelRepository.remove(FilterByShardIds(listOf()))

        val resultShardList = shardModelRepository.query(Everything())

        resultShardList shouldBe originalShardList
    }

    @Test
    fun testQueryRows() {
        val deletionShardList = originalShardList.subList(2, 4)
        val expectedShardList = originalShardList - deletionShardList
        originalShardList.forEach(shardModelRepository::add)

        val result = shardModelRepository.query(FilterByShardIds(listOf(originalShardList[0], originalShardList[1])))

        result shouldBe expectedShardList
    }

}