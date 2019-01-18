package com.emarsys.core.util.batch

import com.emarsys.core.Mapper
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.shard.specification.FilterByShardIds
import com.emarsys.core.util.batch.BatchingShardTrigger.RequestStrategy.PERSISTENT
import com.emarsys.core.util.batch.BatchingShardTrigger.RequestStrategy.TRANSIENT
import com.emarsys.core.util.predicate.Predicate
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.MockitoTestUtils.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.*

class BatchingShardTriggerTest {

    private lateinit var repository: Repository<ShardModel, SqlSpecification>
    private lateinit var predicate: Predicate<List<ShardModel>>
    private lateinit var querySpecification: SqlSpecification
    private lateinit var chunker: Mapper<List<ShardModel>, List<List<ShardModel>>>
    private lateinit var merger: Mapper<List<ShardModel>, RequestModel>
    private lateinit var manager: RequestManager

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun init() {
        repository = mock(Repository::class.java) as Repository<ShardModel, SqlSpecification>
        predicate = (mock(Predicate::class.java) as Predicate<List<ShardModel>>).apply {
            whenever(this.evaluate(ArgumentMatchers.anyList())).thenReturn(true)
        }
        querySpecification = mock(SqlSpecification::class.java)
        chunker = mock(Mapper::class.java) as Mapper<List<ShardModel>, List<List<ShardModel>>>
        merger = mock(Mapper::class.java) as Mapper<List<ShardModel>, RequestModel>
        manager = mock(RequestManager::class.java)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_repository_mustNotBeNull() {
        BatchingShardTrigger(null, predicate, querySpecification, chunker, merger, manager, PERSISTENT)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_predicate_mustNotBeNull() {
        BatchingShardTrigger(repository, null, querySpecification, chunker, merger, manager, PERSISTENT)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_specification_mustNotBeNull() {
        BatchingShardTrigger(repository, predicate, null, chunker, merger, manager, PERSISTENT)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_chunker_mustNotBeNull() {
        BatchingShardTrigger(repository, predicate, querySpecification, null, merger, manager, PERSISTENT)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_merger_mustNotBeNull() {
        BatchingShardTrigger(repository, predicate, querySpecification, chunker, null, manager, PERSISTENT)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_manager_mustNotBeNull() {
        BatchingShardTrigger(repository, predicate, querySpecification, chunker, merger, null, PERSISTENT)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestStrategy_mustNotBeNull() {
        BatchingShardTrigger(repository, predicate, querySpecification, chunker, merger, manager, null)
    }

    @Test
    fun testRun_persistent_submitsCorrectRequestModels_toRequestManager() {
        val (requestModel1, requestModel2, requestModel3) = setupMocks().requests

        persistentTrigger().run()

        Mockito.inOrder(manager).run {
            verify(manager).submit(requestModel1, null)
            verify(manager).submit(requestModel2, null)
            verify(manager).submit(requestModel3, null)
            verifyNoMoreInteractions(manager)
        }
    }

    @Test
    fun testRun_transient_submitsCorrectRequestModels_toRequestManager() {
        val (requestModel1, requestModel2, requestModel3) = setupMocks().requests

        transientTrigger().run()

        Mockito.inOrder(manager).run {
            verify(manager).submitNow(requestModel1)
            verify(manager).submitNow(requestModel2)
            verify(manager).submitNow(requestModel3)
            verifyNoMoreInteractions(manager)
        }
    }

    @Test
    fun testRun_removesHandledShards_fromDatabase() {
        val (shards, requests) = setupMocks()
        val (shard1, shard2, shard3) = shards
        val (requestModel1, requestModel2, requestModel3) = requests

        persistentTrigger().run()

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
    fun testRun_doesNothing_whenPredicateReturns_false() {
        whenever(predicate.evaluate(ArgumentMatchers.anyList())).thenReturn(false)

        anyTrigger().run()

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

    private fun persistentTrigger() = trigger(PERSISTENT)

    private fun transientTrigger() = trigger(TRANSIENT)

    private fun anyTrigger() = persistentTrigger()

    private fun trigger(requestStrategy: BatchingShardTrigger.RequestStrategy) = BatchingShardTrigger(
            repository,
            predicate,
            querySpecification,
            chunker,
            merger,
            manager,
            requestStrategy)

}