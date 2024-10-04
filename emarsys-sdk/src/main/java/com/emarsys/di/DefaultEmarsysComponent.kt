package com.emarsys.di

import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.emarsys.Emarsys
import com.emarsys.EmarsysRequestModelFactory
import com.emarsys.clientservice.ClientService
import com.emarsys.clientservice.ClientServiceApi
import com.emarsys.config.Config
import com.emarsys.config.ConfigApi
import com.emarsys.config.ConfigInternal
import com.emarsys.config.DefaultConfigInternal
import com.emarsys.config.EmarsysConfig
import com.emarsys.config.RemoteConfigResponseMapper
import com.emarsys.core.DefaultCoreCompletionHandler
import com.emarsys.core.Mapper
import com.emarsys.core.activity.ActivityLifecycleActionRegistry
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.activity.TransitionSafeCurrentActivityWatchdog
import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.api.proxyApi
import com.emarsys.core.app.AppLifecycleObserver
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.connection.ConnectionProvider
import com.emarsys.core.connection.ConnectionWatchDog
import com.emarsys.core.contentresolver.EmarsysContentResolver
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
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.notification.NotificationManagerHelper
import com.emarsys.core.notification.NotificationManagerProxy
import com.emarsys.core.permission.PermissionChecker
import com.emarsys.core.provider.activity.CurrentActivityProvider
import com.emarsys.core.provider.activity.FallbackActivityProvider
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
import com.emarsys.core.storage.BooleanStorage
import com.emarsys.core.storage.CoreStorageKey
import com.emarsys.core.storage.DefaultKeyValueStore
import com.emarsys.core.storage.KeyValueStore
import com.emarsys.core.storage.SecureSharedPreferencesProvider
import com.emarsys.core.storage.Storage
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.util.FileDownloader
import com.emarsys.core.util.batch.BatchingShardTrigger
import com.emarsys.core.util.batch.ListChunker
import com.emarsys.core.util.log.LogShardListMerger
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.predicate.ListSizeAtLeast
import com.emarsys.core.worker.DefaultWorker
import com.emarsys.core.worker.DelegatorCompletionHandlerProvider
import com.emarsys.core.worker.Worker
import com.emarsys.deeplink.DeepLink
import com.emarsys.deeplink.DeepLinkApi
import com.emarsys.eventservice.EventService
import com.emarsys.eventservice.EventServiceApi
import com.emarsys.geofence.Geofence
import com.emarsys.geofence.GeofenceApi
import com.emarsys.inapp.InApp
import com.emarsys.inapp.InAppApi
import com.emarsys.inbox.MessageInbox
import com.emarsys.inbox.MessageInboxApi
import com.emarsys.mobileengage.DefaultMobileEngageInternal
import com.emarsys.mobileengage.LoggingMobileEngageInternal
import com.emarsys.mobileengage.MobileEngage
import com.emarsys.mobileengage.MobileEngageApi
import com.emarsys.mobileengage.MobileEngageInternal
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.client.ClientServiceInternal
import com.emarsys.mobileengage.client.DefaultClientServiceInternal
import com.emarsys.mobileengage.client.LoggingClientServiceInternal
import com.emarsys.mobileengage.deeplink.DeepLinkAction
import com.emarsys.mobileengage.deeplink.DeepLinkInternal
import com.emarsys.mobileengage.deeplink.DefaultDeepLinkInternal
import com.emarsys.mobileengage.device.DeviceInfoStartAction
import com.emarsys.mobileengage.endpoint.Endpoint
import com.emarsys.mobileengage.event.CacheableEventHandler
import com.emarsys.mobileengage.event.DefaultEventServiceInternal
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.event.LoggingEventServiceInternal
import com.emarsys.mobileengage.geofence.DefaultGeofenceInternal
import com.emarsys.mobileengage.geofence.FetchGeofencesAction
import com.emarsys.mobileengage.geofence.GeofenceFilter
import com.emarsys.mobileengage.geofence.GeofenceInternal
import com.emarsys.mobileengage.geofence.GeofencePendingIntentProvider
import com.emarsys.mobileengage.geofence.GeofenceResponseMapper
import com.emarsys.mobileengage.geofence.LoggingGeofenceInternal
import com.emarsys.mobileengage.iam.AppStartAction
import com.emarsys.mobileengage.iam.DefaultInAppInternal
import com.emarsys.mobileengage.iam.InAppEventHandlerInternal
import com.emarsys.mobileengage.iam.InAppInternal
import com.emarsys.mobileengage.iam.LoggingInAppInternal
import com.emarsys.mobileengage.iam.OverlayInAppPresenter
import com.emarsys.mobileengage.iam.dialog.IamDialog
import com.emarsys.mobileengage.iam.dialog.IamDialogProvider
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridgeFactory
import com.emarsys.mobileengage.iam.jsbridge.JSCommandFactoryProvider
import com.emarsys.mobileengage.iam.jsbridge.OnAppEventListener
import com.emarsys.mobileengage.iam.jsbridge.OnCloseListener
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClickedRepository
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIamRepository
import com.emarsys.mobileengage.iam.model.requestRepositoryProxy.RequestRepositoryProxy
import com.emarsys.mobileengage.iam.webview.IamWebViewFactory
import com.emarsys.mobileengage.inbox.DefaultMessageInboxInternal
import com.emarsys.mobileengage.inbox.LoggingMessageInboxInternal
import com.emarsys.mobileengage.inbox.MessageInboxInternal
import com.emarsys.mobileengage.inbox.MessageInboxResponseMapper
import com.emarsys.mobileengage.notification.ActionCommandFactory
import com.emarsys.mobileengage.push.DefaultPushInternal
import com.emarsys.mobileengage.push.DefaultPushTokenProvider
import com.emarsys.mobileengage.push.LoggingPushInternal
import com.emarsys.mobileengage.push.NotificationInformationListenerProvider
import com.emarsys.mobileengage.push.PushInternal
import com.emarsys.mobileengage.push.PushTokenProvider
import com.emarsys.mobileengage.push.SilentNotificationInformationListenerProvider
import com.emarsys.mobileengage.request.CoreCompletionHandlerRefreshTokenProxyProvider
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.mobileengage.request.mapper.ContactTokenHeaderMapper
import com.emarsys.mobileengage.request.mapper.DefaultRequestHeaderMapper
import com.emarsys.mobileengage.request.mapper.DeviceEventStateRequestMapper
import com.emarsys.mobileengage.request.mapper.MobileEngageHeaderMapper
import com.emarsys.mobileengage.request.mapper.OpenIdTokenRequestMapper
import com.emarsys.mobileengage.responsehandler.ClientInfoResponseHandler
import com.emarsys.mobileengage.responsehandler.DeviceEventStateResponseHandler
import com.emarsys.mobileengage.responsehandler.InAppCleanUpResponseHandler
import com.emarsys.mobileengage.responsehandler.InAppCleanUpResponseHandlerV4
import com.emarsys.mobileengage.responsehandler.InAppMessageResponseHandler
import com.emarsys.mobileengage.responsehandler.MobileEngageClientStateResponseHandler
import com.emarsys.mobileengage.responsehandler.MobileEngageTokenResponseHandler
import com.emarsys.mobileengage.responsehandler.OnEventActionResponseHandler
import com.emarsys.mobileengage.service.mapper.RemoteMessageMapperFactory
import com.emarsys.mobileengage.session.MobileEngageSession
import com.emarsys.mobileengage.session.SessionIdHolder
import com.emarsys.mobileengage.storage.MobileEngageStorageKey
import com.emarsys.mobileengage.util.RequestModelHelper
import com.emarsys.oneventaction.OnEventAction
import com.emarsys.oneventaction.OnEventActionApi
import com.emarsys.predict.DefaultPredictInternal
import com.emarsys.predict.LoggingPredictInternal
import com.emarsys.predict.Predict
import com.emarsys.predict.PredictApi
import com.emarsys.predict.PredictInternal
import com.emarsys.predict.PredictResponseMapper
import com.emarsys.predict.PredictRestricted
import com.emarsys.predict.PredictRestrictedApi
import com.emarsys.predict.provider.PredictRequestModelBuilderProvider
import com.emarsys.predict.request.PredictHeaderFactory
import com.emarsys.predict.request.PredictRequestContext
import com.emarsys.predict.response.VisitorIdResponseHandler
import com.emarsys.predict.response.XPResponseHandler
import com.emarsys.predict.shard.PredictShardListMerger
import com.emarsys.predict.storage.PredictStorageKey
import com.emarsys.push.Push
import com.emarsys.push.PushApi
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailabilityLight
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import java.lang.reflect.Method
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec

