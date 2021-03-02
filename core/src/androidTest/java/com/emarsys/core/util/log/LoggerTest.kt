package com.emarsys.core.util.log

import android.os.Handler
import com.emarsys.core.concurrency.CoreSdkHandlerProvider
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.di.DependencyContainer
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.di.FakeCoreDependencyContainer
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.util.log.entry.LogEntry
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.ThreadSpy
import com.nhaarman.mockitokotlin2.*
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentCaptor
import java.util.concurrent.CountDownLatch


class LoggerTest {

    companion object {
        const val TIMESTAMP = 400L
        const val UUID = "UUID12345"
        const val TTL = Long.MAX_VALUE
    }

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var coreSdkHandler: Handler
    private lateinit var shardRepositoryMock: Repository<ShardModel, SqlSpecification>
    private lateinit var timestampProviderMock: TimestampProvider
    private lateinit var uuidProviderMock: UUIDProvider
    private lateinit var dependencyContainer: DependencyContainer
    private lateinit var loggerInstance: Logger
    private lateinit var loggerMock: Logger
    private lateinit var mockLogLevelStorage: StringStorage

    @Before
    @Suppress("UNCHECKED_CAST")
    fun init() {
        coreSdkHandler = CoreSdkHandlerProvider().provideHandler()
        shardRepositoryMock = mock()
        timestampProviderMock = mock<TimestampProvider>().apply {
            whenever(provideTimestamp()).thenReturn(TIMESTAMP)
        }
        uuidProviderMock = mock<UUIDProvider>().apply {
            whenever(provideId()).thenReturn(UUID)
        }
        mockLogLevelStorage = mock()

        loggerInstance = Logger(coreSdkHandler,
                shardRepositoryMock,
                timestampProviderMock,
                uuidProviderMock,
                mockLogLevelStorage,
                false
        )
        loggerMock = mock()

        dependencyContainer = FakeCoreDependencyContainer(coreSdkHandler = coreSdkHandler,
                shardRepository = shardRepositoryMock,
                timestampProvider = timestampProviderMock,
                uuidProvider = uuidProviderMock,
                logger = loggerMock)

        DependencyInjection.setup(dependencyContainer)
    }

    @After
    fun tearDown() {
        coreSdkHandler.looper.quit()
        DependencyInjection.tearDown()
    }

    @Test
    fun testPersistLog_addsLog_toShardRepository() {
        loggerInstance.persistLog(LogLevel.ERROR, logEntryMock("log_request", mapOf(
                "key1" to "value",
                "key2" to 3,
                "key3" to true
        )))

        val captor = ArgumentCaptor.forClass(ShardModel::class.java)

        verify(shardRepositoryMock, timeout(100)).add(capture<ShardModel>(captor))

        captor.value shouldBe ShardModel(
                UUID,
                "log_request",
                mapOf(
                        "key1" to "value",
                        "key2" to 3,
                        "key3" to true,
                        "level" to "ERROR"
                ),
                TIMESTAMP,
                TTL)
    }

    @Test
    fun testPersistLog_addsOtherLog_toShardRepository() {
        loggerInstance.persistLog(LogLevel.ERROR, logEntryMock("any_log", mapOf()))

        val captor = ArgumentCaptor.forClass(ShardModel::class.java)

        verify(shardRepositoryMock, timeout(100)).add(capture<ShardModel>(captor))

        captor.value shouldBe ShardModel(
                UUID,
                "any_log",
                mapOf("level" to "ERROR"),
                TIMESTAMP,
                TTL)
    }

    @Test
    fun testPersistLog_addsLog_toShardRepository_viaCoreSdkHandler() {
        val threadSpy = ThreadSpy<Unit>()

        org.mockito.Mockito.doAnswer(threadSpy).`when`(shardRepositoryMock).add(any())

        loggerInstance.persistLog(LogLevel.ERROR, logEntryMock())

        threadSpy.verifyCalledOnCoreSdkThread()
    }

    @Test
    fun testLog_delegatesToInstance_withINFOLogLevel() {
        whenever(mockLogLevelStorage.get()).thenReturn("INFO")

        val logEntry = logEntryMock(testTopic = "testTopic", testData = mapOf("testKey" to "testValue"))

        Logger.log(logEntry)

        waitForTask()

        verify(loggerMock).handleLog(LogLevel.INFO, logEntry)
    }

    @Test
    fun testLog_doesNotLogAnything_ifDependencyInjection_isNotSetup() {
        DependencyInjection.tearDown()

        dependencyContainer = mock()

        DependencyInjection.setup(dependencyContainer)
        DependencyInjection.tearDown()

        Logger.log(logEntryMock())

        waitForTask()

        verifyZeroInteractions(dependencyContainer)
    }

    @Test
    fun testPersistLog_shouldNotPersist_whenLoggingOurLog() {
        val latch = CountDownLatch(1)

        loggerInstance.persistLog(
                LogLevel.INFO,
                logEntryMock("log_request",
                        mapOf(
                                "url" to "https://log-dealer.eservice.emarsys.net/v1/log",
                                "key2" to 3,
                                "key3" to true
                        )
                )
        ) { latch.countDown() }
        latch.await()

        verify(shardRepositoryMock, timeout(100).times(0)).add(any())
    }

    @Test
    fun testPersistLog_shouldNotPersist_whenLogLevelIsBelowStoredLogLevel() {
        val latch = CountDownLatch(1)

        whenever(mockLogLevelStorage.get()).thenReturn("INFO")

        loggerInstance.persistLog(
                LogLevel.TRACE,
                logEntryMock()
        ) { latch.countDown() }
        latch.await()

        verify(shardRepositoryMock, times(0)).add(any())
    }

    @Test
    fun testPersistLog_shouldPersist_whenAppStartEvent() {
        val latch = CountDownLatch(1)
        whenever(mockLogLevelStorage.get()).thenReturn("ERROR")
        loggerInstance.persistLog(
                LogLevel.INFO,
                mock {
                    on { data }.doReturn(mapOf())
                    on { topic }.doReturn("app:start")
                }
        ) { latch.countDown() }
        latch.await()
        verify(shardRepositoryMock, times(1)).add(any())
    }

    private fun logEntryMock(testTopic: String = "", testData: Map<String, Any?> = mapOf()) =
            mock<LogEntry> {
                on { data }.doReturn(testData)
                on { topic }.doReturn(testTopic)
            }

    private fun waitForTask() {
        val latch = CountDownLatch(1)
        coreSdkHandler.post {
            latch.countDown()
        }
        latch.await()
    }
}