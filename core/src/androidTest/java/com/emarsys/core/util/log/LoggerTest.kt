package com.emarsys.core.util.log

import android.content.Context
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
import com.emarsys.core.util.log.entry.MethodNotAllowed
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.mockito.ThreadSpy
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
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

class LoggerTest {

    companion object {
        const val TIMESTAMP = 400L
        const val UUID = "UUID12345"
        const val TTL = Long.MAX_VALUE
    }


    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var mockShardRepository: Repository<ShardModel, SqlSpecification>
    private lateinit var timestampProviderMock: TimestampProvider
    private lateinit var uuidProviderMock: UUIDProvider
    private lateinit var dependencyContainer: CoreComponent
    private lateinit var loggerInstance: Logger
    private lateinit var loggerMock: Logger
    private lateinit var mockLogLevelStorage: StringStorage
    private lateinit var context: Context

    @Before
    @Suppress("UNCHECKED_CAST")
    fun init() {
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        mockShardRepository = mock()
        timestampProviderMock = mock<TimestampProvider>().apply {
            whenever(provideTimestamp()).thenReturn(TIMESTAMP)
        }
        uuidProviderMock = mock<UUIDProvider>().apply {
            whenever(provideId()).thenReturn(UUID)
        }
        mockLogLevelStorage = mock()

        context = InstrumentationRegistry.getTargetContext()
        loggerInstance = Logger(
            concurrentHandlerHolder,
            mockShardRepository,
            timestampProviderMock,
            uuidProviderMock,
            mockLogLevelStorage,
            false,
            context
        )
        loggerMock = mock()

        dependencyContainer = FakeCoreDependencyContainer(
            concurrentHandlerHolder = concurrentHandlerHolder,
            shardRepository = mockShardRepository,
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
        loggerInstance.handleLog(
            LogLevel.INFO, logEntryMock(
                "log_request", mapOf(
                    "key1" to "value",
                    "key2" to 3,
                    "key3" to true
                )
            ), null
        )
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
            verify(mockShardRepository, timeout(100)).add(capture<ShardModel>(captor))
        }
        val expected = ShardModel(
            UUID,
            "log_request",
            mapOf(
                "key1" to "value",
                "key2" to 3,
                "key3" to true,
                "level" to "ERROR",
                "thread" to "testThreadName",
                "breadcrumbs" to listOf("topic='log_request', data={key1=value, key2=3, key3=true}"),
            ),
            TIMESTAMP,
            TTL
        )
        val result = captor.value
        result.id shouldBe expected.id
        result.timestamp shouldBe expected.timestamp
        result.ttl shouldBe expected.ttl
        result.data shouldBe expected.data
    }

    @Test
    fun testPersistLog_DoesntAddBreadcumbs_toNonErrorLog() {
        whenever(mockLogLevelStorage.get()).thenReturn("INFO")

        for (i in 0..13) {
            loggerInstance.handleLog(
                LogLevel.DEBUG, logEntryMock(
                    "log_request", mapOf(
                        "key$i" to "value",
                        "key2" to 3,
                        "key3" to true
                    )
                ), null
            )
        }
        loggerInstance.persistLog(
            LogLevel.INFO, logEntryMock(
                "log_request", mapOf(
                    "key14" to "value",
                    "key2" to 3,
                    "key3" to true
                )
            ), "testThreadName", null
        )

        val captor = ArgumentCaptor.forClass(ShardModel::class.java)

        runBlocking {
            verify(mockShardRepository, timeout(100)).add(capture<ShardModel>(captor))
        }
        val expected = ShardModel(
            UUID,
            "log_request",
            mapOf(
                "key14" to "value",
                "key2" to 3,
                "key3" to true,
                "level" to "INFO",
                "thread" to "testThreadName"
            ),
            TIMESTAMP,
            TTL
        )
        val result = captor.value
        result.id shouldBe expected.id
        result.timestamp shouldBe expected.timestamp
        result.ttl shouldBe expected.ttl
        result.data shouldBe expected.data
    }

    @Test
    fun testPersistLog_addsBreadcumbs_toErrorLog() {

        for (i in 0..13) {
            loggerInstance.handleLog(
                LogLevel.INFO, logEntryMock(
                    "log_request", mapOf(
                        "key$i" to "value",
                        "key2" to 3,
                        "key3" to true
                    )
                ), null
            )
        }
        loggerInstance.persistLog(
            LogLevel.ERROR, logEntryMock(
                "log_request", mapOf(
                    "key14" to "value",
                    "key2" to 3,
                    "key3" to true
                )
            ), "testThreadName", null
        )

        val captor = ArgumentCaptor.forClass(ShardModel::class.java)

        runBlocking {
            verify(mockShardRepository, timeout(100)).add(capture<ShardModel>(captor))
        }
        val expected = ShardModel(
            UUID,
            "log_request",
            mapOf(
                "key14" to "value",
                "key2" to 3,
                "key3" to true,
                "level" to "ERROR",
                "thread" to "testThreadName",
                "breadcrumbs" to listOf(
                    "topic='log_request', data={key13=value, key2=3, key3=true}",
                    "topic='log_request', data={key12=value, key2=3, key3=true}",
                    "topic='log_request', data={key11=value, key2=3, key3=true}",
                    "topic='log_request', data={key10=value, key2=3, key3=true}",
                    "topic='log_request', data={key9=value, key2=3, key3=true}",
                    "topic='log_request', data={key8=value, key2=3, key3=true}",
                    "topic='log_request', data={key7=value, key2=3, key3=true}",
                    "topic='log_request', data={key6=value, key2=3, key3=true}",
                    "topic='log_request', data={key5=value, key2=3, key3=true}",
                    "topic='log_request', data={key4=value, key2=3, key3=true}",
                    "topic='log_request', data={key3=true, key2=3}"
                ),
            ),
            TIMESTAMP,
            TTL
        )
        val result = captor.value
        result.id shouldBe expected.id
        result.timestamp shouldBe expected.timestamp
        result.ttl shouldBe expected.ttl
        result.data shouldBe expected.data
    }

    @Test
    fun testPersistLog_shouldNotAddLog_toShardRepository_whenLogEntryIsMethodNotAllowed() {
        val methodNotAllowedLog = MethodNotAllowed(this::class.java, "testMethodName", mapOf())

        loggerInstance.handleLog(LogLevel.ERROR, methodNotAllowedLog, null)

        waitForTask()

        verify(mockShardRepository, times(0)).add(any())
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
            verify(mockShardRepository, timeout(100)).add(capture<ShardModel>(captor))
        }
        val expected = ShardModel(
            UUID,
            "log_request",
            mapOf(

                "level" to "ERROR",
                "thread" to "testThreadName",
                "breadcrumbs" to listOf<String>(),
            ),
            TIMESTAMP,
            TTL
        )
        val result = captor.value
        result.id shouldBe expected.id
        result.timestamp shouldBe expected.timestamp
        result.ttl shouldBe expected.ttl
        result.data shouldBe expected.data
    }

    @Test
    fun testPersistLog_addsLog_toShardRepository_viaCoreSdkHandler() {
        val threadSpy = ThreadSpy<Unit>()
        runBlocking {
            org.mockito.Mockito.doAnswer(threadSpy).`when`(mockShardRepository).add(any())
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
            verify(mockShardRepository, timeout(100).times(0)).add(any())
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
            verify(mockShardRepository, times(0)).add(any())
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
            verify(mockShardRepository, times(1)).add(any())
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