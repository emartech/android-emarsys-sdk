package com.emarsys.core.util.batch

import com.emarsys.core.Mapper
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.shard.specification.FilterByShardIds
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.MockitoTestUtils.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito
import org.mockito.Mockito.*

class BatchingShardTriggerTest {

    lateinit var trigger: BatchingShardTrigger

    lateinit var repository: Repository<ShardModel, SqlSpecification>
    lateinit var querySpecification: SqlSpecification
    lateinit var chunker: Mapper<List<ShardModel>, List<List<ShardModel>>>
    lateinit var merger: Mapper<List<ShardModel>, RequestModel>
    lateinit var manager: RequestManager

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun init() {
        repository = mock(Repository::class.java) as Repository<ShardModel, SqlSpecification>
        querySpecification = mock(SqlSpecification::class.java)
        chunker = mock(Mapper::class.java) as Mapper<List<ShardModel>, List<List<ShardModel>>>
        merger = mock(Mapper::class.java) as Mapper<List<ShardModel>, RequestModel>
        manager = mock(RequestManager::class.java)

        trigger = BatchingShardTrigger(repository, querySpecification, chunker, merger, manager)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_repository_mustNotBeNull() {
        BatchingShardTrigger(null, querySpecification, chunker, merger, manager)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_specification_mustNotBeNull() {
        BatchingShardTrigger(repository, null, chunker, merger, manager)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_chunker_mustNotBeNull() {
        BatchingShardTrigger(repository, querySpecification, null, merger, manager)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_merger_mustNotBeNull() {
        BatchingShardTrigger(repository, querySpecification, chunker, null, manager)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_manager_mustNotBeNull() {
        BatchingShardTrigger(repository, querySpecification, chunker, merger, null)
    }

    @Test
    fun testRun_submitsCorrectRequestModels_toRequestManager() {
        val (requestModel1, requestModel2, requestModel3) = setupMocks().requests

        trigger.run()

        Mockito.inOrder(manager).run {
            verify(manager).submit(requestModel1, null)
            verify(manager).submit(requestModel2, null)
            verify(manager).submit(requestModel3, null)
            verifyNoMoreInteractions(manager)
        }
    }

    @Test
    fun testRun_removesHandledShards_fromDatabase() {
        val (shards, requests) = setupMocks()
        val (shard1, shard2, shard3) = shards
        val (requestModel1, requestModel2, requestModel3) = requests

        trigger.run()

        Mockito.inOrder(manager, repository).run {
            this.verify(repository).query(querySpecification)
            this.verify(manager, Mockito.timeout(50)).submit(requestModel1, null)
            this.verify(repository).remove(FilterByShardIds(listOf(shard1)))
            this.verify(manager, Mockito.timeout(50)).submit(requestModel2, null)
            this.verify(repository).remove(FilterByShardIds(listOf(shard2)))
            this.verify(manager, Mockito.timeout(50)).submit(requestModel3, null)
            this.verify(repository).remove(FilterByShardIds(listOf(shard3)))
            this.verifyNoMoreInteractions()
        }

    }

    @Test
    fun testRun_doesNothing_whenQueryReturns_emptyList() {
        whenever(repository.query(querySpecification)).thenReturn(listOf())

        trigger.run()

        verify(repository).query(querySpecification)
        verifyNoMoreInteractions(repository)
        verifyZeroInteractions(chunker)
        verifyZeroInteractions(merger)
        verifyZeroInteractions(manager)
    }

    private fun setupMocks(): MockObjects {
        val shard1 = mock(ShardModel::class.java)
        val shard2 = mock(ShardModel::class.java)
        val shard3 = mock(ShardModel::class.java)

        val shards = listOf(shard1, shard2, shard3)

        val requestModel1 = mock(RequestModel::class.java)
        val requestModel2 = mock(RequestModel::class.java)
        val requestModel3 = mock(RequestModel::class.java)

        val requestModels = listOf(requestModel1, requestModel2, requestModel3)

        val chunkedShards = shards.map { listOf(it) }

        whenever(repository.query(querySpecification)).thenReturn(shards)

        whenever(chunker.map(shards)).thenReturn(chunkedShards)

        chunkedShards.forEachIndexed { i, chunkedShardList ->
            whenever(merger.map(chunkedShardList)).thenReturn(requestModels[i])
        }

        return MockObjects(shards, requestModels)
    }

    private data class MockObjects(val shards: List<ShardModel>, val requests: List<RequestModel>)

}