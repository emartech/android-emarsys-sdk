package com.emarsys.core.util.batch

import com.emarsys.core.Mapper
import com.emarsys.core.connection.ConnectionWatchDog
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.shard.specification.FilterByShardIds
import com.emarsys.core.util.batch.BatchingShardTrigger.RequestStrategy.PERSISTENT
import com.emarsys.core.util.batch.BatchingShardTrigger.RequestStrategy.TRANSIENT
import com.emarsys.core.util.predicate.Predicate
import com.emarsys.testUtil.mockito.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions

class BatchingShardTriggerTest  {

    private lateinit var mockRepository: Repository<ShardModel, SqlSpecification>
    private lateinit var mockPredicate: Predicate<List<ShardModel>>
    private lateinit var mockQuerySpecification: SqlSpecification
    private lateinit var mockChunker: Mapper<List<ShardModel>, List<List<ShardModel>>>
    private lateinit var mockMerger: Mapper<List<ShardModel>, RequestModel>
    private lateinit var mockRequestManager: RequestManager
    private lateinit var mockConnectionWatchDog: ConnectionWatchDog


    @Before
    fun setUp() {
        mockRepository = mock()
        mockPredicate = mock {
            on { evaluate(any()) } doReturn true
        }
        mockQuerySpecification = mock()
        mockChunker = mock()
        mockMerger = mock()
        mockRequestManager = mock()
        mockConnectionWatchDog = mock {
            on { isConnected } doReturn true
        }
    }

    @Test
    fun testRun_persistent_submitsCorrectRequestModels_toRequestManager() {
        val (requestModel1, requestModel2, requestModel3) = setupMocks().requests

        persistentTrigger().run()

        inOrder(mockRequestManager).run {
            verify(mockRequestManager).submit(requestModel1, null)
            verify(mockRequestManager).submit(requestModel2, null)
            verify(mockRequestManager).submit(requestModel3, null)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun testRun_transient_submitsCorrectRequestModels_toRequestManager() {
        val (requestModel1, requestModel2, requestModel3) = setupMocks().requests

        transientTrigger().run()

        inOrder(mockRequestManager).run {
            verify(mockRequestManager).submitNow(requestModel1)
            verify(mockRequestManager).submitNow(requestModel2)
            verify(mockRequestManager).submitNow(requestModel3)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun testRun_removesHandledShards_fromDatabase() {
        val (shards, requests) = setupMocks()
        val (shard1, shard2, shard3) = shards
        val (requestModel1, requestModel2, requestModel3) = requests

        persistentTrigger().run()
        runBlocking {
            inOrder(mockRequestManager, mockRepository).run {
                this.verify(mockRepository).query(mockQuerySpecification)
                this.verify(mockRequestManager, Mockito.timeout(50)).submit(requestModel1, null)
                this.verify(mockRepository).remove(FilterByShardIds(listOf(shard1)))
                this.verify(mockRequestManager, Mockito.timeout(50)).submit(requestModel2, null)
                this.verify(mockRepository).remove(FilterByShardIds(listOf(shard2)))
                this.verify(mockRequestManager, Mockito.timeout(50)).submit(requestModel3, null)
                this.verify(mockRepository).remove(FilterByShardIds(listOf(shard3)))
                this.verifyNoMoreInteractions()
            }
        }
    }

    @Test
    fun testRun_doesNothing_whenPredicateReturns_false() {
        whenever(mockPredicate.evaluate(any())).thenReturn(false)

        anyTrigger().run()

        verify(mockRepository).query(mockQuerySpecification)
        verifyNoMoreInteractions(mockRepository)
        verifyNoInteractions(mockChunker)
        verifyNoInteractions(mockMerger)
        verifyNoInteractions(mockRequestManager)
    }

    @Test
    fun testRun_shouldNotInvokeQuery_whenOffline() {
        whenever(mockConnectionWatchDog.isConnected).thenReturn(false)

        anyTrigger().run()

        verifyNoInteractions(mockRepository)
    }

    private fun setupMocks(): MockObjects {
        val shard1 = mock<ShardModel>()
        val shard2 = mock<ShardModel>()
        val shard3 = mock<ShardModel>()

        val shards = listOf(shard1, shard2, shard3)

        val requestModel1 = mock<RequestModel>()
        val requestModel2 = mock<RequestModel>()
        val requestModel3 = mock<RequestModel>()

        val requestModels = listOf(requestModel1, requestModel2, requestModel3)

        val chunkedShards = shards.map { listOf(it) }

        whenever(mockRepository.query(mockQuerySpecification)).thenReturn(shards)

        whenever(mockChunker.map(shards)).thenReturn(chunkedShards)

        chunkedShards.forEachIndexed { i, chunkedShardList ->
            whenever(mockMerger.map(chunkedShardList)).thenReturn(requestModels[i])
        }

        return MockObjects(shards, requestModels)
    }

    private data
class MockObjects(val shards: List<ShardModel>, val requests: List<RequestModel>)

    private fun persistentTrigger() = trigger(PERSISTENT)

    private fun transientTrigger() = trigger(TRANSIENT)

    private fun anyTrigger() = persistentTrigger()

    private fun trigger(requestStrategy: BatchingShardTrigger.RequestStrategy) =
        BatchingShardTrigger(
            mockRepository,
            mockPredicate,
            mockQuerySpecification,
            mockChunker,
            mockMerger,
            mockRequestManager,
            requestStrategy,
            mockConnectionWatchDog
        )

}