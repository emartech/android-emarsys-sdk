package com.emarsys.di

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.util.Base64
import androidx.core.app.NotificationManagerCompat
import com.emarsys.Emarsys
import com.emarsys.EmarsysRequestModelFactory
import com.emarsys.clientservice.ClientService
import com.emarsys.clientservice.ClientServiceApi
import com.emarsys.config.*
import com.emarsys.core.DefaultCoreCompletionHandler
import com.emarsys.core.Mapper
import com.emarsys.core.activity.ActivityLifecycleAction
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.activity.CurrentActivityWatchdog
import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.api.proxyApi
import com.emarsys.core.app.AppLifecycleObserver
import com.emarsys.core.concurrency.CoreSdkHandlerProvider
import com.emarsys.core.connection.ConnectionProvider
import com.emarsys.core.connection.ConnectionWatchDog
import com.emarsys.core.contentresolver.hardwareid.HardwareIdContentResolver
import com.emarsys.core.crypto.Crypto
import com.emarsys.core.crypto.HardwareIdentificationCrypto
import com.emarsys.core.database.CoreSQLiteDatabase
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.HardwareRepository
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.core.notification.NotificationManagerHelper
import com.emarsys.core.notification.NotificationManagerProxy
import com.emarsys.core.permission.PermissionChecker
import com.emarsys.core.provider.activity.CurrentActivityProvider
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.random.RandomProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.RestClient
import com.emarsys.core.request.factory.CoreCompletionHandlerMiddlewareProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.request.model.RequestModelRepository
import com.emarsys.core.resource.MetaDataReader
import com.emarsys.core.response.AbstractResponseHandler
import com.emarsys.core.response.ResponseHandlersProcessor
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.shard.ShardModelRepository
import com.emarsys.core.shard.specification.FilterByShardType
import com.emarsys.core.storage.*
import com.emarsys.core.util.FileDownloader
import com.emarsys.core.util.batch.BatchingShardTrigger
import com.emarsys.core.util.batch.ListChunker
import com.emarsys.core.util.log.LogShardListMerger
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.predicate.ListSizeAtLeast
import com.emarsys.core.worker.DefaultWorker
import com.emarsys.core.worker.Worker
import com.emarsys.deeplink.DeepLink
import com.emarsys.deeplink.DeepLinkApi
import com.emarsys.eventservice.EventService
import com.emarsys.eventservice.EventServiceApi
import com.emarsys.geofence.Geofence
import com.emarsys.geofence.GeofenceApi
import com.emarsys.inapp.InApp
import com.emarsys.inapp.InAppApi
import com.emarsys.inbox.Inbox
import com.emarsys.inbox.InboxApi
import com.emarsys.inbox.MessageInbox
import com.emarsys.inbox.MessageInboxApi
import com.emarsys.mobileengage.*
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.client.ClientServiceInternal
import com.emarsys.mobileengage.client.DefaultClientServiceInternal
import com.emarsys.mobileengage.client.LoggingClientServiceInternal
import com.emarsys.mobileengage.deeplink.DeepLinkAction
import com.emarsys.mobileengage.deeplink.DeepLinkInternal
import com.emarsys.mobileengage.deeplink.DefaultDeepLinkInternal
import com.emarsys.mobileengage.deeplink.LoggingDeepLinkInternal
import com.emarsys.mobileengage.device.DeviceInfoStartAction
import com.emarsys.mobileengage.endpoint.Endpoint
import com.emarsys.mobileengage.event.DefaultEventServiceInternal
import com.emarsys.mobileengage.event.EventHandlerProvider
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.event.LoggingEventServiceInternal
import com.emarsys.mobileengage.geofence.*
import com.emarsys.mobileengage.iam.*
import com.emarsys.mobileengage.iam.dialog.IamDialogProvider
import com.emarsys.mobileengage.iam.inline.InlineInAppWebViewFactory
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridgeFactory
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClickedRepository
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIamRepository
import com.emarsys.mobileengage.iam.model.requestRepositoryProxy.RequestRepositoryProxy
import com.emarsys.mobileengage.iam.webview.IamStaticWebViewProvider
import com.emarsys.mobileengage.iam.webview.WebViewProvider
import com.emarsys.mobileengage.inbox.*
import com.emarsys.mobileengage.inbox.model.NotificationCache
import com.emarsys.mobileengage.notification.ActionCommandFactory
import com.emarsys.mobileengage.push.*
import com.emarsys.mobileengage.request.CoreCompletionHandlerRefreshTokenProxyProvider
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.mobileengage.request.mapper.ContactTokenHeaderMapper
import com.emarsys.mobileengage.request.mapper.DeviceEventStateRequestMapper
import com.emarsys.mobileengage.request.mapper.MobileEngageHeaderMapper
import com.emarsys.mobileengage.request.mapper.OpenIdTokenRequestMapper
import com.emarsys.mobileengage.responsehandler.*
import com.emarsys.mobileengage.service.RemoteMessageMapper
import com.emarsys.mobileengage.session.MobileEngageSession
import com.emarsys.mobileengage.session.SessionIdHolder
import com.emarsys.mobileengage.storage.MobileEngageStorageKey
import com.emarsys.mobileengage.util.RequestHeaderUtils
import com.emarsys.mobileengage.util.RequestModelHelper
import com.emarsys.oneventaction.OnEventAction
import com.emarsys.oneventaction.OnEventActionApi
import com.emarsys.predict.*
import com.emarsys.predict.provider.PredictRequestModelBuilderProvider
import com.emarsys.predict.request.PredictHeaderFactory
import com.emarsys.predict.request.PredictRequestContext
import com.emarsys.predict.response.VisitorIdResponseHandler
import com.emarsys.predict.response.XPResponseHandler
import com.emarsys.predict.shard.PredictShardListMerger
import com.emarsys.predict.storage.PredictStorageKey
import com.emarsys.push.Push
import com.emarsys.push.PushApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import org.json.JSONObject
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec


