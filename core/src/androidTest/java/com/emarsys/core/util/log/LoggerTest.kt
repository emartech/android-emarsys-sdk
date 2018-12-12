package com.emarsys.core.util.log

import android.os.Handler
import com.emarsys.core.concurrency.CoreSdkHandlerProvider
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.di.DependencyContainer
import com.emarsys.core.di.DependencyInjection
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.MockitoTestUtils.whenever
import com.emarsys.testUtil.mockito.ThreadSpy
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.*

class LoggerTest {

    @Rule
    @JvmField
    var timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var logShardRepository: Repository<LogShard, SqlSpecification>
    private lateinit var handler: Handler
    private lateinit var dependencyContainer: DependencyContainer

    @Before
    @Suppress("UNCHECKED_CAST")
    fun init() {
        logShardRepository = mock(Repository::class.java) as Repository<LogShard, SqlSpecification>
        handler = CoreSdkHandlerProvider().provideHandler()
        dependencyContainer = mock(DependencyContainer::class.java).apply {
            whenever(logRepository).thenReturn(logShardRepository)
            whenever(coreSdkHandler).thenReturn(handler)
        }

        DependencyInjection.setup(dependencyContainer)
    }

    @After
    fun tearDown() {
        handler.looper.quit()
        DependencyInjection.tearDown()
    }

    @Test
    fun testLog_addsLog_toLogRepository() {
        val shard = mock(LogShard::class.java)

        Logger.log(shard)

        verify(logShardRepository, Mockito.timeout(100)).add(shard)
    }

    @Test
    fun testLog_addsLog_toLogRepository_viaCoreSdkHandler() {
        val threadSpy = ThreadSpy<Unit>()
        doAnswer(threadSpy).`when`(logShardRepository).add(ArgumentMatchers.any())

        Logger.log(mock(LogShard::class.java))

        threadSpy.verifyCalledOnCoreSdkThread()
    }

}