open class DefaultEmarsysComponent(config: EmarsysConfig) : EmarsysComponent {
    companion object {
        private const val EMARSYS_SHARED_PREFERENCES_NAME = "emarsys_shared_preferences"
        private const val EMARSYS_SECURE_SHARED_PREFERENCES_NAME =
            "emarsys_secure_shared_preferences"
        private const val GEOFENCE_LIMIT = 99
        private const val PUBLIC_KEY =
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAELjWEUIBX9zlm1OI4gF1hMCBLzpaBwgs9HlmSIBAqP4MDGy4ibOOV3FVDrnAY0Q34LZTbPBlp3gRNZJ19UoSy2Q=="
    }

    private val isHuaweiServiceAvailable: Boolean =
        try {
            val huaweiServiceCheckerClass =
                Class.forName(
                    "com.emarsys.HuaweiServiceChecker",
                    true,
                    config.application.classLoader
                )
            val huaweiServiceChecker = huaweiServiceCheckerClass.newInstance()

            val types = listOf<Class<*>>(Context::class.java).toTypedArray()
            val method: Method = huaweiServiceCheckerClass.getDeclaredMethod("check", *types)
            method.isAccessible = true

            method.invoke(huaweiServiceChecker, config.application.applicationContext) as Boolean
        } catch (ignored: Exception) {
            false
        }

