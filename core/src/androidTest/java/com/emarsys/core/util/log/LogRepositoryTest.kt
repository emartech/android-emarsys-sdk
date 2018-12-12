package com.emarsys.core.util.log

import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.shard.ShardModel
import io.kotlintest.matchers.beTheSameInstanceAs
import io.kotlintest.should
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

import java.lang.IllegalArgumentException
import java.util.*

class LogRepositoryTest {

    private lateinit var shardRepository: Repository<ShardModel, SqlSpecification>
    private lateinit var logRepository: LogRepository

    @Before
    @Suppress("UNCHECKED_CAST")
    fun init() {
        shardRepository = mock(Repository::class.java) as Repository<ShardModel, SqlSpecification>
        logRepository = LogRepository(shardRepository)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_shardRepository_mustNotBeNull() {
        LogRepository(null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testAdd_doesNotAcceptNull() {
        logRepository.add(null)
    }

    @Test
    fun testAdd_delegatesToShardRepository() {
        val logShard = mock(LogShard::class.java)

        logRepository.add(logShard)

        verify(shardRepository).add(logShard)
    }

    @Test
    fun testQuery_doesNotCallShardRepository() {
        val result = logRepository.query(mock(SqlSpecification::class.java))

        verifyZeroInteractions(shardRepository)
        result should beTheSameInstanceAs(Collections.emptyList())
    }

    @Test
    fun testDelete_doesNotCallShardRepository() {
        logRepository.query(mock(SqlSpecification::class.java))

        verifyZeroInteractions(shardRepository)
    }

}