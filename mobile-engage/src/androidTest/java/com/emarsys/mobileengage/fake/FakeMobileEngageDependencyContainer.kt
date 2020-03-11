package com.emarsys.mobileengage.fake

import android.os.Handler
import com.emarsys.core.DefaultCoreCompletionHandler
import com.emarsys.core.RunnerProxy
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.activity.CurrentActivityWatchdog
import com.emarsys.core.database.CoreSQLiteDatabase
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.provider.activity.CurrentActivityProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.RestClient
import com.emarsys.core.response.ResponseHandlersProcessor
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.storage.Storage
import com.emarsys.core.util.FileDownloader
import com.emarsys.core.util.log.Logger
import com.emarsys.mobileengage.MobileEngageInternal
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.RefreshTokenInternal
import com.emarsys.mobileengage.client.ClientServiceInternal
import com.emarsys.mobileengage.deeplink.DeepLinkInternal
import com.emarsys.mobileengage.di.MobileEngageDependencyContainer
import com.emarsys.mobileengage.event.EventHandlerProvider
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.geofence.GeofenceInternal
import com.emarsys.mobileengage.iam.InAppInternal
import com.emarsys.mobileengage.iam.InAppPresenter
import com.emarsys.mobileengage.inbox.InboxInternal
import com.emarsys.mobileengage.inbox.model.NotificationCache
import com.emarsys.mobileengage.notification.ActionCommandFactory
import com.emarsys.mobileengage.push.PushInternal
import com.emarsys.mobileengage.push.PushTokenProvider
import com.nhaarman.mockitokotlin2.mock

