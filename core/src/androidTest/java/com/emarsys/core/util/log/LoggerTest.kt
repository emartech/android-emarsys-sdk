package com.emarsys.core.util.log

import android.os.Handler
import com.emarsys.core.concurrency.CoreSdkHandlerProvider
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.di.DependencyContainer
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.util.log.entry.LogEntry
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.MockitoTestUtils.whenever
import com.emarsys.testUtil.mockito.ThreadSpy
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.*

class LoggerTest {

    companion object {
        const val TIMESTAMP = 400L
        const val UUID = "UUID12345"
        const val TTL = Long.MAX_VALUE
    }

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var handler: Handler
    private lateinit var shardRepositoryMock: Repository<ShardModel, SqlSpecification>
    private lateinit var timestampProviderMock: TimestampProvider
    private lateinit var uuidProviderMock: UUIDProvider
    private lateinit var dependencyContainer: DependencyContainer

    @Before
    @Suppress("UNCHECKED_CAST")
    fun init() {
        shardRepositoryMock = mock(Repository::class.java) as Repository<ShardModel, SqlSpecification>
        handler = CoreSdkHandlerProvider().provideHandler()
        timestampProviderMock = mock(TimestampProvider::class.java).apply {
            whenever(provideTimestamp()).thenReturn(TIMESTAMP)
        }
        uuidProviderMock = mock(UUIDProvider::class.java).apply {
            whenever(provideId()).thenReturn(UUID)
        }
        dependencyContainer = mock(DependencyContainer::class.java).apply {
            whenever(shardRepository).thenReturn(shardRepositoryMock)
            whenever(coreSdkHandler).thenReturn(handler)
            whenever(timestampProvider).thenReturn(timestampProviderMock)
            whenever(uuidProvider).thenReturn(uuidProviderMock)
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
        val logContent = mapOf(
                "key1" to "value",
                "key2" to 3,
                "key3" to true
        )

        Logger.log(logEntryMock("log_crash", logContent))

        val captor = ArgumentCaptor.forClass(ShardModel::class.java)

        verify(shardRepositoryMock, Mockito.timeout(100)).add(captor.capture())

        captor.value shouldBe ShardModel(
                UUID,
                "log_crash",
                logContent,
                TIMESTAMP,
                TTL)
    }

    @Test
    fun testLog_addsLog_toLogRepository_viaCoreSdkHandler() {
        val threadSpy = ThreadSpy<Unit>()
        doAnswer(threadSpy).`when`(shardRepositoryMock).add(ArgumentMatchers.any())

        Logger.log(logEntryMock())

        threadSpy.verifyCalledOnCoreSdkThread()
    }

    @Test
    fun testLog_doesNotLogAnything_ifDependencyInjection_isNotSetup() {
        DependencyInjection.tearDown()
        handler = spy(handler)
        whenever(dependencyContainer.coreSdkHandler).thenReturn(handler)
        DependencyInjection.setup(dependencyContainer)
        DependencyInjection.tearDown()

        Logger.log(logEntryMock())

        verifyZeroInteractions(handler)
    }

    private fun logEntryMock(topic: String = "", data: Map<String, Any> = mapOf()) =
            mock(LogEntry::class.java).apply {
                whenever(getData()).thenReturn(data)
                whenever(getTopic()).thenReturn(topic)
            }
}