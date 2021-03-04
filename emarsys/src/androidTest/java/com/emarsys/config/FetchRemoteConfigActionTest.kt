package com.emarsys.config

import android.os.Handler
import android.os.Looper
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.activity.CurrentActivityWatchdog
import com.emarsys.core.concurrency.CoreSdkHandlerProvider
import com.emarsys.core.database.CoreSQLiteDatabase
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.di.DependencyContainer
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.RestClient
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.storage.KeyValueStore
import com.emarsys.core.util.FileDownloader
import com.emarsys.core.util.log.Logger
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.timeout
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.verify

class FetchRemoteConfigActionTest {

    private lateinit var fetchAction: FetchRemoteConfigAction
    private lateinit var mockConfigInternal: ConfigInternal

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setup() {
        mockConfigInternal = mock()

        DependencyInjection.setup(object : DependencyContainer {
            override fun getCoreSdkHandler(): CoreSdkHandler {
                return CoreSdkHandlerProvider().provideHandler()
            }

            override fun getUiHandler(): Handler {
                return Handler(Looper.getMainLooper())
            }

            override fun getActivityLifecycleWatchdog(): ActivityLifecycleWatchdog {
                return mock()
            }

            override fun getCurrentActivityWatchdog(): CurrentActivityWatchdog {
                return mock()
            }

            override fun getCoreSQLiteDatabase(): CoreSQLiteDatabase {
                return mock()
            }

            override fun getDeviceInfo(): DeviceInfo {
                return mock()
            }

            override fun getShardRepository(): Repository<ShardModel, SqlSpecification> {
                return mock()
            }

            override fun getTimestampProvider(): TimestampProvider {
                return mock()
            }

            override fun getUuidProvider(): UUIDProvider {
                return mock()
            }

            override fun getLogShardTrigger(): Runnable {
                return mock()
            }

            override fun getLogger(): Logger {
                return mock()
            }

            override fun getRestClient(): RestClient {
                return mock()
            }

            override fun getFileDownloader(): FileDownloader {
                return mock()
            }

            override fun getKeyValueStore(): KeyValueStore {
                return mock()
        }

            override val dependencies: MutableMap<String, Any?>
                get() = mock()
        })

        fetchAction = FetchRemoteConfigAction(mockConfigInternal)
    }

    @After
    fun tearDown() {
        DependencyInjection.tearDown()
    }

    @Test
    fun testExecute_invokesConfigInternalsRefreshRemoteConfigMethod() {
        fetchAction.execute(null)
        verify(mockConfigInternal, timeout(100)).refreshRemoteConfig(null)
    }

}