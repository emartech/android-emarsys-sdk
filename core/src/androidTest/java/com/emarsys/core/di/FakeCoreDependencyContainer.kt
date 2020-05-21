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
import com.emarsys.core.shard.ShardModelRepository
import com.emarsys.core.storage.CoreStorageKey
import com.emarsys.core.storage.KeyValueStore
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.util.FileDownloader
import com.emarsys.core.util.log.Logger
import com.nhaarman.mockitokotlin2.mock

open class FakeCoreDependencyContainer(coreSdkHandler: Handler = mock(),
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
    init {
        Container.addDependency(coreSdkHandler, "coreSdkHandler")
        Container.addDependency(activityLifecycleWatchdog)
        Container.addDependency(currentActivityWatchdog)
        Container.addDependency(coreSQLiteDatabase)
        Container.addDependency(deviceInfo)
        Container.addDependency(shardRepository, "shardRepository")
        Container.addDependency(timestampProvider)
        Container.addDependency(uuidProvider)
        Container.addDependency(completionHandler)
        Container.addDependency(logger)
        Container.addDependency(responseHandlersProcessor)
        Container.addDependency(restClient)
        Container.addDependency(logLevelStorage, CoreStorageKey.LOG_LEVEL.key)
        Container.addDependency(fileDownloader)
        Container.addDependency(currentActivityProvider)
        Container.addDependency(keyValueStore)

    }

    override fun getCoreSdkHandler(): Handler = Container.getDependency("coreSdkHandler")

    override fun getActivityLifecycleWatchdog(): ActivityLifecycleWatchdog = Container.getDependency()

    override fun getCurrentActivityWatchdog(): CurrentActivityWatchdog = Container.getDependency()

    override fun getCoreSQLiteDatabase(): CoreSQLiteDatabase = Container.getDependency()

    override fun getDeviceInfo(): DeviceInfo = Container.getDependency()

    override fun getTimestampProvider(): TimestampProvider = Container.getDependency()

    override fun getUuidProvider(): UUIDProvider = Container.getDependency()

    override fun getLogShardTrigger(): Runnable = Container.getDependency("logShardTrigger")

    override fun getLogger(): Logger = Container.getDependency()

    override fun getRestClient(): RestClient = Container.getDependency()

    override fun getFileDownloader(): FileDownloader = Container.getDependency()

    override fun getShardRepository(): Repository<ShardModel, SqlSpecification> = Container.getDependency<ShardModelRepository>()

    override fun getKeyValueStore(): KeyValueStore = Container.getDependency()
}