open class DefaultEmarsysComponent(config: EmarsysConfig) : EmarsysComponent {
    companion object {
        private const val EMARSYS_SHARED_PREFERENCES_NAME = "emarsys_shared_preferences"
        private const val EMARSYS_SECURE_SHARED_PREFERENCES_NAME = "emarsys_secure_shared_preferences"
        private const val GEOFENCE_LIMIT = 99
        private const val PUBLIC_KEY = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAELjWEUIBX9zlm1OI4gF1hMCBLzpaBwgs9HlmSIBAqP4MDGy4ibOOV3FVDrnAY0Q34LZTbPBlp3gRNZJ19UoSy2Q=="
    }

    override val coreSdkHandler: CoreSdkHandler = CoreSdkHandlerProvider().provideHandler()

    override val uiHandler: Handler = Handler(config.application.mainLooper)

    override val inbox: InboxApi = (Inbox() as InboxApi).proxyApi(coreSdkHandler)

    override val loggingInbox: InboxApi = (Inbox(true) as InboxApi).proxyApi(coreSdkHandler)

    override val deepLink: DeepLinkApi = (DeepLink() as DeepLinkApi).proxyApi(coreSdkHandler)

    override val loggingDeepLink: DeepLinkApi = (DeepLink(true) as DeepLinkApi).proxyApi(coreSdkHandler)

    override val messageInbox: MessageInboxApi = (MessageInbox() as MessageInboxApi).proxyApi(coreSdkHandler)

    override val loggingMessageInbox: MessageInboxApi = (MessageInbox(true) as MessageInboxApi).proxyApi(coreSdkHandler)

    override val inApp: InAppApi = (InApp() as InAppApi).proxyApi(coreSdkHandler)

    override val loggingInApp: InAppApi = (InApp(true) as InAppApi).proxyApi(coreSdkHandler)

    override val onEventAction: OnEventActionApi = (OnEventAction() as OnEventActionApi).proxyApi(coreSdkHandler)

    override val loggingOnEventAction: OnEventActionApi = (OnEventAction() as OnEventActionApi).proxyApi(coreSdkHandler)

    override val push: PushApi = (Push() as PushApi).proxyApi(coreSdkHandler)

    override val loggingPush: PushApi = (Push(true) as PushApi).proxyApi(coreSdkHandler)

    override val predict: PredictApi = (Predict() as PredictApi).proxyApi(coreSdkHandler)

    override val loggingPredict: PredictApi = (Predict(true) as PredictApi).proxyApi(coreSdkHandler)

