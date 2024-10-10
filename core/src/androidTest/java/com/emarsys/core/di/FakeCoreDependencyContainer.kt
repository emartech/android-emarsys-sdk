package com.emarsys.core.di

import android.content.SharedPreferences
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.activity.ActivityLifecycleActionRegistry
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.activity.TransitionSafeCurrentActivityWatchdog
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.connection.ConnectionWatchDog
import com.emarsys.core.crypto.Crypto
import com.emarsys.core.database.CoreSQLiteDatabase
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.RestClient
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.storage.KeyValueStore
import com.emarsys.core.storage.Storage
import com.emarsys.core.util.FileDownloader
import com.emarsys.core.util.log.Logger
import com.emarsys.core.worker.Worker
import org.mockito.kotlin.mock

class FakeCoreDependencyContainer(
    override val concurrentHandlerHolder: ConcurrentHandlerHolder = ConcurrentHandlerHolderFactory.create(),
    override val activityLifecycleWatchdog: ActivityLifecycleWatchdog = mock(),
    override val coreSQLiteDatabase: CoreSQLiteDatabase = mock(),
    override val deviceInfo: DeviceInfo = mock(),
    override val shardRepository: Repository<ShardModel, SqlSpecification> = mock(),
    override val timestampProvider: TimestampProvider = mock(),
    override val uuidProvider: UUIDProvider = mock(),
    override val logShardTrigger: Runnable = mock(),
    override val logger: Logger = mock(),
    override val restClient: RestClient = mock(),
    override val fileDownloader: FileDownloader = mock(),
    override val keyValueStore: KeyValueStore = mock(),
    override val sharedPreferences: SharedPreferences = mock(),
    override val sharedPreferencesV3: SharedPreferences = mock(),
    override val hardwareIdProvider: HardwareIdProvider = mock(),
    override val coreDbHelper: CoreDbHelper = mock(),
    override val hardwareIdStorage: Storage<String?> = mock(),
    override val crypto: Crypto = mock(),
    override val requestManager: RequestManager = mock(),
    override val worker: Worker = mock(),
    override val requestModelRepository: Repository<RequestModel, SqlSpecification> = mock(),
    override val connectionWatchdog: ConnectionWatchDog = mock(),
    override val coreCompletionHandler: CoreCompletionHandler = mock(),
    override val logLevelStorage: Storage<String?> = mock(),
    override val activityLifecycleActionRegistry: ActivityLifecycleActionRegistry = mock(),
    override val transitionSafeCurrentActivityWatchdog: TransitionSafeCurrentActivityWatchdog = mock(),
) : CoreComponent