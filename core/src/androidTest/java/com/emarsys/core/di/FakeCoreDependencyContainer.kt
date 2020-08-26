package com.emarsys.core.di

import android.os.Handler
import com.emarsys.core.DefaultCoreCompletionHandler
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.activity.CurrentActivityWatchdog
import com.emarsys.core.database.CoreSQLiteDatabase
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.activity.CurrentActivityProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.RestClient
import com.emarsys.core.response.ResponseHandlersProcessor
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.storage.CoreStorageKey
import com.emarsys.core.storage.KeyValueStore
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.util.FileDownloader
import com.emarsys.core.util.log.Logger
import com.nhaarman.mockitokotlin2.mock

class FakeCoreDependencyContainer(coreSdkHandler: Handler = mock(),
                                  uiHandler: Handler = mock(),
                                  activityLifecycleWatchdog: ActivityLifecycleWatchdog = mock(),
                                  currentActivityWatchdog: CurrentActivityWatchdog = mock(),
                                  coreSQLiteDatabase: CoreSQLiteDatabase = mock(),
                                  deviceInfo: DeviceInfo = mock(),
                                  shardRepository: Repository<ShardModel, SqlSpecification> = mock(),
                                  timestampProvider: TimestampProvider = mock(),
                                  uuidProvider: UUIDProvider = mock(),
                                  completionHandler: DefaultCoreCompletionHandler = mock(),
                                  logger: Logger = mock(),
                                  responseHandlersProcessor: ResponseHandlersProcessor = mock(),
                                  restClient: RestClient = mock(),
                                  logLevelStorage: StringStorage = mock(),
                                  fileDownloader: FileDownloader = mock(),
                                  currentActivityProvider: CurrentActivityProvider = mock(),
                                  keyValueStore: KeyValueStore = mock()
) : DependencyContainer {
    override val dependencies: MutableMap<String, Any?> = mutableMapOf()

    init {
        addDependency(dependencies, coreSdkHandler, "coreSdkHandler")
        addDependency(dependencies, uiHandler, "uiHandler")
        addDependency(dependencies, activityLifecycleWatchdog)
        addDependency(dependencies, currentActivityWatchdog)
        addDependency(dependencies, coreSQLiteDatabase)
        addDependency(dependencies, deviceInfo)
        addDependency(dependencies, shardRepository, "shardRepository")
        addDependency(dependencies, timestampProvider)
        addDependency(dependencies, uuidProvider)
        addDependency(dependencies, completionHandler)
        addDependency(dependencies, logger)
        addDependency(dependencies, responseHandlersProcessor)
        addDependency(dependencies, restClient)
        addDependency(dependencies, logLevelStorage, CoreStorageKey.LOG_LEVEL.key)
        addDependency(dependencies, fileDownloader)
        addDependency(dependencies, currentActivityProvider)
        addDependency(dependencies, keyValueStore)

    }

    override fun getCoreSdkHandler(): Handler = getDependency(dependencies, "coreSdkHandler")

    override fun getUiHandler(): Handler = getDependency(dependencies, "uiHandler")

    override fun getActivityLifecycleWatchdog(): ActivityLifecycleWatchdog = getDependency(dependencies)

    override fun getCurrentActivityWatchdog(): CurrentActivityWatchdog = getDependency(dependencies)

    override fun getCoreSQLiteDatabase(): CoreSQLiteDatabase = getDependency(dependencies)

    override fun getDeviceInfo(): DeviceInfo = getDependency(dependencies)

    override fun getTimestampProvider(): TimestampProvider = getDependency(dependencies)

    override fun getUuidProvider(): UUIDProvider = getDependency(dependencies)

    override fun getLogShardTrigger(): Runnable = getDependency(dependencies, "logShardTrigger")

    override fun getLogger(): Logger = getDependency(dependencies)

    override fun getRestClient(): RestClient = getDependency(dependencies)

    override fun getFileDownloader(): FileDownloader = getDependency(dependencies)

    override fun getShardRepository(): Repository<ShardModel, SqlSpecification> = getDependency(dependencies, "shardRepository")

    override fun getKeyValueStore(): KeyValueStore = getDependency(dependencies)
}