package com.emarsys.di

import android.os.Handler
import com.emarsys.config.ConfigApi
import com.emarsys.config.ConfigInternal
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.DefaultCoreCompletionHandler
import com.emarsys.core.RunnerProxy
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.activity.CurrentActivityWatchdog
import com.emarsys.core.database.CoreSQLiteDatabase
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.di.Container
import com.emarsys.core.di.Container.addDependency
import com.emarsys.core.di.Container.getDependency
import com.emarsys.core.endpoint.ServiceEndpointProvider
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
import com.emarsys.geofence.GeofenceApi
import com.emarsys.inapp.InAppApi
import com.emarsys.inbox.InboxApi
import com.emarsys.inbox.MessageInboxApi
import com.emarsys.mobileengage.MobileEngageInternal
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.RefreshTokenInternal
import com.emarsys.mobileengage.client.ClientServiceInternal
import com.emarsys.mobileengage.deeplink.DeepLinkInternal
import com.emarsys.mobileengage.endpoint.Endpoint
import com.emarsys.mobileengage.event.DefaultEventServiceInternal
import com.emarsys.mobileengage.event.EventHandlerProvider
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.geofence.GeofenceInternal
import com.emarsys.mobileengage.iam.InAppInternal
import com.emarsys.mobileengage.iam.InAppPresenter
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam
import com.emarsys.mobileengage.inbox.InboxInternal
import com.emarsys.mobileengage.inbox.MessageInboxInternal
import com.emarsys.mobileengage.inbox.model.NotificationCache
import com.emarsys.mobileengage.notification.ActionCommandFactory
import com.emarsys.mobileengage.push.PushInternal
import com.emarsys.mobileengage.push.PushTokenProvider
import com.emarsys.mobileengage.responsehandler.MobileEngageTokenResponseHandler
import com.emarsys.mobileengage.storage.MobileEngageStorageKey
import com.emarsys.predict.PredictApi
import com.emarsys.predict.PredictInternal
import com.emarsys.predict.storage.PredictStorageKey
import com.emarsys.push.PushApi
import com.nhaarman.mockitokotlin2.mock

