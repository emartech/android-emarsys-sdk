package com.emarsys.core.di

import android.os.Handler
import com.emarsys.core.RunnerProxy
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.activity.CurrentActivityWatchdog
import com.emarsys.core.database.CoreSQLiteDatabase
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.RestClient
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.storage.KeyValueStore
import com.emarsys.core.util.FileDownloader
import com.emarsys.core.util.log.Logger


interface DependencyContainer {

    fun getCoreSdkHandler(): Handler

    fun getActivityLifecycleWatchdog(): ActivityLifecycleWatchdog

    fun getCurrentActivityWatchdog(): CurrentActivityWatchdog

    fun getCoreSQLiteDatabase(): CoreSQLiteDatabase

    fun getDeviceInfo(): DeviceInfo

    fun getShardRepository(): Repository<ShardModel, SqlSpecification>

    fun getTimestampProvider(): TimestampProvider

    fun getUuidProvider(): UUIDProvider

    fun getLogShardTrigger(): Runnable

    fun getRunnerProxy(): RunnerProxy

    fun getLogger(): Logger

    fun getRestClient(): RestClient

    fun getFileDownloader(): FileDownloader

    fun getKeyValueStore(): KeyValueStore
}