    override val config: ConfigApi = (Config() as ConfigApi).proxyApi(coreSdkHandler)

    override val geofence: GeofenceApi = (Geofence() as GeofenceApi).proxyApi(coreSdkHandler)

    override val loggingGeofence: GeofenceApi = (Geofence(true) as GeofenceApi).proxyApi(coreSdkHandler)

    override val mobileEngage: MobileEngageApi = (MobileEngage() as MobileEngageApi).proxyApi(coreSdkHandler)

    override val loggingMobileEngage: MobileEngageApi = (MobileEngage(true) as MobileEngageApi).proxyApi(coreSdkHandler)

    override val predictRestricted: PredictRestrictedApi = (PredictRestricted() as PredictRestrictedApi).proxyApi(coreSdkHandler)

    override val loggingPredictRestricted: PredictRestrictedApi = (PredictRestricted(true) as PredictRestrictedApi).proxyApi(coreSdkHandler)

    override val clientService: ClientServiceApi = (ClientService() as ClientServiceApi).proxyApi(coreSdkHandler)

    override val loggingClientService: ClientServiceApi = (ClientService(true) as ClientServiceApi).proxyApi(coreSdkHandler)

    override val eventService: EventServiceApi = (EventService() as EventServiceApi).proxyApi(coreSdkHandler)

    override val loggingEventService: EventServiceApi = (EventService(true) as EventServiceApi).proxyApi(coreSdkHandler)

    override val responseHandlersProcessor: ResponseHandlersProcessor by lazy {
        ResponseHandlersProcessor(mutableListOf())
    }

    override val overlayInAppPresenter: OverlayInAppPresenter by lazy {
        OverlayInAppPresenter(
                coreSdkHandler,
                uiHandler,
                IamStaticWebViewProvider(config.application),
                inAppInternal,
                IamDialogProvider(uiHandler),
                buttonClickedRepository,
                displayedIamRepository,
                timestampProvider,
                currentActivityProvider,
                iamJsBridgeFactory)
    }

    override val activityLifecycleWatchdog: ActivityLifecycleWatchdog by lazy {
        val applicationStartActions = arrayOf<ActivityLifecycleAction>(
                DeviceInfoStartAction(clientServiceInternal, deviceInfoPayloadStorage, deviceInfo)
        )
        val activityCreatedActions = arrayOf<ActivityLifecycleAction>(
                DeepLinkAction(deepLinkInternal)
        )
        val initializationActions = arrayOf<ActivityLifecycleAction?>(
                FetchGeofencesAction(geofenceInternal),
                FetchRemoteConfigAction(configInternal),
                InAppStartAction(eventServiceInternal, contactTokenStorage)
        )

        ActivityLifecycleWatchdog(
                applicationStartActions,
                activityCreatedActions,
                initializationActions,
                coreSdkHandler)
    }

    fun initializeResponseHandlers(config: EmarsysConfig) {
        val responseHandlers: MutableList<AbstractResponseHandler?> = ArrayList()
        responseHandlers.add(VisitorIdResponseHandler(keyValueStore, predictServiceProvider))
        responseHandlers.add(XPResponseHandler(keyValueStore, predictServiceProvider))
        responseHandlers.add(MobileEngageTokenResponseHandler("refreshToken", refreshTokenStorage, requestModelHelper))
        responseHandlers.add(contactTokenResponseHandler)
        responseHandlers.add(MobileEngageClientStateResponseHandler(clientStateStorage, requestModelHelper))
        responseHandlers.add(ClientInfoResponseHandler(deviceInfo, deviceInfoPayloadStorage))
        responseHandlers.add(InAppMessageResponseHandler(overlayInAppPresenter))
        responseHandlers.add(InAppCleanUpResponseHandler(
                displayedIamRepository,
                buttonClickedRepository,
                requestModelHelper
        ))
        responseHandlers.add(InAppCleanUpResponseHandlerV4(
                displayedIamRepository,
                buttonClickedRepository,
                requestModelHelper
        ))
        responseHandlers.add(OnEventActionResponseHandler(
                ActionCommandFactory(config.application, eventServiceInternal, onEventActionEventHandlerProvider, uiHandler),
                displayedIamRepository,
                eventServiceInternal,
                timestampProvider,
                coreSdkHandler))
        responseHandlers.add(DeviceEventStateResponseHandler(
                deviceEventStateStorage,
                requestModelHelper
        ))
        responseHandlersProcessor.addResponseHandlers(responseHandlers)
    }

