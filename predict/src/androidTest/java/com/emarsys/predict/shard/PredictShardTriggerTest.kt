package com.emarsys.predict.shard

import com.emarsys.core.Mapper
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.shard.specification.FilterByShardType
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.*

class PredictShardTriggerTest {

    lateinit var trigger: PredictShardTrigger

    lateinit var repository: Repository<ShardModel, SqlSpecification>
    lateinit var chunker: Mapper<List<ShardModel>, List<List<ShardModel>>>
    lateinit var merger: Mapper<List<ShardModel>, RequestModel>
    lateinit var manager: RequestManager

    @Before
    @Suppress("UNCHECKED_CAST")
    fun init() {
        repository = mock(Repository::class.java) as Repository<ShardModel, SqlSpecification>
        chunker = mock(Mapper::class.java) as Mapper<List<ShardModel>, List<List<ShardModel>>>
        merger = mock(Mapper::class.java) as Mapper<List<ShardModel>, RequestModel>
        manager = mock(RequestManager::class.java)

        trigger = PredictShardTrigger(repository, chunker, merger, manager)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_repository_mustNotBeNull() {
        PredictShardTrigger(null, chunker, merger, manager)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_chunker_mustNotBeNull() {
        PredictShardTrigger(repository, null, merger, manager)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_merger_mustNotBeNull() {
        PredictShardTrigger(repository, chunker, null, manager)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_manager_mustNotBeNull() {
        PredictShardTrigger(repository, chunker, merger, null)
    }

    @Test
    fun testRun_submitsCorrectRequestModels_toRequestManager() {
        val shard1 = mock(ShardModel::class.java)
        val shard2 = mock(ShardModel::class.java)
        val shard3 = mock(ShardModel::class.java)

        val requestModel1 = mock(RequestModel::class.java)
        val requestModel2 = mock(RequestModel::class.java)
        val requestModel3 = mock(RequestModel::class.java)

        val requestModels = listOf(requestModel1, requestModel2, requestModel3)

        val shards = listOf(shard1, shard2, shard3)

        val chunkedShards = shards.map { listOf(it) }

        `when`(repository.query(FilterByShardType("predict_%"))).thenReturn(shards)

        `when`(chunker.map(shards)).thenReturn(chunkedShards)

        chunkedShards.forEachIndexed { i, chunkedShardList ->
            `when`(merger.map(chunkedShardList)).thenReturn(requestModels[i])
        }

        trigger.run()

        Mockito.inOrder(manager).run {
            verify(manager).submit(requestModel1)
            verify(manager).submit(requestModel2)
            verify(manager).submit(requestModel3)
        }
        verifyNoMoreInteractions(manager)
    }

}