package com.emarsys.di

import android.os.Handler
import com.emarsys.core.DefaultCoreCompletionHandler
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
import com.emarsys.core.response.ResponseHandlersProcessor
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.storage.Storage
import com.emarsys.core.util.log.Logger
import com.emarsys.inapp.InAppApi
import com.emarsys.inbox.InboxApi
import com.emarsys.mobileengage.MobileEngageInternal
import com.emarsys.mobileengage.RefreshTokenInternal
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.api.NotificationEventHandler
import com.emarsys.mobileengage.client.ClientServiceInternal
import com.emarsys.mobileengage.deeplink.DeepLinkInternal
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.iam.InAppInternal
import com.emarsys.mobileengage.iam.InAppPresenter
import com.emarsys.mobileengage.inbox.InboxInternal
import com.emarsys.mobileengage.inbox.model.NotificationCache
import com.emarsys.mobileengage.push.PushInternal
import com.emarsys.predict.PredictApi
import com.emarsys.predict.PredictInternal
import com.emarsys.push.PushApi
import org.mockito.Mockito.mock

@Suppress("UNCHECKED_CAST")
class FakeDependencyContainer(
        private val coreSdkHandler: Handler = mock(Handler::class.java),
        private val activityLifecycleWatchdog: ActivityLifecycleWatchdog = mock(ActivityLifecycleWatchdog::class.java),
        private val currentActivityWatchdog: CurrentActivityWatchdog = mock(CurrentActivityWatchdog::class.java),
        private val coreSQLiteDatabase: CoreSQLiteDatabase = mock(CoreSQLiteDatabase::class.java),
        private val deviceInfo: DeviceInfo = mock(DeviceInfo::class.java),
        private val shardRepository: Repository<ShardModel, SqlSpecification> = mock(Repository::class.java) as Repository<ShardModel, SqlSpecification>,
        private val timestampProvider: TimestampProvider = mock(TimestampProvider::class.java),
        private val uuidProvider: UUIDProvider = mock(UUIDProvider::class.java),
        private val logShardTrigger: Runnable = mock(Runnable::class.java),
        private val mobileEngageInternal: MobileEngageInternal = mock(MobileEngageInternal::class.java),
        private val pushInternal: PushInternal = mock(PushInternal::class.java),
        private val inboxInternal: InboxInternal = mock(InboxInternal::class.java),
        private val inAppInternal: InAppInternal = mock(InAppInternal::class.java),
        private val refreshTokenInternal: RefreshTokenInternal = mock(RefreshTokenInternal::class.java),
        private val deepLinkInternal: DeepLinkInternal = mock(DeepLinkInternal::class.java),
        private val eventServiceInternal: EventServiceInternal = mock(EventServiceInternal::class.java),
        private val clientServiceInternal: ClientServiceInternal = mock(ClientServiceInternal::class.java),
        private val completionHandler: DefaultCoreCompletionHandler = mock(DefaultCoreCompletionHandler::class.java),
        private val requestContext: MobileEngageRequestContext = mock(MobileEngageRequestContext::class.java),
        private val inAppPresenter: InAppPresenter = mock(InAppPresenter::class.java),
        private val notificationEventHandler: NotificationEventHandler = mock(NotificationEventHandler::class.java),
        private val predictInternal: PredictInternal = mock(PredictInternal::class.java),
        private val predictShardTrigger: Runnable = mock(Runnable::class.java),
        private val runnerProxy: RunnerProxy = mock(RunnerProxy::class.java),
        private val logger: Logger = mock(Logger::class.java),
        private val deviceInfoHashStorage: Storage<Int> = mock(Storage::class.java) as Storage<Int>,
        private val contactFieldValueStorage: Storage<String> = mock(Storage::class.java) as Storage<String>,
        private val contactTokenStorage: Storage<String> = mock(Storage::class.java) as Storage<String>,
        private val clientStateStorage: Storage<String> = mock(Storage::class.java) as Storage<String>,
        private val responseHandlersProcessor: ResponseHandlersProcessor = mock(ResponseHandlersProcessor::class.java),
        private val notificationCache: NotificationCache = mock(NotificationCache::class.java),
        private val restClient: RestClient = mock(RestClient::class.java),
        private val inbox: InboxApi = mock(InboxApi::class.java),
        private val inApp: InAppApi = mock(InAppApi::class.java),
        private val push: PushApi = mock(PushApi::class.java),
        private val predict: PredictApi = mock(PredictApi::class.java)) : EmarysDependencyContainer {

    override fun getCoreSdkHandler(): Handler {
        return coreSdkHandler
    }

    override fun getActivityLifecycleWatchdog(): ActivityLifecycleWatchdog {
        return activityLifecycleWatchdog
    }

    override fun getCurrentActivityWatchdog(): CurrentActivityWatchdog {
        return currentActivityWatchdog
    }

    override fun getCoreSQLiteDatabase(): CoreSQLiteDatabase {
        return coreSQLiteDatabase
    }

    override fun getDeviceInfo(): DeviceInfo {
        return deviceInfo
    }

    override fun getShardRepository(): Repository<ShardModel, SqlSpecification> {
        return shardRepository
    }

    override fun getTimestampProvider(): TimestampProvider {
        return timestampProvider
    }

    override fun getUuidProvider(): UUIDProvider {
        return uuidProvider
    }

    override fun getLogShardTrigger(): Runnable {
        return logShardTrigger
    }

    override fun getMobileEngageInternal(): MobileEngageInternal {
        return mobileEngageInternal
    }

    override fun getRefreshTokenInternal(): RefreshTokenInternal {
        return refreshTokenInternal
    }

    override fun getClientServiceInternal(): ClientServiceInternal {
        return clientServiceInternal
    }

    override fun getInboxInternal(): InboxInternal {
        return inboxInternal
    }

    override fun getInAppInternal(): InAppInternal {
        return inAppInternal
    }

    override fun getPredictInternal(): PredictInternal {
        return predictInternal
    }

    override fun getPushInternal(): PushInternal {
        return pushInternal
    }

    override fun getEventServiceInternal(): EventServiceInternal {
        return eventServiceInternal
    }

    override fun getDeepLinkInternal(): DeepLinkInternal {
        return deepLinkInternal
    }

    override fun getCoreCompletionHandler(): DefaultCoreCompletionHandler {
        return completionHandler
    }

    override fun getRequestContext(): MobileEngageRequestContext {
        return requestContext
    }

    override fun getInAppPresenter(): InAppPresenter {
        return inAppPresenter
    }

    override fun getNotificationEventHandler(): NotificationEventHandler {
        return notificationEventHandler
    }

    override fun getPredictShardTrigger(): Runnable {
        return predictShardTrigger
    }

    override fun getRunnerProxy(): RunnerProxy {
        return runnerProxy
    }

    override fun getLogger(): Logger {
        return logger
    }

    override fun getRestClient(): RestClient {
        return restClient
    }

    override fun getDeviceInfoHashStorage(): Storage<Int> {
        return deviceInfoHashStorage
    }

    override fun getContactFieldValueStorage(): Storage<String> {
        return contactFieldValueStorage
    }

    override fun getContactTokenStorage(): Storage<String> {
        return contactTokenStorage
    }

    override fun getClientStateStorage(): Storage<String> {
        return clientStateStorage
    }

    override fun getResponseHandlersProcessor(): ResponseHandlersProcessor {
        return responseHandlersProcessor
    }

    override fun getNotificationCache(): NotificationCache {
        return notificationCache
    }


    override fun getInbox(): InboxApi {
        return inbox
    }

    override fun getInApp(): InAppApi {
        return inApp
    }

    override fun getPush(): PushApi {
        return push
    }

    override fun getPredict(): PredictApi {
        return predict
    }
}
