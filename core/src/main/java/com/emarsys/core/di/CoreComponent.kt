package com.emarsys.core.di

import android.content.SharedPreferences
import android.os.Handler
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.activity.ActivityLifecycleActionRegistry
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.activity.CurrentActivityWatchdog
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

fun core() = CoreComponent.instance
    ?: throw IllegalStateException("DependencyContainer has to be setup first!")

fun setupCoreComponent(coreComponent: CoreComponent) {
    CoreComponent.instance = coreComponent
}

fun tearDownCoreComponent() {
    CoreComponent.instance = null
}

fun isCoreComponentSetup() =
    CoreComponent.instance != null


interface CoreComponent {
    companion object {
        var instance: CoreComponent? = null

        fun isSetup() = instance != null
    }

    val concurrentHandlerHolder: ConcurrentHandlerHolder

    val uiHandler: Handler

    val activityLifecycleWatchdog: ActivityLifecycleWatchdog

    val currentActivityWatchdog: CurrentActivityWatchdog

    val activityLifecycleActionRegistry: ActivityLifecycleActionRegistry

    val coreSQLiteDatabase: CoreSQLiteDatabase

    val deviceInfo: DeviceInfo

    val shardRepository: Repository<ShardModel, SqlSpecification>

    val timestampProvider: TimestampProvider

    val uuidProvider: UUIDProvider

    val logShardTrigger: Runnable

    val logger: Logger

    val restClient: RestClient

    val fileDownloader: FileDownloader

    val keyValueStore: KeyValueStore

    val sharedPreferences: SharedPreferences

    val hardwareIdProvider: HardwareIdProvider

    val coreDbHelper: CoreDbHelper

    val hardwareIdStorage: Storage<String?>

    val logLevelStorage: Storage<String?>

    val crypto: Crypto

    val requestManager: RequestManager

    val worker: Worker

    val requestModelRepository: Repository<RequestModel, SqlSpecification>

    val connectionWatchdog: ConnectionWatchDog

    val coreCompletionHandler: CoreCompletionHandler
}