class FakeMobileEngageDependencyContainer(
        private val coreSdkHandler: Handler = mock(),
        private val activityLifecycleWatchdog: ActivityLifecycleWatchdog = mock(),
        private val currentActivityWatchdog: CurrentActivityWatchdog = mock(),
        private val coreSQLiteDatabase: CoreSQLiteDatabase = mock(),
        private val deviceInfo: DeviceInfo = mock(),
        private val shardRepository: Repository<ShardModel, SqlSpecification> = mock(),
        private val timestampProvider: TimestampProvider = mock(),
        private val uuidProvider: UUIDProvider = mock(),
        private val logShardTrigger: Runnable = mock(),
        private val mobileEngageInternal: MobileEngageInternal = mock(),
        private val loggingMobileEngageInternal: MobileEngageInternal = mock(),
        private val pushInternal: PushInternal = mock(),
        private val loggingPushInternal: PushInternal = mock(),
        private val inboxInternal: InboxInternal = mock(),
        private val loggingInboxInternal: InboxInternal = mock(),
        private val inAppInternal: InAppInternal = mock(),
        private val loggingInAppInternal: InAppInternal = mock(),
        private val deepLinkInternal: DeepLinkInternal = mock(),
        private val loggingDeepLinkInternal: DeepLinkInternal = mock(),
        private val eventServiceInternal: EventServiceInternal = mock(),
        private val loggingEventServiceInternal: EventServiceInternal = mock(),
        private val clientServiceInternal: ClientServiceInternal = mock(),
        private val loggingClientServiceInternal: ClientServiceInternal = mock(),
        private val refreshTokenInternal: RefreshTokenInternal = mock(),
        private val completionHandler: DefaultCoreCompletionHandler = mock(),
        private val requestContext: MobileEngageRequestContext = mock(),
        private val inAppPresenter: InAppPresenter = mock(),
        private val predictShardTrigger: Runnable = mock(),
        private val runnerProxy: RunnerProxy = RunnerProxy(),
        private val logger: Logger = mock(),
        private val deviceInfoHashStorage: Storage<Int> = mock(),
        private val contactFieldValueStorage: Storage<String> = mock(),
        private val contactTokenStorage: Storage<String> = mock(),
        private val clientStateStorage: Storage<String> = mock(),
        private val responseHandlersProcessor: ResponseHandlersProcessor = mock(),
        private val notificationCache: NotificationCache = mock(),
        private val restClient: RestClient = mock(),
        private val pushTokenProvider: PushTokenProvider = mock(),
        private val clientServiceProvider: ServiceEndpointProvider = mock(),
        private val eventServiceProvider: ServiceEndpointProvider = mock(),
        private val deepLinkServiceProvider: ServiceEndpointProvider = mock(),
        private val mobileEngageV2ServiceProvider: ServiceEndpointProvider = mock(),
        private val inboxServiceProvider: ServiceEndpointProvider = mock(),
        private val clientServiceStorage: Storage<String> = mock(),
        private val eventServiceStorage: Storage<String> = mock(),
        private val deepLinkServiceStorage: Storage<String> = mock(),
        private val mobileEngageV2ServiceStorage: Storage<String> = mock(),
        private val inboxServiceStorage: Storage<String> = mock(),
        private val predictServiceStorage: Storage<String> = mock(),
        private val fileDownloader: FileDownloader = mock(),
        private val actionCommandFactory: ActionCommandFactory = mock(),
        private val silentMessageActionCommandFactory: ActionCommandFactory = mock(),
        private val notificationEventHandlerProvider: EventHandlerProvider = mock(),
        private val silentMessageEventHandlerProvider: EventHandlerProvider = mock(),
        private val currentActivityProvider: CurrentActivityProvider = mock(),
        private val geofenceInternal: GeofenceInternal = mock()
) : MobileEngageDependencyContainer {

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

    override fun getCoreSdkHandler(): Handler {
        return coreSdkHandler
    }

    override fun getFileDownloader(): FileDownloader {
        return fileDownloader
    }

    override fun getActivityLifecycleWatchdog(): ActivityLifecycleWatchdog {
        return activityLifecycleWatchdog
    }

    override fun getCurrentActivityWatchdog(): CurrentActivityWatchdog {
        return currentActivityWatchdog
    }

    override fun getCurrentActivityProvider(): CurrentActivityProvider {
        return currentActivityProvider
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

    override fun getPushInternal(): PushInternal {
        return pushInternal
    }

    override fun getEventServiceInternal(): EventServiceInternal {
        return eventServiceInternal
    }

    override fun getDeepLinkInternal(): DeepLinkInternal {
        return deepLinkInternal
    }

    override fun getGeofenceInternal(): GeofenceInternal {
        return geofenceInternal
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

    override fun getPushTokenProvider(): PushTokenProvider {
        return pushTokenProvider
    }

    override fun getDeepLinkServiceProvider(): ServiceEndpointProvider {
        return deepLinkServiceProvider
    }

    override fun getMobileEngageV2ServiceProvider(): ServiceEndpointProvider {
        return mobileEngageV2ServiceProvider
    }

    override fun getClientServiceProvider(): ServiceEndpointProvider {
        return clientServiceProvider
    }

    override fun getInboxServiceProvider(): ServiceEndpointProvider {
        return inboxServiceProvider
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

    override fun getDeepLinkServiceStorage(): Storage<String> {
        return deepLinkServiceStorage
    }

    override fun getInboxServiceStorage(): Storage<String> {
        return inboxServiceStorage
    }

    override fun getMobileEngageV2ServiceStorage(): Storage<String> {
        return mobileEngageV2ServiceStorage
    }

    override fun getNotificationActionCommandFactory(): ActionCommandFactory {
        return actionCommandFactory
    }

    override fun getSilentMessageActionCommandFactory(): ActionCommandFactory {
        return silentMessageActionCommandFactory
    }

    override fun getNotificationEventHandlerProvider(): EventHandlerProvider {
        return notificationEventHandlerProvider
    }

    override fun getSilentMessageEventHandlerProvider(): EventHandlerProvider {
        return silentMessageEventHandlerProvider
    }
}