class FakeDependencyContainer(
        coreSdkHandler: Handler = mock(),
        activityLifecycleWatchdog: ActivityLifecycleWatchdog = mock(),
        currentActivityWatchdog: CurrentActivityWatchdog = mock(),
        coreSQLiteDatabase: CoreSQLiteDatabase = mock(),
        deviceInfo: DeviceInfo = mock(),
        shardRepository: Repository<ShardModel, SqlSpecification> = mock(),
        timestampProvider: TimestampProvider = mock(),
        uuidProvider: UUIDProvider = mock(),
        logShardTrigger: Runnable = mock(),
        mobileEngageInternal: MobileEngageInternal = mock(),
        loggingMobileEngageInternal: MobileEngageInternal = mock(),
        pushInternal: PushInternal = mock(),
        loggingPushInternal: PushInternal = mock(),
        inboxInternal: InboxInternal = mock(),
        loggingInboxInternal: InboxInternal = mock(),
        messageInboxInternal: MessageInboxInternal = mock(),
        loggingMessageInboxInternal: MessageInboxInternal = mock(),
        inAppInternal: InAppInternal = mock(),
        loggingInAppInternal: InAppInternal = mock(),
        deepLinkInternal: DeepLinkInternal = mock(),
        loggingDeepLinkInternal: DeepLinkInternal = mock(),
        eventServiceInternal: EventServiceInternal = mock(),
        loggingEventServiceInternal: EventServiceInternal = mock(),
        clientServiceInternal: ClientServiceInternal = mock(),
        loggingClientServiceInternal: ClientServiceInternal = mock(),
        predictInternal: PredictInternal = mock(),
        loggingPredictInternal: PredictInternal = mock(),
        refreshTokenInternal: RefreshTokenInternal = mock(),
        completionHandler: DefaultCoreCompletionHandler = mock(),
        requestContext: MobileEngageRequestContext = mock(),
        inAppPresenter: InAppPresenter = mock(),
        predictShardTrigger: Runnable = mock(),
        runnerProxy: RunnerProxy = RunnerProxy(),
        logger: Logger = mock(),
        deviceInfoPayloadStorage: StringStorage = mock(),
        contactFieldValueStorage: StringStorage = mock(),
        contactTokenStorage: StringStorage = mock(),
        refreshContactTokenStorage: StringStorage = mock(),
        clientStateStorage: StringStorage = mock(),
        pushTokenStorage: StringStorage = mock(),
        responseHandlersProcessor: ResponseHandlersProcessor = mock(),
        notificationCache: NotificationCache = mock(),
        restClient: RestClient = mock(),
        inbox: InboxApi = mock(),
        loggingInbox: InboxApi = mock(),
        messageInbox: MessageInboxApi = mock(),
        loggingMessageInbox: MessageInboxApi = mock(),
        inApp: InAppApi = mock(),
        loggingInApp: InAppApi = mock(),
        push: PushApi = mock(),
        loggingPush: PushApi = mock(),
        predict: PredictApi = mock(),
        loggingPredict: PredictApi = mock(),
        config: ConfigApi = mock(),
        geofence: GeofenceApi = mock(),
        loggingGeofence: GeofenceApi = mock(),
        pushTokenProvider: PushTokenProvider = mock(),
        clientServiceProvider: ServiceEndpointProvider = mock(),
        eventServiceProvider: ServiceEndpointProvider = mock(),
        deepLinkServiceProvider: ServiceEndpointProvider = mock(),
        mobileEngageV2ServiceProvider: ServiceEndpointProvider = mock(),
        inboxServiceProvider: ServiceEndpointProvider = mock(),
        messageInboxServiceProvider: ServiceEndpointProvider = mock(),
        predictServiceProvider: ServiceEndpointProvider = mock(),
        configInternal: ConfigInternal = mock(),
        clientServiceStorage: StringStorage = mock(),
        eventServiceStorage: StringStorage = mock(),
        deepLinkServiceStorage: StringStorage = mock(),
        mobileEngageV2ServiceStorage: StringStorage = mock(),
        inboxServiceStorage: StringStorage = mock(),
        messageInboxServiceStorage: StringStorage = mock(),
        predictServiceStorage: StringStorage = mock(),
        logLevelStorage: StringStorage = mock(),
        fileDownloader: FileDownloader = mock(),
        actionCommandFactory: ActionCommandFactory = mock(),
        silentMessageActionCommandFactory: ActionCommandFactory = mock(),
        notificationEventHandlerProvider: EventHandlerProvider = mock(),
        silentMessageEventHandlerProvider: EventHandlerProvider = mock(),
        geofenceEventHandlerProvider: EventHandlerProvider = mock(),
        currentActivityProvider: CurrentActivityProvider = mock(),
        geofenceInternal: GeofenceInternal = mock(),
        loggingGeofenceInternal: GeofenceInternal = mock(),
        buttonClickedRepository: Repository<ButtonClicked, SqlSpecification> = mock(),
        displayedIamRepository: Repository<DisplayedIam, SqlSpecification> = mock(),
        keyValueStore: KeyValueStore = mock(),
        contactTokenResponseHandler: MobileEngageTokenResponseHandler = mock()
) : EmarsysDependencyContainer {

    init {
        addDependency(coreSdkHandler, "coreSdkHandler")
        addDependency(activityLifecycleWatchdog)
        addDependency(currentActivityWatchdog)
        addDependency(coreSQLiteDatabase)
        addDependency(deviceInfo)
        addDependency(shardRepository, "shardRepository")
        addDependency(timestampProvider)
        addDependency(uuidProvider)
        addDependency(completionHandler)
        addDependency(runnerProxy)
        addDependency(logger)
        addDependency(responseHandlersProcessor)
        addDependency(restClient)
        addDependency(logLevelStorage, CoreStorageKey.LOG_LEVEL.key)
        addDependency(fileDownloader)
        addDependency(currentActivityProvider)

        addDependency(logShardTrigger, "logShardTrigger")
        addDependency(mobileEngageInternal, "defaultInstance")
        addDependency(loggingMobileEngageInternal, "loggingInstance")
        addDependency(pushInternal, "defaultInstance")
        addDependency(loggingPushInternal, "loggingInstance")
        addDependency(inboxInternal, "defaultInstance")
        addDependency(loggingInboxInternal, "loggingInstance")
        addDependency(messageInboxInternal, "defaultInstance")
        addDependency(loggingMessageInboxInternal, "loggingInstance")
        addDependency(inAppInternal, "defaultInstance")
        addDependency(loggingInAppInternal, "loggingInstance")
        addDependency(deepLinkInternal, "defaultInstance")
        addDependency(loggingDeepLinkInternal, "loggingInstance")
        addDependency(eventServiceInternal, "defaultInstance")
        addDependency(loggingEventServiceInternal, "loggingInstance")
        addDependency(clientServiceInternal, "defaultInstance")
        addDependency(loggingClientServiceInternal, "loggingInstance")
        addDependency(predictInternal, "defaultInstance")
        addDependency(loggingPredictInternal, "loggingInstance")
        addDependency(refreshTokenInternal)
        addDependency(requestContext)
        addDependency(inAppPresenter)
        addDependency(predictShardTrigger, "predictShardTrigger")
        addDependency(deviceInfoPayloadStorage, MobileEngageStorageKey.DEVICE_INFO_HASH.key)
        addDependency(contactFieldValueStorage, MobileEngageStorageKey.CONTACT_FIELD_VALUE.key)
        addDependency(contactTokenStorage, MobileEngageStorageKey.CONTACT_TOKEN.key)
        addDependency(clientStateStorage, MobileEngageStorageKey.CLIENT_STATE.key)
        addDependency(pushTokenStorage, MobileEngageStorageKey.PUSH_TOKEN.key)
        addDependency(refreshContactTokenStorage, MobileEngageStorageKey.REFRESH_TOKEN.key)
        addDependency(notificationCache)
        addDependency(inbox, "defaultInstance")
        addDependency(loggingInbox, "loggingInstance")
        addDependency(messageInbox, "defaultInstance")
        addDependency(loggingMessageInbox, "loggingInstance")
        addDependency(inApp, "defaultInstance")
        addDependency(loggingInApp, "loggingInstance")
        addDependency(push, "defaultInstance")
        addDependency(loggingPush, "loggingInstance")
        addDependency(predict, "defaultInstance")
        addDependency(loggingPredict, "loggingInstance")
        addDependency(config)
        addDependency(geofence, "defaultInstance")
        addDependency(loggingGeofence, "loggingInstance")
        addDependency(pushTokenProvider)
        addDependency(clientServiceProvider, Endpoint.ME_V3_CLIENT_HOST)
        addDependency(eventServiceProvider, Endpoint.ME_V3_EVENT_HOST)
        addDependency(deepLinkServiceProvider, Endpoint.DEEP_LINK)
        addDependency(mobileEngageV2ServiceProvider, Endpoint.ME_BASE_V2)
        addDependency(inboxServiceProvider, Endpoint.INBOX_BASE)
        addDependency(messageInboxServiceProvider, Endpoint.ME_V3_INBOX_HOST)
        addDependency(predictServiceProvider, com.emarsys.predict.endpoint.Endpoint.PREDICT_BASE_URL)
        addDependency(configInternal)
        addDependency(clientServiceStorage, MobileEngageStorageKey.CLIENT_SERVICE_URL.key)
        addDependency(eventServiceStorage, MobileEngageStorageKey.EVENT_SERVICE_URL.key)
        addDependency(deepLinkServiceStorage, MobileEngageStorageKey.DEEPLINK_SERVICE_URL.key)
        addDependency(mobileEngageV2ServiceStorage, MobileEngageStorageKey.ME_V2_SERVICE_URL.key)
        addDependency(inboxServiceStorage, MobileEngageStorageKey.INBOX_SERVICE_URL.key)
        addDependency(messageInboxServiceStorage, MobileEngageStorageKey.MESSAGE_INBOX_SERVICE_URL.key)
        addDependency(predictServiceStorage, PredictStorageKey.PREDICT_SERVICE_URL.key)
        addDependency(actionCommandFactory, "notificationActionCommandFactory")
        addDependency(silentMessageActionCommandFactory, "silentMessageActionCommandFactory")
        addDependency(notificationEventHandlerProvider, "notificationEventHandlerProvider")
        addDependency(silentMessageEventHandlerProvider, "silentMessageEventHandlerProvider")
        addDependency(geofenceEventHandlerProvider, "geofenceEventHandlerProvider")
        addDependency(geofenceInternal, "defaultInstance")
        addDependency(loggingGeofenceInternal, "loggingInstance")
        addDependency(buttonClickedRepository, "buttonClickedRepository")
        addDependency(displayedIamRepository, "displayedIamRepository")
        addDependency(keyValueStore)
        addDependency(contactTokenResponseHandler, "contactTokenResponseHandler")

    }

    override fun getCoreSdkHandler(): Handler = Container.getDependency("coreSdkHandler")

    override fun getActivityLifecycleWatchdog(): ActivityLifecycleWatchdog = Container.getDependency()

    override fun getCurrentActivityWatchdog(): CurrentActivityWatchdog = Container.getDependency()

    override fun getCoreSQLiteDatabase(): CoreSQLiteDatabase = Container.getDependency()

    override fun getDeviceInfo(): DeviceInfo = Container.getDependency()

    override fun getTimestampProvider(): TimestampProvider = Container.getDependency()

    override fun getUuidProvider(): UUIDProvider = Container.getDependency()

    override fun getLogShardTrigger(): Runnable = Container.getDependency("logShardTrigger")

    override fun getRunnerProxy(): RunnerProxy = Container.getDependency()

    override fun getLogger(): Logger = Container.getDependency()

    override fun getRestClient(): RestClient = Container.getDependency()

    override fun getFileDownloader(): FileDownloader = Container.getDependency()

    override fun getShardRepository(): Repository<ShardModel, SqlSpecification> = Container.getDependency<ShardModelRepository>()


    override fun getInbox(): InboxApi = getDependency("defaultInstance")

    override fun getLoggingInbox(): InboxApi = getDependency("loggingInstance")

    override fun getMessageInbox(): MessageInboxApi = getDependency("defaultInstance")

    override fun getLoggingMessageInbox(): MessageInboxApi = getDependency("loggingInstance")

    override fun getInApp(): InAppApi = getDependency("defaultInstance")

    override fun getLoggingInApp(): InAppApi = getDependency("loggingInstance")

    override fun getPush(): PushApi = getDependency("defaultInstance")

    override fun getLoggingPush(): PushApi = getDependency("loggingInstance")

    override fun getPredict(): PredictApi = getDependency("defaultInstance")

    override fun getLoggingPredict(): PredictApi = getDependency("loggingInstance")

    override fun getConfig(): ConfigApi = getDependency()

    override fun getGeofence(): GeofenceApi = getDependency("defaultInstance")

    override fun getLoggingGeofence(): GeofenceApi = getDependency("loggingInstance")

    override fun getConfigInternal(): ConfigInternal = getDependency()
    override fun getMobileEngageInternal(): MobileEngageInternal = getDependency("defaultInstance")

    override fun getLoggingMobileEngageInternal(): MobileEngageInternal = getDependency("loggingInstance")

    override fun getClientServiceInternal(): ClientServiceInternal = getDependency("defaultInstance")

    override fun getLoggingClientServiceInternal(): ClientServiceInternal = getDependency("loggingInstance")

    override fun getInboxInternal(): InboxInternal = getDependency("defaultInstance")

    override fun getLoggingInboxInternal(): InboxInternal = getDependency("loggingInstance")

    override fun getMessageInboxInternal(): MessageInboxInternal = getDependency("defaultInstance")

    override fun getLoggingMessageInboxInternal(): MessageInboxInternal = getDependency("loggingInstance")

    override fun getInAppInternal(): InAppInternal = getDependency("defaultInstance")

    override fun getLoggingInAppInternal(): InAppInternal = getDependency("loggingInstance")

    override fun getDeepLinkInternal(): DeepLinkInternal = getDependency("defaultInstance")

    override fun getLoggingDeepLinkInternal(): DeepLinkInternal = getDependency("loggingInstance")

    override fun getPushInternal(): PushInternal = getDependency("defaultInstance")

    override fun getLoggingPushInternal(): PushInternal = getDependency("loggingInstance")

    override fun getEventServiceInternal(): EventServiceInternal = getDependency<DefaultEventServiceInternal>("defaultInstance")

    override fun getLoggingEventServiceInternal(): EventServiceInternal = getDependency("loggingInstance")

    override fun getRefreshTokenInternal(): RefreshTokenInternal = getDependency()
    override fun getCoreCompletionHandler(): CoreCompletionHandler = getDependency()

    override fun getRequestContext(): MobileEngageRequestContext = getDependency()

    override fun getInAppPresenter(): InAppPresenter = getDependency()

    override fun getDeviceInfoPayloadStorage(): StringStorage = getDependency(MobileEngageStorageKey.DEVICE_INFO_HASH.key)

    override fun getContactFieldValueStorage(): StringStorage = getDependency(MobileEngageStorageKey.CONTACT_FIELD_VALUE.key)

    override fun getContactTokenStorage(): StringStorage = getDependency(MobileEngageStorageKey.CONTACT_TOKEN.key)

    override fun getClientStateStorage(): StringStorage = getDependency(MobileEngageStorageKey.CLIENT_STATE.key)

    override fun getPushTokenStorage(): StringStorage = getDependency(MobileEngageStorageKey.PUSH_TOKEN.key)

    override fun getRefreshContactTokenStorage(): StringStorage = getDependency(MobileEngageStorageKey.REFRESH_TOKEN.key)

    override fun getLogLevelStorage(): StringStorage = getDependency(CoreStorageKey.LOG_LEVEL.key)

    override fun getResponseHandlersProcessor(): ResponseHandlersProcessor = getDependency()

    override fun getNotificationCache(): NotificationCache = getDependency()

    override fun getPushTokenProvider(): PushTokenProvider = getDependency()

    override fun getClientServiceProvider(): ServiceEndpointProvider = getDependency(Endpoint.ME_V3_CLIENT_HOST)

    override fun getEventServiceProvider(): ServiceEndpointProvider = getDependency(Endpoint.ME_V3_EVENT_HOST)

    override fun getDeepLinkServiceProvider(): ServiceEndpointProvider = getDependency(Endpoint.DEEP_LINK)

    override fun getInboxServiceProvider(): ServiceEndpointProvider = getDependency(Endpoint.INBOX_BASE)

    override fun getMessageInboxServiceProvider(): ServiceEndpointProvider = getDependency(Endpoint.ME_V3_INBOX_HOST)

    override fun getMobileEngageV2ServiceProvider(): ServiceEndpointProvider = getDependency(Endpoint.ME_BASE_V2)

    override fun getClientServiceStorage(): StringStorage = getDependency(MobileEngageStorageKey.CLIENT_SERVICE_URL.key)

    override fun getEventServiceStorage(): StringStorage = getDependency(MobileEngageStorageKey.EVENT_SERVICE_URL.key)

    override fun getDeepLinkServiceStorage(): StringStorage = getDependency(MobileEngageStorageKey.DEEPLINK_SERVICE_URL.key)

    override fun getInboxServiceStorage(): StringStorage = getDependency(MobileEngageStorageKey.INBOX_SERVICE_URL.key)

    override fun getMessageInboxServiceStorage(): StringStorage = getDependency(MobileEngageStorageKey.MESSAGE_INBOX_SERVICE_URL.key)

    override fun getMobileEngageV2ServiceStorage(): StringStorage = getDependency(MobileEngageStorageKey.ME_V2_SERVICE_URL.key)

    override fun getNotificationActionCommandFactory(): ActionCommandFactory = getDependency("notificationActionCommandFactory")

    override fun getSilentMessageActionCommandFactory(): ActionCommandFactory = getDependency("silentMessageActionCommandFactory")

    override fun getNotificationEventHandlerProvider(): EventHandlerProvider = getDependency("notificationEventHandlerProvider")

    override fun getSilentMessageEventHandlerProvider(): EventHandlerProvider = getDependency("silentMessageEventHandlerProvider")

    override fun getGeofenceEventHandlerProvider(): EventHandlerProvider = getDependency("geofenceEventHandlerProvider")

    override fun getCurrentActivityProvider(): CurrentActivityProvider = getDependency()

    override fun getGeofenceInternal(): GeofenceInternal = getDependency("defaultInstance")

    override fun getLoggingGeofenceInternal(): GeofenceInternal = getDependency("loggingInstance")

    override fun getPredictInternal(): PredictInternal = getDependency("defaultInstance")

    override fun getLoggingPredictInternal(): PredictInternal = getDependency("loggingInstance")

    override fun getPredictShardTrigger(): Runnable = getDependency("predictShardTrigger")

    override fun getPredictServiceProvider(): ServiceEndpointProvider = getDependency(com.emarsys.predict.endpoint.Endpoint.PREDICT_BASE_URL)

    override fun getPredictServiceStorage(): StringStorage = getDependency(PredictStorageKey.PREDICT_SERVICE_URL.key)


    override fun getButtonClickedRepository(): Repository<ButtonClicked, SqlSpecification> = getDependency("buttonClickedRepository")
    override fun getDisplayedIamRepository(): Repository<DisplayedIam, SqlSpecification> = getDependency("displayedIamRepository")
    override fun getKeyValueStore(): KeyValueStore = getDependency()
    override fun getContactTokenResponseHandler(): MobileEngageTokenResponseHandler = getDependency("contactTokenResponseHandler")


}