    private val isGoogleAvailable = GoogleApiAvailabilityLight.getInstance()
        .isGooglePlayServicesAvailable(config.application) == ConnectionResult.SUCCESS

    override val isGooglePlayServiceAvailable =
        if (isGoogleAvailable == isHuaweiServiceAvailable) {
            true
        } else {
            !isHuaweiServiceAvailable
        }

    override val notificationOpenedActivityClass: Class<*>
        get() = com.emarsys.NotificationOpenedActivity::class.java

    final override val concurrentHandlerHolder: ConcurrentHandlerHolder =
        ConcurrentHandlerHolderFactory.create()

    override val deepLink: DeepLinkApi =
        (DeepLink() as DeepLinkApi).proxyApi(concurrentHandlerHolder)

    override val messageInbox: MessageInboxApi =
        (MessageInbox() as MessageInboxApi).proxyApi(concurrentHandlerHolder)

    override val loggingMessageInbox: MessageInboxApi =
        (MessageInbox(true) as MessageInboxApi).proxyApi(concurrentHandlerHolder)

    override val inApp: InAppApi = (InApp() as InAppApi).proxyApi(concurrentHandlerHolder)

    override val loggingInApp: InAppApi =
        (InApp(true) as InAppApi).proxyApi(concurrentHandlerHolder)

    override val onEventAction: OnEventActionApi =
        (OnEventAction() as OnEventActionApi).proxyApi(concurrentHandlerHolder)

    override val loggingOnEventAction: OnEventActionApi =
        (OnEventAction() as OnEventActionApi).proxyApi(concurrentHandlerHolder)

    override val push: PushApi = (Push() as PushApi).proxyApi(concurrentHandlerHolder)

    override val loggingPush: PushApi = (Push(true) as PushApi).proxyApi(concurrentHandlerHolder)

    override val predict: PredictApi = (Predict() as PredictApi).proxyApi(concurrentHandlerHolder)

    override val loggingPredict: PredictApi =
        (Predict(true) as PredictApi).proxyApi(concurrentHandlerHolder)

    override val config: ConfigApi = (Config() as ConfigApi).proxyApi(concurrentHandlerHolder)

    override val geofence: GeofenceApi =
        (Geofence() as GeofenceApi).proxyApi(concurrentHandlerHolder)

    override val loggingGeofence: GeofenceApi =
        (Geofence(true) as GeofenceApi).proxyApi(concurrentHandlerHolder)

    override val mobileEngage: MobileEngageApi =
        (MobileEngage() as MobileEngageApi).proxyApi(concurrentHandlerHolder)

    override val loggingMobileEngage: MobileEngageApi =
        (MobileEngage(true) as MobileEngageApi).proxyApi(concurrentHandlerHolder)

    override val predictRestricted: PredictRestrictedApi =
        (PredictRestricted() as PredictRestrictedApi).proxyApi(concurrentHandlerHolder)

    override val loggingPredictRestricted: PredictRestrictedApi =
        (PredictRestricted(true) as PredictRestrictedApi).proxyApi(concurrentHandlerHolder)

    override val clientService: ClientServiceApi =
        (ClientService() as ClientServiceApi).proxyApi(concurrentHandlerHolder)

    override val loggingClientService: ClientServiceApi =
        (ClientService(true) as ClientServiceApi).proxyApi(concurrentHandlerHolder)

    override val eventService: EventServiceApi =
        (EventService() as EventServiceApi).proxyApi(concurrentHandlerHolder)

    override val loggingEventService: EventServiceApi =
        (EventService(true) as EventServiceApi).proxyApi(concurrentHandlerHolder)

    override val responseHandlersProcessor: ResponseHandlersProcessor by lazy {
        ResponseHandlersProcessor(mutableListOf())
    }

    override val clipboardManager: ClipboardManager by lazy {
        ContextCompat.getSystemService(
            config.application as Context,
            ClipboardManager::class.java
        ) as ClipboardManager
    }