    override val restClient: RestClient by lazy {
        RestClient(ConnectionProvider(),
                timestampProvider,
                responseHandlersProcessor,
                createRequestModelMappers(),
                uiHandler,
                coreSdkHandler)
    }

    override val sharedPreferences: SharedPreferences by lazy {
        val oldPrefs = config.application.getSharedPreferences(EMARSYS_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        SecureSharedPreferencesProvider(config.application, EMARSYS_SECURE_SHARED_PREFERENCES_NAME, oldPrefs).provide()
    }

    override val contactTokenStorage: Storage<String?> by lazy {
        StringStorage(MobileEngageStorageKey.CONTACT_TOKEN, sharedPreferences)
    }

    override val clientStateStorage: Storage<String?> by lazy {
        StringStorage(MobileEngageStorageKey.CLIENT_STATE, sharedPreferences)
    }

    override val pushTokenStorage: Storage<String?> by lazy {
        StringStorage(MobileEngageStorageKey.PUSH_TOKEN, sharedPreferences)
    }

    override val uuidProvider: UUIDProvider by lazy {
        UUIDProvider()
    }

    override val hardwareIdStorage: Storage<String?> by lazy {
        StringStorage(CoreStorageKey.HARDWARE_ID, sharedPreferences)
    }

    override val coreDbHelper: CoreDbHelper by lazy {
        CoreDbHelper(config.application, mutableMapOf())
    }

    override val crypto: Crypto by lazy {
        Crypto(createPublicKey())
    }

    override val hardwareIdProvider: HardwareIdProvider by lazy {
        val hardwareRepository = HardwareRepository(coreDbHelper)
        val hardwareIdentificationCrypto = HardwareIdentificationCrypto(config.sharedSecret, crypto)
        val hardwareIdContentResolver = HardwareIdContentResolver(config.application, hardwareIdentificationCrypto, config.sharedPackageNames)
        HardwareIdProvider(uuidProvider, hardwareRepository, hardwareIdStorage, hardwareIdContentResolver, hardwareIdentificationCrypto)
    }

    override val deviceInfo: DeviceInfo by lazy {
            val notificationManagerCompat = NotificationManagerCompat.from(config.application)
            val notificationManager = config.application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationManagerProxy = NotificationManagerProxy(notificationManager, notificationManagerCompat)
            val notificationSettings: NotificationSettings = NotificationManagerHelper(notificationManagerProxy)
            DeviceInfo(config.application, hardwareIdProvider, VersionProvider(), LanguageProvider(), notificationSettings, config.automaticPushTokenSendingEnabled)
    }

    override val timestampProvider: TimestampProvider by lazy {
        TimestampProvider()
    }

    override val refreshTokenStorage: Storage<String?> by lazy {
        StringStorage(MobileEngageStorageKey.REFRESH_TOKEN, sharedPreferences)
    }

    override val contactFieldValueStorage: Storage<String?> by lazy {
        StringStorage(MobileEngageStorageKey.CONTACT_FIELD_VALUE, sharedPreferences)
    }

    override val sessionIdHolder: SessionIdHolder by lazy {
        SessionIdHolder(null)
    }

    override val requestContext: MobileEngageRequestContext by lazy {
        MobileEngageRequestContext(
                config.mobileEngageApplicationCode,
                config.contactFieldId,
                null,
                deviceInfo,
                timestampProvider,
                uuidProvider,
                clientStateStorage,
                contactTokenStorage,
                refreshTokenStorage,
                contactFieldValueStorage,
                pushTokenStorage,
                sessionIdHolder)
    }

    override val inAppEventHandlerInternal: InAppEventHandlerInternal by lazy {
        InAppEventHandlerInternal()
    }

    override val shardRepository: Repository<ShardModel, SqlSpecification> by lazy {
        ShardModelRepository(coreDbHelper)
    }

    override val buttonClickedRepository: Repository<ButtonClicked, SqlSpecification> by lazy {
        ButtonClickedRepository(coreDbHelper)
    }

    override val displayedIamRepository: Repository<DisplayedIam, SqlSpecification> by lazy {
        DisplayedIamRepository(coreDbHelper)
    }

    override val requestModelRepository: Repository<RequestModel, SqlSpecification> by lazy {
        createRequestModelRepository(coreDbHelper, inAppEventHandlerInternal)
    }

    override val connectionWatchdog: ConnectionWatchDog by lazy {
        ConnectionWatchDog(config.application, coreSdkHandler)
    }

    override val coreCompletionHandler: DefaultCoreCompletionHandler by lazy {
        DefaultCoreCompletionHandler(mutableMapOf())
    }

    override val clientServiceStorage: Storage<String?> by lazy {
        StringStorage(MobileEngageStorageKey.CLIENT_SERVICE_URL, sharedPreferences)
    }

    override val eventServiceStorage: Storage<String?> by lazy {
        StringStorage(MobileEngageStorageKey.EVENT_SERVICE_URL, sharedPreferences)
    }

    override val deepLinkServiceStorage: Storage<String?> by lazy {
        StringStorage(MobileEngageStorageKey.DEEPLINK_SERVICE_URL, sharedPreferences)
    }

    override val inboxServiceStorage: Storage<String?> by lazy {
        StringStorage(MobileEngageStorageKey.INBOX_SERVICE_URL, sharedPreferences)
    }

    override val messageInboxServiceStorage: Storage<String?> by lazy {
        StringStorage(MobileEngageStorageKey.MESSAGE_INBOX_SERVICE_URL, sharedPreferences)
    }

    override val mobileEngageV2ServiceStorage: Storage<String?> by lazy {
        StringStorage(MobileEngageStorageKey.ME_V2_SERVICE_URL, sharedPreferences)
    }

    override val deviceEventStateStorage: Storage<String?> by lazy {
        StringStorage(MobileEngageStorageKey.DEVICE_EVENT_STATE, sharedPreferences)
    }

    override val clientServiceEndpointProvider: ServiceEndpointProvider by lazy {
        ServiceEndpointProvider(clientServiceStorage, Endpoint.ME_CLIENT_HOST)
    }

    override val eventServiceEndpointProvider: ServiceEndpointProvider by lazy {
        ServiceEndpointProvider(eventServiceStorage, Endpoint.ME_EVENT_HOST)
    }

    override val deepLinkServiceProvider: ServiceEndpointProvider by lazy {
        ServiceEndpointProvider(deepLinkServiceStorage, Endpoint.DEEP_LINK)
    }

    override val inboxServiceProvider: ServiceEndpointProvider by lazy {
        ServiceEndpointProvider(inboxServiceStorage, Endpoint.INBOX_BASE)
    }

    override val messageInboxServiceProvider: ServiceEndpointProvider by lazy {
        ServiceEndpointProvider(messageInboxServiceStorage, Endpoint.ME_V3_INBOX_HOST)
    }

    override val mobileEngageV2ServiceProvider: ServiceEndpointProvider by lazy {
        ServiceEndpointProvider(mobileEngageV2ServiceStorage, Endpoint.ME_BASE_V2)
    }

    override val requestModelHelper: RequestModelHelper by lazy {
        RequestModelHelper(clientServiceEndpointProvider, eventServiceEndpointProvider, messageInboxServiceProvider)
    }

    override val coreCompletionHandlerRefreshTokenProxyProvider: CoreCompletionHandlerRefreshTokenProxyProvider by lazy {
        val coreCompletionHandlerMiddlewareProvider = CoreCompletionHandlerMiddlewareProvider(
                requestModelRepository,
                uiHandler,
                coreSdkHandler
        )
        CoreCompletionHandlerRefreshTokenProxyProvider(
                coreCompletionHandlerMiddlewareProvider,
                refreshTokenInternal,
                restClient,
                contactTokenStorage,
                pushTokenStorage,
                coreCompletionHandler,
                requestModelHelper)
    }

    override val worker: Worker by lazy {
        DefaultWorker(
                requestModelRepository,
                connectionWatchdog,
                uiHandler,
                coreCompletionHandler,
                restClient,
                coreCompletionHandlerRefreshTokenProxyProvider)
    }

    override val requestManager: RequestManager by lazy {
        RequestManager(
                coreSdkHandler,
                requestModelRepository,
                shardRepository,
                worker,
                restClient,
                coreCompletionHandler,
                coreCompletionHandler,
                coreCompletionHandlerRefreshTokenProxyProvider
        ).apply {
            setDefaultHeaders(RequestHeaderUtils.createDefaultHeaders(requestContext))
        }
    }

    override val mobileEngageRequestModelFactory: MobileEngageRequestModelFactory by lazy {
        MobileEngageRequestModelFactory(
                requestContext,
                clientServiceEndpointProvider,
                eventServiceEndpointProvider,
                mobileEngageV2ServiceProvider,
                inboxServiceProvider,
                messageInboxServiceProvider,
                buttonClickedRepository)
    }

    override val loggingMobileEngageInternal: MobileEngageInternal by lazy {
        LoggingMobileEngageInternal(Emarsys::class.java)
    }

    override val eventServiceInternal: EventServiceInternal by lazy {
        DefaultEventServiceInternal(requestManager, mobileEngageRequestModelFactory)
    }

    override val loggingEventServiceInternal: EventServiceInternal by lazy {
        LoggingEventServiceInternal(Emarsys::class.java)
    }

    override val mobileEngageSession: MobileEngageSession by lazy {
        MobileEngageSession(timestampProvider, uuidProvider, eventServiceInternal, sessionIdHolder)
    }

    override val notificationEventHandlerProvider: EventHandlerProvider by lazy {
        var notificationEventHandler: EventHandler? = null
        if (config.notificationEventHandler != null) {
            notificationEventHandler = object : EventHandler {
                override fun handleEvent(context: Context, eventName: String, payload: JSONObject?) {
                    config.notificationEventHandler!!.handleEvent(context, eventName, payload)
                }
            }
        }
        EventHandlerProvider(notificationEventHandler)
    }

    override val silentMessageEventHandlerProvider: EventHandlerProvider by lazy {
        EventHandlerProvider(null)
    }

    override val notificationInformationListenerProvider: NotificationInformationListenerProvider by lazy {
        NotificationInformationListenerProvider(null)
    }

    override val silentNotificationInformationListenerProvider: SilentNotificationInformationListenerProvider by lazy {
        SilentNotificationInformationListenerProvider(null)
    }

    override val mobileEngageInternal: MobileEngageInternal by lazy {
        DefaultMobileEngageInternal(requestManager, mobileEngageRequestModelFactory, requestContext, mobileEngageSession, sessionIdHolder)
    }

    override val clientServiceInternal: ClientServiceInternal by lazy {
        DefaultClientServiceInternal(requestManager, mobileEngageRequestModelFactory)
    }

    override val loggingClientServiceInternal: ClientServiceInternal by lazy {
        LoggingClientServiceInternal(Emarsys::class.java)
    }

    override val inboxInternal: InboxInternal by lazy {
        DefaultInboxInternal(requestManager, requestContext, mobileEngageRequestModelFactory)
    }

    override val loggingInboxInternal: InboxInternal by lazy {
        LoggingInboxInternal(Emarsys::class.java)
    }

    override val messageInboxInternal: MessageInboxInternal by lazy {
        DefaultMessageInboxInternal(requestManager, mobileEngageRequestModelFactory, MessageInboxResponseMapper())
    }

    override val loggingMessageInboxInternal: MessageInboxInternal by lazy {
        LoggingMessageInboxInternal(Emarsys::class.java)
    }

    override val inAppInternal: InAppInternal by lazy {
        DefaultInAppInternal(inAppEventHandlerInternal, eventServiceInternal)
    }

    override val loggingInAppInternal: InAppInternal by lazy {
        LoggingInAppInternal(Emarsys::class.java)
    }
    override val deepLinkInternal: DeepLinkInternal by lazy {
        DefaultDeepLinkInternal(requestManager, requestContext, deepLinkServiceProvider)
    }

    override val loggingDeepLinkInternal: DeepLinkInternal by lazy {
        LoggingDeepLinkInternal(Emarsys::class.java)
    }

    override val pushInternal: PushInternal by lazy {
        DefaultPushInternal(requestManager,
                uiHandler,
                mobileEngageRequestModelFactory,
                eventServiceInternal,
                pushTokenStorage,
                notificationEventHandlerProvider,
                silentMessageEventHandlerProvider,
                notificationInformationListenerProvider,
                silentNotificationInformationListenerProvider)
    }

    override val loggingPushInternal: PushInternal by lazy {
        LoggingPushInternal(Emarsys::class.java)
    }

    override val refreshTokenInternal: RefreshTokenInternal by lazy {
        MobileEngageRefreshTokenInternal(
                contactTokenResponseHandler,
                restClient,
                mobileEngageRequestModelFactory)
    }

    override val webViewProvider: WebViewProvider by lazy {
        WebViewProvider(config.application)
    }

    override val currentActivityProvider: CurrentActivityProvider by lazy {
        CurrentActivityProvider()
    }

    override val currentActivityWatchdog: CurrentActivityWatchdog by lazy {
        CurrentActivityWatchdog(currentActivityProvider)
    }

    override val iamJsBridgeFactory: IamJsBridgeFactory by lazy {
        IamJsBridgeFactory(uiHandler)
    }

    override val deviceInfoPayloadStorage: Storage<String?> by lazy {
        StringStorage(MobileEngageStorageKey.DEVICE_INFO_HASH, sharedPreferences)
    }

    override val logLevelStorage: Storage<String?> by lazy {
        StringStorage(CoreStorageKey.LOG_LEVEL, sharedPreferences)
    }

    override val notificationCache: NotificationCache by lazy {
        NotificationCache()
    }

    override val pushTokenProvider: PushTokenProvider by lazy {
        DefaultPushTokenProvider(pushTokenStorage)
    }

    override val onEventActionEventHandlerProvider: EventHandlerProvider by lazy {
        EventHandlerProvider(null)
    }

    override val notificationActionCommandFactory: ActionCommandFactory by lazy {
        ActionCommandFactory(config.application, eventServiceInternal, onEventActionEventHandlerProvider, uiHandler)
    }

    override val silentMessageActionCommandFactory: ActionCommandFactory by lazy {
        ActionCommandFactory(config.application, eventServiceInternal, silentMessageEventHandlerProvider, uiHandler)
    }

    override val geofenceEventHandlerProvider: EventHandlerProvider by lazy {
        EventHandlerProvider(null)
    }

    override val geofenceInternal: GeofenceInternal by lazy {
        val geofenceActionCommandFactory = ActionCommandFactory(config.application, eventServiceInternal, geofenceEventHandlerProvider, uiHandler)

        DefaultGeofenceInternal(mobileEngageRequestModelFactory,
                requestManager,
                GeofenceResponseMapper(),
                PermissionChecker(config.application),
                FusedLocationProviderClient(config.application),
                GeofenceFilter(GEOFENCE_LIMIT),
                GeofencingClient(config.application),
                config.application,
                geofenceActionCommandFactory,
                geofenceEventHandlerProvider,
                BooleanStorage(MobileEngageStorageKey.GEOFENCE_ENABLED, sharedPreferences),
                GeofencePendingIntentProvider(config.application),
                coreSdkHandler,
                uiHandler)
    }

    override val loggingGeofenceInternal: GeofenceInternal by lazy {
        LoggingGeofenceInternal(Emarsys::class.java)
    }

    override val contactTokenResponseHandler: MobileEngageTokenResponseHandler by lazy {
        MobileEngageTokenResponseHandler("contactToken", contactTokenStorage, requestModelHelper)
    }

    override val inlineInAppWebViewFactory: InlineInAppWebViewFactory by lazy {
        InlineInAppWebViewFactory(webViewProvider)
    }

    override val fileDownloader: FileDownloader by lazy {
        FileDownloader(config.application)
    }

    override val remoteMessageMapper: RemoteMessageMapper by lazy {
        RemoteMessageMapper(MetaDataReader(), config.application, fileDownloader, deviceInfo)
    }

    override val appLifecycleObserver: AppLifecycleObserver by lazy {
        AppLifecycleObserver(mobileEngageSession, coreSdkHandler)
    }

    override val keyValueStore: KeyValueStore by lazy {
        DefaultKeyValueStore(sharedPreferences)
    }

    override val predictRequestContext: PredictRequestContext by lazy {
        PredictRequestContext(config.predictMerchantId, deviceInfo, timestampProvider, uuidProvider, keyValueStore)
    }

    override val configInternal: ConfigInternal by lazy {
        val emarsysRequestModelFactory = EmarsysRequestModelFactory(requestContext)

        DefaultConfigInternal(
                requestContext,
                mobileEngageInternal,
                pushInternal,
                pushTokenProvider,
                predictRequestContext,
                deviceInfo,
                requestManager,
                emarsysRequestModelFactory,
                RemoteConfigResponseMapper(RandomProvider(), hardwareIdProvider),
                clientServiceStorage,
                eventServiceStorage,
                deepLinkServiceStorage,
                inboxServiceStorage,
                mobileEngageV2ServiceStorage,
                predictServiceStorage,
                messageInboxServiceStorage,
                logLevelStorage,
                crypto,
                clientServiceInternal)
    }


    override val coreSQLiteDatabase: CoreSQLiteDatabase by lazy {
        coreDbHelper.writableCoreDatabase
    }

    override val logShardTrigger: Runnable by lazy {
        BatchingShardTrigger(
                shardRepository,
                ListSizeAtLeast(10),
                FilterByShardType(FilterByShardType.SHARD_TYPE_LOG),
                ListChunker(10),
                LogShardListMerger(timestampProvider, uuidProvider, deviceInfo, config.mobileEngageApplicationCode, config.predictMerchantId),
                requestManager,
                BatchingShardTrigger.RequestStrategy.TRANSIENT,
                connectionWatchdog)
    }

    override val logger: Logger by lazy {
        Logger(coreSdkHandler,
                shardRepository,
                timestampProvider,
                uuidProvider,
                logLevelStorage,
                config.verboseConsoleLoggingEnabled,
                config.application)
    }

    override val predictRequestModelBuilderProvider: PredictRequestModelBuilderProvider by lazy {
        PredictRequestModelBuilderProvider(predictRequestContext, PredictHeaderFactory(predictRequestContext), predictServiceProvider)
    }

    override val predictInternal: PredictInternal by lazy {
        DefaultPredictInternal(predictRequestContext, requestManager, predictRequestModelBuilderProvider, PredictResponseMapper())
    }

    override val loggingPredictInternal: PredictInternal by lazy {
        LoggingPredictInternal(Emarsys::class.java)
    }

    override val predictShardTrigger: Runnable by lazy {
        BatchingShardTrigger(
                shardRepository,
                ListSizeAtLeast(1),
                FilterByShardType(FilterByShardType.SHARD_TYPE_PREDICT),
                ListChunker(1),
                PredictShardListMerger(predictRequestContext, predictRequestModelBuilderProvider),
                requestManager,
                BatchingShardTrigger.RequestStrategy.PERSISTENT,
                connectionWatchdog
        )
    }

    override val predictServiceProvider: ServiceEndpointProvider by lazy {
        ServiceEndpointProvider(predictServiceStorage, com.emarsys.predict.endpoint.Endpoint.PREDICT_BASE_URL)
    }

    override val predictServiceStorage: Storage<String?> by lazy {
        StringStorage(PredictStorageKey.PREDICT_SERVICE_URL, sharedPreferences)
    }

    private fun createRequestModelRepository(coreDbHelper: CoreDbHelper, inAppEventHandler: InAppEventHandlerInternal): Repository<RequestModel, SqlSpecification> {
        val requestModelRepository = RequestModelRepository(coreDbHelper)
        return RequestRepositoryProxy(
                requestModelRepository,
                displayedIamRepository,
                buttonClickedRepository,
                timestampProvider,
                uuidProvider,
                inAppEventHandler,
                eventServiceEndpointProvider,
                requestModelHelper
        )
    }

    private fun createPublicKey(): PublicKey {
        val publicKeySpec = X509EncodedKeySpec(
                Base64.decode(PUBLIC_KEY, 0)
        )
        val keyFactory = KeyFactory.getInstance("EC")
        return keyFactory.generatePublic(publicKeySpec)
    }

    private fun createRequestModelMappers(): List<Mapper<RequestModel, RequestModel>> {
        return listOf(
                MobileEngageHeaderMapper(requestContext, requestModelHelper),
                OpenIdTokenRequestMapper(requestContext, requestModelHelper),
                ContactTokenHeaderMapper(requestContext, requestModelHelper),
                DeviceEventStateRequestMapper(requestContext, requestModelHelper, deviceEventStateStorage))
    }
}