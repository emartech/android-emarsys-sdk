package com.emarsys.core.util.log

import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.di.CoreComponent
import com.emarsys.core.di.FakeCoreDependencyContainer
import com.emarsys.core.di.core
import com.emarsys.core.di.setupCoreComponent
import com.emarsys.core.di.tearDownCoreComponent
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.util.log.entry.LogEntry
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.mockito.ThreadSpy
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.capture
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.timeout
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.concurrent.CountDownLatch


class LoggerTest : AnnotationSpec() {

    companion object {
        const val TIMESTAMP = 400L
        const val UUID = "UUID12345"
        const val TTL = Long.MAX_VALUE
    }


    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var shardRepositoryMock: Repository<ShardModel, SqlSpecification>
    private lateinit var timestampProviderMock: TimestampProvider
    private lateinit var uuidProviderMock: UUIDProvider
    private lateinit var dependencyContainer: CoreComponent
    private lateinit var loggerInstance: Logger
    private lateinit var loggerMock: Logger
    private lateinit var mockLogLevelStorage: StringStorage

    @Before
    @Suppress("UNCHECKED_CAST")
    fun init() {
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        shardRepositoryMock = mock()
        timestampProviderMock = mock<TimestampProvider>().apply {
            whenever(provideTimestamp()).thenReturn(TIMESTAMP)
        }
        uuidProviderMock = mock<UUIDProvider>().apply {
            whenever(provideId()).thenReturn(UUID)
        }
        mockLogLevelStorage = mock()

        loggerInstance = Logger(
            concurrentHandlerHolder,
            shardRepositoryMock,
            timestampProviderMock,
            uuidProviderMock,
            mockLogLevelStorage,
            false,
            mock()
        )
        loggerMock = mock()

        dependencyContainer = FakeCoreDependencyContainer(
            concurrentHandlerHolder = concurrentHandlerHolder,
            shardRepository = shardRepositoryMock,
            timestampProvider = timestampProviderMock,
            uuidProvider = uuidProviderMock,
            logger = loggerMock
        )
        setupCoreComponent(dependencyContainer)
    }

    @After
    fun tearDown() {
        if (CoreComponent.isSetup()) {
            core().concurrentHandlerHolder.coreLooper.quitSafely()
            tearDownCoreComponent()
        }
    }

    @Test
    fun testPersistLog_addsLog_toShardRepository() {
        loggerInstance.persistLog(
            LogLevel.ERROR, logEntryMock(
                "log_request", mapOf(
                    "key1" to "value",
                    "key2" to 3,
                    "key3" to true
                )
            ), "testThreadName", null
        )

        val captor = ArgumentCaptor.forClass(ShardModel::class.java)

        runBlocking {
            verify(shardRepositoryMock, timeout(100)).add(capture<ShardModel>(captor))
        }

        captor.value shouldBe ShardModel(
            UUID,
            "log_request",
            mapOf(
                "key1" to "value",
                "key2" to 3,
                "key3" to true,
                "level" to "ERROR",
                "thread" to "testThreadName"
            ),
            TIMESTAMP,
            TTL
        )
    }

    @Test
    fun testPersistLog_addsOtherLog_toShardRepository() {
        loggerInstance.persistLog(
            LogLevel.ERROR,
            logEntryMock("any_log", mapOf()),
            "testThreadName",
            null
        )

        val captor = ArgumentCaptor.forClass(ShardModel::class.java)
        runBlocking {
            verify(shardRepositoryMock, timeout(100)).add(capture<ShardModel>(captor))
        }
        captor.value shouldBe ShardModel(
            UUID,
            "any_log",
            mapOf(
                "level" to "ERROR",
                "thread" to "testThreadName"
            ),
            TIMESTAMP,
            TTL
        )
    }

    @Test
    fun testPersistLog_addsLog_toShardRepository_viaCoreSdkHandler() {
        val threadSpy = ThreadSpy<Unit>()
        runBlocking {
            org.mockito.Mockito.doAnswer(threadSpy).`when`(shardRepositoryMock).add(any())
        }
        loggerInstance.persistLog(LogLevel.ERROR, logEntryMock(), "testThreadName", null)

        threadSpy.verifyCalledOnCoreSdkThread()
    }

    @Test
    fun testLog_delegatesToInstance_withINFOLogLevel() {
        whenever(mockLogLevelStorage.get()).thenReturn("INFO")

        val logEntry =
            logEntryMock(testTopic = "testTopic", testData = mapOf("testKey" to "testValue"))

        Logger.log(logEntry)

        waitForTask()

        verify(loggerMock).handleLog(LogLevel.INFO, logEntry)
    }

    @Test
    fun testLog_doesNotLogAnything_ifDependencyInjection_isNotSetup() {
        tearDownCoreComponent()

        dependencyContainer = mock()

        setupCoreComponent(dependencyContainer)
        tearDownCoreComponent()

        Logger.log(logEntryMock())

        waitForTask()

        verifyNoInteractions(dependencyContainer)
    }

    @Test
    fun testPersistLog_shouldNotPersist_whenLoggingOurLog() {
        val latch = CountDownLatch(1)

        loggerInstance.persistLog(
            LogLevel.INFO,
            logEntryMock(
                "log_request",
                mapOf(
                    "url" to "https://log-dealer.eservice.emarsys.net/v1/log",
                    "key2" to 3,
                    "key3" to true
                )
            ),
            "testThreadName"
        ) { latch.countDown() }
        latch.await()
        runBlocking {
            verify(shardRepositoryMock, timeout(100).times(0)).add(any())
        }
    }

    @Test
    fun testPersistLog_shouldNotPersist_whenLogLevelIsBelowStoredLogLevel() {
        val latch = CountDownLatch(1)

        whenever(mockLogLevelStorage.get()).thenReturn("INFO")

        loggerInstance.persistLog(
            LogLevel.TRACE,
            logEntryMock(),
            "testThreadName"
        ) { latch.countDown() }
        latch.await()
        runBlocking {
            verify(shardRepositoryMock, times(0)).add(any())
        }
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
            },
            "testThreadName"
        ) { latch.countDown() }
        latch.await()
        runBlocking {
            verify(shardRepositoryMock, times(1)).add(any())
        }
    }

    private fun logEntryMock(testTopic: String = "", testData: Map<String, Any?> = mapOf()) =
        mock<LogEntry> {
            on { data }.doReturn(testData)
            on { topic }.doReturn(testTopic)
        }

    private fun waitForTask() {
        val latch = CountDownLatch(1)
        concurrentHandlerHolder.coreHandler.post {
            latch.countDown()
        }
        latch.await()
    }
}