    override val transitionSafeCurrentActivityWatchdog: TransitionSafeCurrentActivityWatchdog by lazy {
        TransitionSafeCurrentActivityWatchdog(
            concurrentHandlerHolder.coreHandler,
            currentActivityProvider
        )
    }

    override val overlayInAppPresenter: OverlayInAppPresenter by lazy {
        OverlayInAppPresenter(
            concurrentHandlerHolder,
            IamDialogProvider(
                concurrentHandlerHolder,
                timestampProvider,
                inAppInternal,
                displayedIamRepository,
                webViewFactory
            ),
            timestampProvider,
            transitionSafeCurrentActivityWatchdog
        )
    }

    override val activityLifecycleActionRegistry: ActivityLifecycleActionRegistry by lazy {
        val actions = mutableListOf(
            DeviceInfoStartAction(clientServiceInternal, deviceInfoPayloadStorage, deviceInfo),
            DeepLinkAction(deepLinkInternal),
            FetchGeofencesAction(geofenceInternal),
            AppStartAction(eventServiceInternal, contactTokenStorage)
        )
        ActivityLifecycleActionRegistry(concurrentHandlerHolder, currentActivityProvider, actions)
    }

    override val activityLifecycleWatchdog: ActivityLifecycleWatchdog by lazy {
        ActivityLifecycleWatchdog(activityLifecycleActionRegistry)
    }

    fun initializeResponseHandlers(config: EmarsysConfig) {
        val responseHandlers: MutableList<AbstractResponseHandler?> = ArrayList()
        responseHandlers.add(VisitorIdResponseHandler(keyValueStore, predictServiceProvider))
        responseHandlers.add(XPResponseHandler(keyValueStore, predictServiceProvider))
        responseHandlers.add(
            MobileEngageTokenResponseHandler(
                "refreshToken",
                refreshTokenStorage,
                requestModelHelper
            )
        )
        responseHandlers.add(contactTokenResponseHandler)
        responseHandlers.add(
            MobileEngageClientStateResponseHandler(
                clientStateStorage,
                requestModelHelper
            )
        )
        responseHandlers.add(ClientInfoResponseHandler(deviceInfo, deviceInfoPayloadStorage))
        responseHandlers.add(InAppMessageResponseHandler(overlayInAppPresenter))
        responseHandlers.add(
            InAppCleanUpResponseHandler(
                displayedIamRepository,
                buttonClickedRepository,
                requestModelHelper
            )
        )
        responseHandlers.add(
            InAppCleanUpResponseHandlerV4(
                displayedIamRepository,
                buttonClickedRepository,
                requestModelHelper
            )
        )
        responseHandlers.add(
            OnEventActionResponseHandler(
                ActionCommandFactory(
                    config.application,
                    eventServiceInternal,
                    onEventActionCacheableEventHandler,
                    concurrentHandlerHolder
                ),
                displayedIamRepository,
                eventServiceInternal,
                timestampProvider,
                concurrentHandlerHolder
            )
        )
        responseHandlers.add(
            DeviceEventStateResponseHandler(
                deviceEventStateStorage,
                requestModelHelper
            )
        )
        responseHandlersProcessor.addResponseHandlers(responseHandlers)
    }

    override val restClient: RestClient by lazy {
        RestClient(
            ConnectionProvider(),
            timestampProvider,
            responseHandlersProcessor,
            createRequestModelMappers(),
            concurrentHandlerHolder
        )
    }

    private val oldSharedPrefs = config.application.getSharedPreferences(
        EMARSYS_SHARED_PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    override val sharedPreferences: SharedPreferences =
        SecureSharedPreferencesProvider(
            config.application,
            EMARSYS_SECURE_SHARED_PREFERENCES_NAME,
            oldSharedPrefs
        ).provide()

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
        val hardwareRepository = HardwareRepository(coreDbHelper, concurrentHandlerHolder)
        val hardwareIdentificationCrypto = HardwareIdentificationCrypto(config.sharedSecret, crypto)
        val emarsysContentResolver = EmarsysContentResolver(config.application)
        val hardwareIdContentResolver = HardwareIdContentResolver(
            emarsysContentResolver,
            hardwareIdentificationCrypto,
            config.sharedPackageNames
        )
        HardwareIdProvider(
            uuidProvider,
            hardwareRepository,
            hardwareIdStorage,
            hardwareIdContentResolver,
            hardwareIdentificationCrypto
        )
    }

