package com.emarsys.di

import android.os.Handler
import com.emarsys.config.ConfigApi
import com.emarsys.config.ConfigInternal
import com.emarsys.core.DefaultCoreCompletionHandler
import com.emarsys.core.RunnerProxy
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.activity.CurrentActivityWatchdog
import com.emarsys.core.database.CoreSQLiteDatabase
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.RestClient
import com.emarsys.core.response.ResponseHandlersProcessor
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.storage.Storage
import com.emarsys.core.util.log.Logger
import com.emarsys.inapp.InAppApi
import com.emarsys.inbox.InboxApi
import com.emarsys.mobileengage.*
import com.emarsys.mobileengage.api.NotificationEventHandler
import com.emarsys.mobileengage.client.ClientServiceInternal
import com.emarsys.mobileengage.client.DefaultClientServiceInternal
import com.emarsys.mobileengage.client.LoggingClientServiceInternal
import com.emarsys.mobileengage.deeplink.DeepLinkInternal
import com.emarsys.mobileengage.deeplink.DefaultDeepLinkInternal
import com.emarsys.mobileengage.deeplink.LoggingDeepLinkInternal
import com.emarsys.mobileengage.event.DefaultEventServiceInternal
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.event.LoggingEventServiceInternal
import com.emarsys.mobileengage.iam.DefaultInAppInternal
import com.emarsys.mobileengage.iam.InAppInternal
import com.emarsys.mobileengage.iam.InAppPresenter
import com.emarsys.mobileengage.iam.LoggingInAppInternal
import com.emarsys.mobileengage.inbox.DefaultInboxInternal
import com.emarsys.mobileengage.inbox.InboxInternal
import com.emarsys.mobileengage.inbox.LoggingInboxInternal
import com.emarsys.mobileengage.inbox.model.NotificationCache
import com.emarsys.mobileengage.push.DefaultPushInternal
import com.emarsys.mobileengage.push.LoggingPushInternal
import com.emarsys.mobileengage.push.PushInternal
import com.emarsys.mobileengage.push.PushTokenProvider
import com.emarsys.predict.DefaultPredictInternal
import com.emarsys.predict.LoggingPredictInternal
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
        private val mobileEngageInternal: MobileEngageInternal = mock(DefaultMobileEngageInternal::class.java),
        private val loggingMobileEngageInternal: MobileEngageInternal = mock(LoggingMobileEngageInternal::class.java),
        private val pushInternal: PushInternal = mock(DefaultPushInternal::class.java),
        private val loggingPushInternal: PushInternal = mock(LoggingPushInternal::class.java),
        private val inboxInternal: InboxInternal = mock(DefaultInboxInternal::class.java),
        private val loggingInboxInternal: InboxInternal = mock(LoggingInboxInternal::class.java),
        private val inAppInternal: InAppInternal = mock(DefaultInAppInternal::class.java),
        private val loggingInAppInternal: InAppInternal = mock(LoggingInAppInternal::class.java),
        private val deepLinkInternal: DeepLinkInternal = mock(DefaultDeepLinkInternal::class.java),
        private val loggingDeepLinkInternal: DeepLinkInternal = mock(LoggingDeepLinkInternal::class.java),
        private val eventServiceInternal: EventServiceInternal = mock(DefaultEventServiceInternal::class.java),
        private val loggingEventServiceInternal: EventServiceInternal = mock(LoggingEventServiceInternal::class.java),
        private val clientServiceInternal: ClientServiceInternal = mock(DefaultClientServiceInternal::class.java),
        private val loggingClientServiceInternal: ClientServiceInternal = mock(LoggingClientServiceInternal::class.java),
        private val predictInternal: PredictInternal = mock(DefaultPredictInternal::class.java),
        private val loggingPredictInternal: PredictInternal = mock(LoggingPredictInternal::class.java),
        private val refreshTokenInternal: RefreshTokenInternal = mock(RefreshTokenInternal::class.java),
        private val completionHandler: DefaultCoreCompletionHandler = mock(DefaultCoreCompletionHandler::class.java),
        private val requestContext: MobileEngageRequestContext = mock(MobileEngageRequestContext::class.java),
        private val inAppPresenter: InAppPresenter = mock(InAppPresenter::class.java),
        private val notificationEventHandler: NotificationEventHandler = mock(NotificationEventHandler::class.java),
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
        private val loggingInbox: InboxApi = mock(InboxApi::class.java),
        private val inApp: InAppApi = mock(InAppApi::class.java),
        private val loggingInApp: InAppApi = mock(InAppApi::class.java),
        private val push: PushApi = mock(PushApi::class.java),
        private val loggingPush: PushApi = mock(PushApi::class.java),
        private val predict: PredictApi = mock(PredictApi::class.java),
        private val loggingPredict: PredictApi = mock(PredictApi::class.java),
        private val config: ConfigApi = mock(ConfigApi::class.java),
        private val pushTokenProvider: PushTokenProvider = mock(PushTokenProvider::class.java),
        private val clientServiceProvider: ServiceEndpointProvider = mock(ServiceEndpointProvider::class.java),
        private val eventServiceProvider: ServiceEndpointProvider = mock(ServiceEndpointProvider::class.java),
        private val deepLinkServiceProvider: ServiceEndpointProvider = mock(ServiceEndpointProvider::class.java),
        private val mobileEngageV2ServiceProvider: ServiceEndpointProvider = mock(ServiceEndpointProvider::class.java),
        private val inboxServiceProvider: ServiceEndpointProvider = mock(ServiceEndpointProvider::class.java),
        private val predictServiceProvider: ServiceEndpointProvider = mock(ServiceEndpointProvider::class.java),
        private val configInternal: ConfigInternal = mock(ConfigInternal::class.java),
        private val clientServiceStorage: Storage<String> = mock(Storage::class.java) as Storage<String>,
        private val eventServiceStorage: Storage<String> = mock(Storage::class.java) as Storage<String>,
        private val deepLinkServiceStorage: Storage<String> = mock(Storage::class.java) as Storage<String>,
        private val mobileEngageV2ServiceStorage: Storage<String> = mock(Storage::class.java) as Storage<String>,
        private val inboxServiceStorage: Storage<String> = mock(Storage::class.java) as Storage<String>,
        private val predictServiceStorage: Storage<String> = mock(Storage::class.java) as Storage<String>
) : EmarsysDependencyContainer {

    override fun getLoggingClientServiceInternal(): ClientServiceInternal {
        return loggingClientServiceInternal
    }

    override fun getLoggingInboxInternal(): InboxInternal {
        return loggingInboxInternal
    }

    override fun getLoggingInAppInternal(): InAppInternal {
        return loggingInAppInternal
    }

    override fun getLoggingDeepLinkInternal(): DeepLinkInternal {
        return loggingDeepLinkInternal
    }

    override fun getLoggingPushInternal(): PushInternal {
        return loggingPushInternal
    }

    override fun getLoggingEventServiceInternal(): EventServiceInternal {
        return loggingEventServiceInternal
    }

    override fun getLoggingPredictInternal(): PredictInternal {
        return loggingPredictInternal
    }

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

    override fun getLoggingMobileEngageInternal(): MobileEngageInternal {
        return loggingMobileEngageInternal
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

    override fun getLoggingInbox(): InboxApi {
        return loggingInbox
    }

    override fun getInApp(): InAppApi {
        return inApp
    }

    override fun getLoggingInApp(): InAppApi {
        return loggingInApp
    }

    override fun getPush(): PushApi {
        return push
    }

    override fun getLoggingPush(): PushApi {
        return loggingPush
    }

    override fun getPredict(): PredictApi {
        return predict
    }

    override fun getLoggingPredict(): PredictApi {
        return loggingPredict
    }

    override fun getConfig(): ConfigApi {
        return config
    }

    override fun getPushTokenProvider(): PushTokenProvider {
        return pushTokenProvider
    }

    override fun getDeepLinkServiceProvider(): ServiceEndpointProvider {
        return deepLinkServiceProvider
    }

    override fun getMobileEngageV2ServiceProvider(): ServiceEndpointProvider {
        return mobileEngageV2ServiceProvider
    }

    override fun getPredictServiceProvider(): ServiceEndpointProvider {
        return predictServiceProvider
    }

    override fun getClientServiceProvider(): ServiceEndpointProvider {
        return clientServiceProvider
    }

    override fun getInboxServiceProvider(): ServiceEndpointProvider {
        return inboxServiceProvider
    }

    override fun getConfigInternal(): ConfigInternal {
        return configInternal;
    }

    override fun getEventServiceProvider(): ServiceEndpointProvider {
        return eventServiceProvider
    }

    override fun getClientServiceStorage(): Storage<String> {
        return clientServiceStorage
    }

    override fun getEventServiceStorage(): Storage<String> {
        return eventServiceStorage
    }

    override fun getPredictServiceStorage(): Storage<String> {
        return predictServiceStorage
    }

    override fun getDeepLinkServiceStorage(): Storage<String> {
        return deepLinkServiceStorage
    }

    override fun getInboxServiceStorage(): Storage<String> {
        return inboxServiceStorage
    }

    override fun getMobileEngageV2ServiceStorage(): Storage<String> {
        return mobileEngageV2ServiceStorage
    }
}