    override val deviceInfo: DeviceInfo by lazy {
        val notificationManagerCompat = NotificationManagerCompat.from(config.application)
        val notificationManager =
            config.application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationManagerProxy =
            NotificationManagerProxy(notificationManager, notificationManagerCompat)
        val notificationSettings: NotificationSettings =
            NotificationManagerHelper(notificationManagerProxy)
        DeviceInfo(
            config.application,
            hardwareIdProvider,
            VersionProvider(),
            LanguageProvider(),
            notificationSettings,
            config.automaticPushTokenSendingEnabled,
            isGooglePlayServiceAvailable
        )
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
            config.applicationCode,
            null,
            null,
            deviceInfo,
            timestampProvider,
            uuidProvider,
            clientStateStorage,
            contactTokenStorage,
            refreshTokenStorage,
            pushTokenStorage,
            contactFieldValueStorage,
            sessionIdHolder
        )
    }

    override val inAppEventHandlerInternal: InAppEventHandlerInternal by lazy {
        InAppEventHandlerInternal()
    }

    override val shardRepository: Repository<ShardModel, SqlSpecification> by lazy {
        ShardModelRepository(coreDbHelper, concurrentHandlerHolder)
    }

    override val buttonClickedRepository: Repository<ButtonClicked, SqlSpecification> by lazy {
        ButtonClickedRepository(coreDbHelper, concurrentHandlerHolder)
    }

    override val displayedIamRepository: Repository<DisplayedIam, SqlSpecification> by lazy {
        DisplayedIamRepository(coreDbHelper, concurrentHandlerHolder)
    }

    override val requestModelRepository: Repository<RequestModel, SqlSpecification> by lazy {
        createRequestModelRepository(coreDbHelper, inAppEventHandlerInternal)
    }

    override val connectionWatchdog: ConnectionWatchDog by lazy {
        ConnectionWatchDog(config.application, concurrentHandlerHolder)
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

    override val messageInboxServiceStorage: Storage<String?> by lazy {
        StringStorage(MobileEngageStorageKey.MESSAGE_INBOX_SERVICE_URL, sharedPreferences)
    }

    override val deviceEventStateStorage: Storage<String?> by lazy {
        StringStorage(MobileEngageStorageKey.DEVICE_EVENT_STATE, sharedPreferences)
    }

    override val geofenceInitialEnterTriggerEnabledStorage: Storage<Boolean?> by lazy {
        BooleanStorage(MobileEngageStorageKey.GEOFENCE_INITIAL_ENTER_TRIGGER, sharedPreferences)
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

    override val messageInboxServiceProvider: ServiceEndpointProvider by lazy {
        ServiceEndpointProvider(messageInboxServiceStorage, Endpoint.ME_V3_INBOX_HOST)
    }

    override val requestModelHelper: RequestModelHelper by lazy {
        RequestModelHelper(
            clientServiceEndpointProvider,
            eventServiceEndpointProvider,
            messageInboxServiceProvider
        )
    }

    override val coreCompletionHandlerRefreshTokenProxyProvider: CoreCompletionHandlerRefreshTokenProxyProvider by lazy {
        val coreCompletionHandlerMiddlewareProvider = CoreCompletionHandlerMiddlewareProvider(
            requestModelRepository,
            concurrentHandlerHolder
        )
        CoreCompletionHandlerRefreshTokenProxyProvider(
            coreCompletionHandlerMiddlewareProvider,
            restClient,
            contactTokenStorage,
            pushTokenStorage,
            coreCompletionHandler,
            requestModelHelper,
            contactTokenResponseHandler,
            mobileEngageRequestModelFactory
        )
    }

    override val worker: Worker by lazy {
        DefaultWorker(
            requestModelRepository,
            connectionWatchdog,
            concurrentHandlerHolder,
            coreCompletionHandler,
            restClient,
            coreCompletionHandlerRefreshTokenProxyProvider
        )
    }

    override val requestManager: RequestManager by lazy {
        RequestManager(
            concurrentHandlerHolder,
            requestModelRepository,
            shardRepository,
            worker,
            restClient,
            coreCompletionHandler,
            coreCompletionHandler,
            coreCompletionHandlerRefreshTokenProxyProvider,
            DelegatorCompletionHandlerProvider()
        )
    }

    override val mobileEngageRequestModelFactory: MobileEngageRequestModelFactory by lazy {
        MobileEngageRequestModelFactory(
            requestContext,
            clientServiceEndpointProvider,
            eventServiceEndpointProvider,
            messageInboxServiceProvider,
            buttonClickedRepository
        )
    }

    override val loggingMobileEngageInternal: MobileEngageInternal by lazy {
        LoggingMobileEngageInternal(Emarsys::class.java)
    }

    override val eventServiceInternal: EventServiceInternal by lazy {
        DefaultEventServiceInternal(mobileEngageRequestModelFactory, requestManager)
    }

    override val loggingEventServiceInternal: EventServiceInternal by lazy {
        LoggingEventServiceInternal(Emarsys::class.java)
    }

    override val mobileEngageSession: MobileEngageSession by lazy {
        MobileEngageSession(
            timestampProvider,
            uuidProvider,
            eventServiceInternal,
            sessionIdHolder,
            contactTokenStorage,
            requestContext
        )
    }

    override val notificationCacheableEventHandler: CacheableEventHandler by lazy {
        CacheableEventHandler()
    }

    override val silentMessageCacheableEventHandler: CacheableEventHandler by lazy {
        CacheableEventHandler()
    }

    override val notificationInformationListenerProvider: NotificationInformationListenerProvider by lazy {
        NotificationInformationListenerProvider(null)
    }

    override val silentNotificationInformationListenerProvider: SilentNotificationInformationListenerProvider by lazy {
        SilentNotificationInformationListenerProvider(null)
    }

    override val mobileEngageInternal: MobileEngageInternal by lazy {
        DefaultMobileEngageInternal(
            requestManager,
            mobileEngageRequestModelFactory,
            requestContext,
            mobileEngageSession,
            sessionIdHolder
        )
    }

    override val clientServiceInternal: ClientServiceInternal by lazy {
        DefaultClientServiceInternal(requestManager, mobileEngageRequestModelFactory)
    }

    override val loggingClientServiceInternal: ClientServiceInternal by lazy {
        LoggingClientServiceInternal(Emarsys::class.java)
    }

    override val messageInboxInternal: MessageInboxInternal by lazy {
        DefaultMessageInboxInternal(
            concurrentHandlerHolder,
            requestManager,
            mobileEngageRequestModelFactory,
            MessageInboxResponseMapper()
        )
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
        DefaultDeepLinkInternal(requestContext, deepLinkServiceProvider, requestManager)
    }

    override val pushInternal: PushInternal by lazy {
        DefaultPushInternal(
            requestManager,
            concurrentHandlerHolder,
            mobileEngageRequestModelFactory,
            eventServiceInternal,
            pushTokenStorage,
            notificationCacheableEventHandler,
            silentMessageCacheableEventHandler,
            notificationInformationListenerProvider,
            silentNotificationInformationListenerProvider
        )
    }

    override val loggingPushInternal: PushInternal by lazy {
        LoggingPushInternal(Emarsys::class.java)
    }

    override val webViewFactory: IamWebViewFactory by lazy {
        IamWebViewFactory(
            iamJsBridgeFactory,
            jsCommandFactoryProvider,
            concurrentHandlerHolder
        )
    }

    override val currentActivityProvider: CurrentActivityProvider by lazy {
        CurrentActivityProvider(fallbackActivityProvider = FallbackActivityProvider())
    }

    override val iamJsBridgeFactory: IamJsBridgeFactory by lazy {
        IamJsBridgeFactory(concurrentHandlerHolder)
    }

    override val jsCommandFactoryProvider: JSCommandFactoryProvider by lazy {
        JSCommandFactoryProvider(
            currentActivityProvider,
            concurrentHandlerHolder,
            inAppInternal,
            buttonClickedRepository,
            jsOnCloseListener,
            jsOnAppEventListener,
            timestampProvider,
            clipboardManager
        )
    }

    override val jsOnCloseListener: OnCloseListener = {
        val currentActivity = currentActivityProvider.get()
        if (currentActivity is FragmentActivity) {
            concurrentHandlerHolder.postOnMain {
                val fragment =
                    currentActivity.supportFragmentManager.findFragmentByTag(IamDialog.TAG)
                if (fragment is DialogFragment) {
                    fragment.dismiss()
                }
                fragment?.let {
                    currentActivity.supportFragmentManager.beginTransaction().remove(it).commitNow()
                }
            }
        }
    }

    override val jsOnAppEventListener: OnAppEventListener = { property: String?, json: JSONObject ->
        concurrentHandlerHolder.postOnMain {
            val payload = json.optJSONObject("payload")
            val currentActivity = currentActivityProvider.get()
            if (property != null && currentActivity != null) {
                inAppInternal.eventHandler?.handleEvent(currentActivity, property, payload)
            }
        }
    }

    override val deviceInfoPayloadStorage: Storage<String?> by lazy {
        StringStorage(MobileEngageStorageKey.DEVICE_INFO_HASH, sharedPreferences)
    }

    override val logLevelStorage: Storage<String?> by lazy {
        StringStorage(CoreStorageKey.LOG_LEVEL, sharedPreferences)
    }

    override val pushTokenProvider: PushTokenProvider by lazy {
        DefaultPushTokenProvider(pushTokenStorage)
    }

    override val onEventActionCacheableEventHandler: CacheableEventHandler by lazy {
        CacheableEventHandler()
    }

    override val notificationActionCommandFactory: ActionCommandFactory by lazy {
        ActionCommandFactory(
            config.application,
            eventServiceInternal,
            notificationCacheableEventHandler,
            concurrentHandlerHolder
        )
    }

    override val silentMessageActionCommandFactory: ActionCommandFactory by lazy {
        ActionCommandFactory(
            config.application,
            eventServiceInternal,
            silentMessageCacheableEventHandler,
            concurrentHandlerHolder
        )
    }

    override val geofenceCacheableEventHandler: CacheableEventHandler by lazy {
        CacheableEventHandler()
    }
    override val fusedLocationProviderClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(config.application)
    }
    override val geofenceInternal: GeofenceInternal by lazy {
        val geofenceActionCommandFactory = ActionCommandFactory(
            config.application,
            eventServiceInternal,
            geofenceCacheableEventHandler,
            concurrentHandlerHolder
        )

        DefaultGeofenceInternal(
            mobileEngageRequestModelFactory,
            requestManager,
            GeofenceResponseMapper(),
            PermissionChecker(config.application),
            fusedLocationProviderClient,
            GeofenceFilter(GEOFENCE_LIMIT),
            LocationServices.getGeofencingClient(config.application),
            geofenceActionCommandFactory,
            geofenceCacheableEventHandler,
            BooleanStorage(MobileEngageStorageKey.GEOFENCE_ENABLED, sharedPreferences),
            GeofencePendingIntentProvider(config.application),
            concurrentHandlerHolder,
            geofenceInitialEnterTriggerEnabledStorage
        )
    }

    override val loggingGeofenceInternal: GeofenceInternal by lazy {
        LoggingGeofenceInternal(Emarsys::class.java)
    }

    override val contactTokenResponseHandler: MobileEngageTokenResponseHandler by lazy {
        MobileEngageTokenResponseHandler("contactToken", contactTokenStorage, requestModelHelper)
    }

    override val remoteMessageMapperFactory: RemoteMessageMapperFactory by lazy {
        RemoteMessageMapperFactory(
            MetaDataReader(),
            config.application as Context,
            uuidProvider,
        )
    }

    override val fileDownloader: FileDownloader by lazy {
        FileDownloader(config.application)
    }

    override val appLifecycleObserver: AppLifecycleObserver by lazy {
        AppLifecycleObserver(mobileEngageSession, concurrentHandlerHolder)
    }

    override val keyValueStore: KeyValueStore by lazy {
        DefaultKeyValueStore(sharedPreferences)
    }

    override val predictRequestContext: PredictRequestContext by lazy {
        PredictRequestContext(
            config.merchantId,
            deviceInfo,
            timestampProvider,
            uuidProvider,
            keyValueStore
        )
    }

    override val configInternal: ConfigInternal by lazy {
        val emarsysRequestModelFactory = EmarsysRequestModelFactory(requestContext)

        DefaultConfigInternal(
            requestContext,
            mobileEngageInternal,
            pushInternal,
            predictRequestContext,
            deviceInfo,
            requestManager,
            emarsysRequestModelFactory,
            RemoteConfigResponseMapper(RandomProvider(), hardwareIdProvider),
            clientServiceStorage,
            eventServiceStorage,
            deepLinkServiceStorage,
            predictServiceStorage,
            messageInboxServiceStorage,
            logLevelStorage,
            crypto,
            clientServiceInternal,
            concurrentHandlerHolder
        )
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
            LogShardListMerger(
                timestampProvider,
                uuidProvider,
                deviceInfo,
                config.applicationCode,
                config.merchantId
            ),
            requestManager,
            BatchingShardTrigger.RequestStrategy.TRANSIENT,
            connectionWatchdog
        )
    }

    override val logger: Logger by lazy {
        Logger(
            concurrentHandlerHolder,
            shardRepository,
            timestampProvider,
            uuidProvider,
            logLevelStorage,
            config.verboseConsoleLoggingEnabled,
            config.application
        )
    }

    override val predictRequestModelBuilderProvider: PredictRequestModelBuilderProvider by lazy {
        PredictRequestModelBuilderProvider(
            predictRequestContext,
            PredictHeaderFactory(predictRequestContext),
            predictServiceProvider
        )
    }

    override val predictInternal: PredictInternal by lazy {
        DefaultPredictInternal(
            predictRequestContext,
            requestManager,
            concurrentHandlerHolder,
            predictRequestModelBuilderProvider,
            PredictResponseMapper()
        )
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
        ServiceEndpointProvider(
            predictServiceStorage,
            com.emarsys.predict.endpoint.Endpoint.PREDICT_BASE_URL
        )
    }

    override val predictServiceStorage: Storage<String?> by lazy {
        StringStorage(PredictStorageKey.PREDICT_SERVICE_URL, sharedPreferences)
    }

    private fun createRequestModelRepository(
        coreDbHelper: CoreDbHelper,
        inAppEventHandler: InAppEventHandlerInternal
    ): Repository<RequestModel, SqlSpecification> {
        val requestModelRepository = RequestModelRepository(coreDbHelper, concurrentHandlerHolder)
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
            DefaultRequestHeaderMapper(requestContext),
            DeviceEventStateRequestMapper(
                requestContext,
                requestModelHelper,
                deviceEventStateStorage
            )
        )
    }

    private fun logInitialSetup(emarsysConfig: EmarsysConfig) {
        if (!emarsysConfig.verboseConsoleLoggingEnabled) {
            return
        }
        Log.d("EMARSYS_SDK", "------------CONFIG START------------")
        Log.d("EMARSYS_SDK", "ApplicationCode : ${emarsysConfig.applicationCode}")
        Log.d("EMARSYS_SDK", "MerchantId : ${emarsysConfig.merchantId}")
        Log.d("EMARSYS_SDK", "ExperimentalFeatures : ${emarsysConfig.experimentalFeatures}")
        Log.d(
            "EMARSYS_SDK",
            "AutomaticPushSendingEnabled : ${emarsysConfig.automaticPushTokenSendingEnabled}"
        )
        Log.d("EMARSYS_SDK", "HardwareId : ${hardwareIdProvider.provideHardwareId()}")

        Log.d(
            "EMARSYS_SDK",
            "${MobileEngageStorageKey.EVENT_SERVICE_URL} : ${eventServiceEndpointProvider.provideEndpointHost()}"
        )
        Log.d(
            "EMARSYS_SDK",
            "${MobileEngageStorageKey.CLIENT_SERVICE_URL} : ${clientServiceEndpointProvider.provideEndpointHost()}"
        )
        Log.d(
            "EMARSYS_SDK",
            "${MobileEngageStorageKey.MESSAGE_INBOX_SERVICE_URL} : ${messageInboxServiceProvider.provideEndpointHost()}"
        )
        Log.d(
            "EMARSYS_SDK",
            "${MobileEngageStorageKey.DEEPLINK_SERVICE_URL} : ${deepLinkServiceProvider.provideEndpointHost()}"
        )
        Log.d(
            "EMARSYS_SDK",
            "${PredictStorageKey.PREDICT_SERVICE_URL} : ${predictServiceProvider.provideEndpointHost()}"
        )

        Log.d(
            "EMARSYS_SDK",
            "${MobileEngageStorageKey.CONTACT_TOKEN} : ${contactTokenStorage.get()}"
        )
        Log.d("EMARSYS_SDK", "${MobileEngageStorageKey.CLIENT_STATE} : ${clientStateStorage.get()}")
        Log.d(
            "EMARSYS_SDK",
            "${MobileEngageStorageKey.REFRESH_TOKEN} : ${refreshTokenStorage.get()}"
        )
        Log.d(
            "EMARSYS_SDK",
            "${MobileEngageStorageKey.DEVICE_EVENT_STATE} : ${
                JSONObject(deviceEventStateStorage.get() ?: "{}").toString(4)
            }"
        )
        Log.d(
            "EMARSYS_SDK",
            "${MobileEngageStorageKey.GEOFENCE_ENABLED} : ${geofenceInternal.isEnabled()}"
        )
        Log.d(
            "EMARSYS_SDK",
            "${MobileEngageStorageKey.GEOFENCE_INITIAL_ENTER_TRIGGER} : ${geofenceInitialEnterTriggerEnabledStorage.get()}"
        )
        Log.d(
            "EMARSYS_SDK",
            "${MobileEngageStorageKey.PUSH_TOKEN} : ${pushTokenProvider.providePushToken()}"
        )
        Log.d(
            "EMARSYS_SDK",
            "${MobileEngageStorageKey.DEVICE_INFO_HASH} : ${
                JSONObject(deviceInfoPayloadStorage.get() ?: "{}").toString(
                    4
                )
            }"
        )
        Log.d("EMARSYS_SDK", "${CoreStorageKey.LOG_LEVEL} : ${logLevelStorage.get()}")
        Log.d("EMARSYS_SDK", "------------CONFIG END------------")
    }
}