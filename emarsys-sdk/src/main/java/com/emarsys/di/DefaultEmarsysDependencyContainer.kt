package com.emarsys.di

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Base64
import androidx.core.app.NotificationManagerCompat
import com.emarsys.Emarsys
import com.emarsys.EmarsysRequestModelFactory
import com.emarsys.config.*
import com.emarsys.core.DefaultCoreCompletionHandler
import com.emarsys.core.Mapper
import com.emarsys.core.activity.ActivityLifecycleAction
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.activity.CurrentActivityWatchdog
import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.api.proxyApi
import com.emarsys.core.concurrency.CoreSdkHandlerProvider
import com.emarsys.core.connection.ConnectionProvider
import com.emarsys.core.connection.ConnectionWatchDog
import com.emarsys.core.crypto.Crypto
import com.emarsys.core.database.CoreSQLiteDatabase
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.di.addDependency
import com.emarsys.core.di.getDependency
import com.emarsys.core.endpoint.ServiceEndpointProvider
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
import com.emarsys.mobileengage.request.MobileEngageHeaderMapper
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.mobileengage.responsehandler.*
import com.emarsys.mobileengage.storage.MobileEngageStorageKey
import com.emarsys.mobileengage.util.RequestHeaderUtils
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
import com.google.firebase.iid.FirebaseInstanceId
import org.json.JSONObject
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*
import java.util.concurrent.ConcurrentHashMap

open class DefaultEmarsysDependencyContainer(emarsysConfig: EmarsysConfig) : EmarsysDependencyContainer {

    companion object {
        private const val EMARSYS_SHARED_PREFERENCES_NAME = "emarsys_shared_preferences"
        private const val GEOFENCE_LIMIT = 100
        private const val PUBLIC_KEY = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAELjWEUIBX9zlm1OI4gF1hMCBLzpaBwgs9HlmSIBAqP4MDGy4ibOOV3FVDrnAY0Q34LZTbPBlp3gRNZJ19UoSy2Q=="
    }

    override val dependencies: MutableMap<String, Any?> = ConcurrentHashMap()

    init {
        initializeDependencies(emarsysConfig)
    }

    private fun setupDependencies(application: Application) {
        initializeInAppPresenter(application)
        initializeResponseHandlers()
        initializeActivityLifecycleWatchdog()
    }

    private fun initializeDependencies(config: EmarsysConfig) {
        val application = config.application
        val coreSdkHandler: Handler = CoreSdkHandlerProvider().provideHandler()
        addDependency(dependencies, coreSdkHandler, "coreSdkHandler")

        Handler(Looper.getMainLooper()).also {
            addDependency(dependencies, it, "uiHandler")
        }

        addDependency(dependencies, (MobileEngage() as MobileEngageApi).proxyApi(coreSdkHandler), "defaultInstance")

        addDependency(dependencies, (MobileEngage(true) as MobileEngageApi).proxyApi(coreSdkHandler), "loggingInstance")

        addDependency(dependencies, (DeepLink() as DeepLinkApi).proxyApi(coreSdkHandler), "defaultInstance")

        addDependency(dependencies, (DeepLink(true) as DeepLinkApi).proxyApi(coreSdkHandler), "loggingInstance")

        addDependency(dependencies, (EventService() as EventServiceApi).proxyApi(coreSdkHandler), "defaultInstance")

        addDependency(dependencies, (EventService(true) as EventServiceApi).proxyApi(coreSdkHandler), "loggingInstance")

        addDependency(dependencies, (InApp() as InAppApi).proxyApi(coreSdkHandler), "defaultInstance")

        addDependency(dependencies, (InApp(true) as InAppApi).proxyApi(coreSdkHandler), "loggingInstance")

        addDependency(dependencies, (Push() as PushApi).proxyApi(coreSdkHandler), "defaultInstance")

        addDependency(dependencies, (Push(true) as PushApi).proxyApi(coreSdkHandler), "loggingInstance")

        addDependency(dependencies, (Predict() as PredictApi).proxyApi(coreSdkHandler), "defaultInstance")

        addDependency(dependencies, (Predict(true) as PredictApi).proxyApi(coreSdkHandler), "loggingInstance")

        addDependency(dependencies, (Config() as ConfigApi).proxyApi(coreSdkHandler))

        addDependency(dependencies, (Geofence() as GeofenceApi).proxyApi(coreSdkHandler), "defaultInstance")

        addDependency(dependencies, (Geofence(true) as GeofenceApi).proxyApi(coreSdkHandler), "loggingInstance")

        addDependency(dependencies, (Inbox() as InboxApi).proxyApi(coreSdkHandler), "defaultInstance")

        addDependency(dependencies, (Inbox(true) as InboxApi).proxyApi(coreSdkHandler), "loggingInstance")

        addDependency(dependencies, (MessageInbox() as MessageInboxApi).proxyApi(coreSdkHandler), "defaultInstance")

        addDependency(dependencies, (MessageInbox(true) as MessageInboxApi).proxyApi(coreSdkHandler), "loggingInstance")

        coreSdkHandler.post {
            initializeDependenciesInBackground(application, config)
            setupDependencies(application)
        }
    }

    private fun initializeDependenciesInBackground(application: Application, config: EmarsysConfig) {
        val prefs = application.getSharedPreferences(EMARSYS_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val uiHandler = Handler(Looper.getMainLooper())

        TimestampProvider().also {
            addDependency(dependencies, it)
        }
        UUIDProvider().also {
            addDependency(dependencies, it)
        }
        StringStorage(MobileEngageStorageKey.DEVICE_INFO_HASH, prefs).also {
            addDependency(dependencies, it, MobileEngageStorageKey.DEVICE_INFO_HASH.key)
        }
        StringStorage(MobileEngageStorageKey.CONTACT_TOKEN, prefs).also {
            addDependency(dependencies, it, MobileEngageStorageKey.CONTACT_TOKEN.key)
        }
        StringStorage(MobileEngageStorageKey.REFRESH_TOKEN, prefs).also {
            addDependency(dependencies, it, MobileEngageStorageKey.REFRESH_TOKEN.key)
        }
        StringStorage(MobileEngageStorageKey.CLIENT_STATE, prefs).also {
            addDependency(dependencies, it, MobileEngageStorageKey.CLIENT_STATE.key)
        }
        StringStorage(MobileEngageStorageKey.CONTACT_FIELD_VALUE, prefs).also {
            addDependency(dependencies, it, MobileEngageStorageKey.CONTACT_FIELD_VALUE.key)
        }
        val geofenceEnabledStorage = BooleanStorage(MobileEngageStorageKey.GEOFENCE_ENABLED, prefs)

        addDependency(dependencies, StringStorage(MobileEngageStorageKey.PUSH_TOKEN, prefs), MobileEngageStorageKey.PUSH_TOKEN.key)
        StringStorage(CoreStorageKey.LOG_LEVEL, prefs).also {
            addDependency(dependencies, it, CoreStorageKey.LOG_LEVEL.key)
        }
        DefaultPushTokenProvider(getPushTokenStorage()).also {
            addDependency(dependencies, it as PushTokenProvider)
        }
        StringStorage(MobileEngageStorageKey.EVENT_SERVICE_URL, prefs).also {
            addDependency(dependencies, it, MobileEngageStorageKey.EVENT_SERVICE_URL.key)
        }
        StringStorage(MobileEngageStorageKey.CLIENT_SERVICE_URL, prefs).also {
            addDependency(dependencies, it, MobileEngageStorageKey.CLIENT_SERVICE_URL.key)
        }
        StringStorage(MobileEngageStorageKey.INBOX_SERVICE_URL, prefs).also {
            addDependency(dependencies, it, MobileEngageStorageKey.INBOX_SERVICE_URL.key)
        }
        StringStorage(MobileEngageStorageKey.MESSAGE_INBOX_SERVICE_URL, prefs).also {
            addDependency(dependencies, it, MobileEngageStorageKey.MESSAGE_INBOX_SERVICE_URL.key)
        }
        StringStorage(MobileEngageStorageKey.ME_V2_SERVICE_URL, prefs).also {
            addDependency(dependencies, it, MobileEngageStorageKey.ME_V2_SERVICE_URL.key)
        }
        StringStorage(MobileEngageStorageKey.DEEPLINK_SERVICE_URL, prefs).also {
            addDependency(dependencies, it, MobileEngageStorageKey.DEEPLINK_SERVICE_URL.key)
        }
        StringStorage(PredictStorageKey.PREDICT_SERVICE_URL, prefs).also {
            addDependency(dependencies, it, PredictStorageKey.PREDICT_SERVICE_URL.key)
        }
        ServiceEndpointProvider(getEventServiceStorage(), Endpoint.ME_V3_EVENT_HOST).also {
            addDependency(dependencies, it, Endpoint.ME_V3_EVENT_HOST)
        }
        ServiceEndpointProvider(getClientServiceStorage(), Endpoint.ME_V3_CLIENT_HOST).also {
            addDependency(dependencies, it, Endpoint.ME_V3_CLIENT_HOST)
        }
        ServiceEndpointProvider(getInboxServiceStorage(), Endpoint.INBOX_BASE).also {
            addDependency(dependencies, it, Endpoint.INBOX_BASE)
        }
        ServiceEndpointProvider(getMessageInboxServiceStorage(), Endpoint.ME_V3_INBOX_HOST).also {
            addDependency(dependencies, it, Endpoint.ME_V3_INBOX_HOST)
        }
        ServiceEndpointProvider(getMobileEngageV2ServiceStorage(), Endpoint.ME_BASE_V2).also {
            addDependency(dependencies, it, Endpoint.ME_BASE_V2)
        }
        ServiceEndpointProvider(getDeepLinkServiceStorage(), Endpoint.DEEP_LINK).also {
            addDependency(dependencies, it, Endpoint.DEEP_LINK)
        }
        ServiceEndpointProvider(getPredictServiceStorage(), com.emarsys.predict.endpoint.Endpoint.PREDICT_BASE_URL).also {
            addDependency(dependencies, it, com.emarsys.predict.endpoint.Endpoint.PREDICT_BASE_URL)
        }
        ResponseHandlersProcessor(ArrayList()).also {
            addDependency(dependencies, it)
        }
        val hardwareIdStorage: Storage<String> = StringStorage(CoreStorageKey.HARDWARE_ID, prefs).also {
            addDependency(dependencies, it, CoreStorageKey.HARDWARE_ID.key)
        }
        val languageProvider = LanguageProvider()
        val fii = FirebaseInstanceId.getInstance()
        val hardwareIdProvider = HardwareIdProvider(application, fii, hardwareIdStorage)
        val versionProvider = VersionProvider()
        val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationManagerCompat = NotificationManagerCompat.from(application)
        val notificationManagerProxy = NotificationManagerProxy(notificationManager, notificationManagerCompat)
        val notificationSettings: NotificationSettings = NotificationManagerHelper(notificationManagerProxy)
        DeviceInfo(application, hardwareIdProvider, versionProvider, languageProvider, notificationSettings, config.isAutomaticPushTokenSendingEnabled).also {
            addDependency(dependencies, it)
        }
        CurrentActivityProvider().also {
            addDependency(dependencies, it)
        }
        CurrentActivityWatchdog(getCurrentActivityProvider()).also {
            addDependency(dependencies, it)
        }
        val coreDbHelper = CoreDbHelper(application, HashMap())
        coreDbHelper.writableCoreDatabase.also {
            addDependency(dependencies, it)
        }
        addDependency(dependencies, ButtonClickedRepository(coreDbHelper) as Repository<ButtonClicked, SqlSpecification>, "buttonClickedRepository")
        DisplayedIamRepository(coreDbHelper).also { addDependency(dependencies, it as Repository<DisplayedIam, SqlSpecification>, "displayedIamRepository") }
        MobileEngageRequestContext(
                config.mobileEngageApplicationCode,
                config.contactFieldId,
                getDeviceInfo(),
                getTimestampProvider(),
                getUuidProvider(),
                getClientStateStorage(),
                getContactTokenStorage(),
                getDependency(MobileEngageStorageKey.REFRESH_TOKEN.key),
                getContactFieldValueStorage(),
                getPushTokenStorage()).also {
            addDependency(dependencies, it)
        }
        val inAppEventHandler = InAppEventHandlerInternal()
        val requestModelRepository = createRequestModelRepository(coreDbHelper, inAppEventHandler)
        val shardModelRepository = ShardModelRepository(coreDbHelper).also {
            addDependency(dependencies, it as Repository<ShardModel, SqlSpecification>, "shardModelRepository")
        }
        RestClient(ConnectionProvider(), getTimestampProvider(), getResponseHandlersProcessor(), createRequestModelMappers()).also {
            addDependency(dependencies, it)
        }
        val requestModelFactory = MobileEngageRequestModelFactory(getRequestContext(), getClientServiceProvider(), getEventServiceProvider(), getMobileEngageV2ServiceProvider(), getInboxServiceProvider(), getMessageInboxServiceProvider())
                .also { addDependency(dependencies, it) }
        val emarsysRequestModelFactory = EmarsysRequestModelFactory(getRequestContext())
        val contactTokenResponseHandler = MobileEngageTokenResponseHandler("contactToken", getContactTokenStorage(), getClientServiceProvider(), getEventServiceProvider(), getMessageInboxServiceProvider()).also {
            addDependency(dependencies, it, "contactTokenResponseHandler")
        }
        NotificationCache().also {
            addDependency(dependencies, it)
        }
        MobileEngageRefreshTokenInternal(
                contactTokenResponseHandler,
                getRestClient(),
                requestModelFactory).also {
            addDependency(dependencies, it as RefreshTokenInternal)
        }
        val connectionWatchDog = ConnectionWatchDog(application, getCoreSdkHandler()).also {
            addDependency(dependencies, it)
        }
        val coreCompletionHandlerMiddlewareProvider = CoreCompletionHandlerMiddlewareProvider(
                requestModelRepository,
                uiHandler,
                getCoreSdkHandler()
        )
        val coreCompletionHandlerRefreshTokenProxyProvider = CoreCompletionHandlerRefreshTokenProxyProvider(
                coreCompletionHandlerMiddlewareProvider,
                getRefreshTokenInternal(),
                getRestClient(),
                getContactTokenStorage(),
                getPushTokenStorage(),
                getClientServiceProvider(),
                getEventServiceProvider(),
                getMessageInboxServiceProvider(),
                getCoreCompletionHandler())
        val worker: Worker = DefaultWorker(
                requestModelRepository,
                connectionWatchDog,
                uiHandler,
                getCoreCompletionHandler(),
                getRestClient(),
                coreCompletionHandlerRefreshTokenProxyProvider)
        val requestManager = RequestManager(
                getCoreSdkHandler(),
                requestModelRepository,
                getShardRepository(),
                worker,
                getRestClient(),
                getCoreCompletionHandler(),
                getCoreCompletionHandler(),
                coreCompletionHandlerRefreshTokenProxyProvider
        ).also { addDependency(dependencies, it) }

        requestManager.setDefaultHeaders(RequestHeaderUtils.createDefaultHeaders(getRequestContext()))
        val sharedPrefsKeyStore = DefaultKeyValueStore(prefs).also { addDependency(dependencies, it as KeyValueStore) }
        var notificationEventHandler: EventHandler? = null
        if (config.notificationEventHandler != null) {
            notificationEventHandler = object : EventHandler {
                override fun handleEvent(context: Context, eventName: String, payload: JSONObject?) {
                    config.notificationEventHandler.handleEvent(context, eventName, payload)
                }
            }
        }
        EventHandlerProvider(notificationEventHandler).also {
            addDependency(dependencies, it, "notificationEventHandlerProvider")
        }
        EventHandlerProvider(null).also {
            addDependency(dependencies, it, "silentMessageEventHandlerProvider")
        }
        EventHandlerProvider(null).also {
            addDependency(dependencies, it, "geofenceEventHandlerProvider")
        }
        NotificationInformationListenerProvider(null).also {
            addDependency(dependencies, it, "notificationInformationListenerProvider")
        }
        SilentNotificationInformationListenerProvider(null).also {
            addDependency(dependencies, it, "silentNotificationInformationListenerProvider")
        }
        BatchingShardTrigger(
                getShardRepository(),
                ListSizeAtLeast(10),
                FilterByShardType(FilterByShardType.SHARD_TYPE_LOG),
                ListChunker(10),
                LogShardListMerger(getTimestampProvider(), getUuidProvider(), getDeviceInfo(), config.mobileEngageApplicationCode, config.predictMerchantId),
                requestManager,
                BatchingShardTrigger.RequestStrategy.TRANSIENT,
                connectionWatchDog
        ).also {
            addDependency(dependencies, it as Runnable, "logShardTrigger")
        }
        val predictRequestContext = PredictRequestContext(config.predictMerchantId, getDeviceInfo(), getTimestampProvider(), getUuidProvider(), sharedPrefsKeyStore)
        val headerFactory = PredictHeaderFactory(predictRequestContext)
        val predictRequestModelBuilderProvider = PredictRequestModelBuilderProvider(predictRequestContext, headerFactory, getPredictServiceProvider())
        val predictResponseMapper = PredictResponseMapper()
        BatchingShardTrigger(
                shardModelRepository,
                ListSizeAtLeast(1),
                FilterByShardType(FilterByShardType.SHARD_TYPE_PREDICT),
                ListChunker(1),
                PredictShardListMerger(predictRequestContext, predictRequestModelBuilderProvider),
                requestManager,
                BatchingShardTrigger.RequestStrategy.PERSISTENT,
                connectionWatchDog
        ).also {
            addDependency(dependencies, it as Runnable, "predictShardTrigger")
        }
        DefaultPredictInternal(predictRequestContext, requestManager, predictRequestModelBuilderProvider, predictResponseMapper).also {
            addDependency(dependencies, it as PredictInternal, "defaultInstance")
        }
        LoggingPredictInternal(Emarsys.Predict::class.java).also {
            addDependency(dependencies, it as PredictInternal, "loggingInstance")
        }
        val geofenceFilter = GeofenceFilter(GEOFENCE_LIMIT)
        val geofencingClient = GeofencingClient(application)
        DefaultMessageInboxInternal(requestManager, getRequestContext(), requestModelFactory, uiHandler, MessageInboxResponseMapper()).also {
            addDependency(dependencies, it as MessageInboxInternal, "defaultInstance")
        }
        DefaultMobileEngageInternal(requestManager, requestModelFactory, getRequestContext()).also {
            addDependency(dependencies, it as MobileEngageInternal, "defaultInstance")
        }
        DefaultEventServiceInternal(requestManager, requestModelFactory).also {
            addDependency(dependencies, it as EventServiceInternal, "defaultInstance")
        }
        ActionCommandFactory(application.applicationContext, getEventServiceInternal(), getSilentMessageEventHandlerProvider()).also {
            addDependency(dependencies, it, "silentMessageActionCommandFactory")
        }
        val geofenceActionCommandFactory = ActionCommandFactory(application.applicationContext, getEventServiceInternal(), getGeofenceEventHandlerProvider())
        DefaultGeofenceInternal(requestModelFactory,
                requestManager,
                GeofenceResponseMapper(),
                PermissionChecker(application.applicationContext),
                FusedLocationProviderClient(application.applicationContext),
                geofenceFilter,
                geofencingClient,
                application,
                geofenceActionCommandFactory,
                getGeofenceEventHandlerProvider(),
                geofenceEnabledStorage,
                GeofencePendingIntentProvider(application.applicationContext)
        ).also {
            addDependency(dependencies, it as GeofenceInternal, "defaultInstance")
        }
        DefaultClientServiceInternal(requestManager, requestModelFactory).also {
            addDependency(dependencies, it as ClientServiceInternal, "defaultInstance")
        }
        DefaultDeepLinkInternal(requestManager, getRequestContext(), getDeepLinkServiceProvider()).also {
            addDependency(dependencies, it as DeepLinkInternal, "defaultInstance")
        }
        DefaultPushInternal(requestManager, uiHandler, requestModelFactory, getEventServiceInternal(), getPushTokenStorage(),
                getNotificationEventHandlerProvider(), getSilentMessageEventHandlerProvider(), getNotificationInformationListenerProvider(), getSilentNotificationInformationListenerProvider()).also {
            addDependency(dependencies, it as PushInternal, "defaultInstance")
        }
        DefaultInAppInternal(inAppEventHandler, getEventServiceInternal()).also {
            addDependency(dependencies, it as InAppInternal, "defaultInstance")
        }
        DefaultInboxInternal(requestManager, getRequestContext(), requestModelFactory).also {
            addDependency(dependencies, it as InboxInternal, "defaultInstance")
        }
        LoggingMobileEngageInternal(Emarsys::class.java).also {
            addDependency(dependencies, it as MobileEngageInternal, "loggingInstance")
        }
        LoggingDeepLinkInternal(Emarsys::class.java).also {
            addDependency(dependencies, it as DeepLinkInternal, "loggingInstance")
        }
        LoggingPushInternal(Emarsys.Push::class.java).also {
            addDependency(dependencies, it as PushInternal, "loggingInstance")
        }
        LoggingClientServiceInternal(Emarsys::class.java).also {
            addDependency(dependencies, it as ClientServiceInternal, "loggingInstance")
        }
        LoggingEventServiceInternal(Emarsys::class.java).also {
            addDependency(dependencies, it as EventServiceInternal, "loggingInstance")
        }
        LoggingGeofenceInternal(Emarsys::class.java).also {
            addDependency(dependencies, it as GeofenceInternal, "loggingInstance")
        }
        LoggingInAppInternal(Emarsys.InApp::class.java).also {
            addDependency(dependencies, it as InAppInternal, "loggingInstance")
        }
        LoggingInboxInternal(Emarsys::class.java).also { addDependency(dependencies, it as InboxInternal, "loggingInstance") }

        LoggingMessageInboxInternal(Emarsys::class.java).also {
            addDependency(dependencies, it, "loggingInstance")
        }

        DefaultConfigInternal(
                getRequestContext(),
                getMobileEngageInternal(),
                getPushInternal(),
                getPushTokenProvider(),
                predictRequestContext,
                getDeviceInfo(),
                requestManager,
                emarsysRequestModelFactory,
                RemoteConfigResponseMapper(RandomProvider(), hardwareIdProvider),
                getClientServiceStorage(),
                getEventServiceStorage(),
                getDeepLinkServiceStorage(),
                getInboxServiceStorage(),
                getMobileEngageV2ServiceStorage(),
                getPredictServiceStorage(),
                getMessageInboxServiceStorage(),
                getLogLevelStorage(),
                Crypto(createPublicKey()),
                getClientServiceInternal()).also {
            addDependency(dependencies, it as ConfigInternal)
        }

        Logger(getCoreSdkHandler(), getShardRepository(), getTimestampProvider(), getUuidProvider(), getLogLevelStorage()).also {
            addDependency(dependencies, it)
        }
        FileDownloader(application.applicationContext).also {
            addDependency(dependencies, it)
        }
        ActionCommandFactory(application.applicationContext, getEventServiceInternal(), getNotificationEventHandlerProvider()).also {
            addDependency(dependencies, it, "notificationActionCommandFactory")
        }
        val webViewProvider = WebViewProvider(application.applicationContext).also {
            addDependency(dependencies, it)
        }
        IamJsBridgeFactory(getUiHandler()).also {
            addDependency(dependencies, it)
        }
        InlineInAppWebViewFactory(webViewProvider).also {
            addDependency(dependencies, it)
        }
    }

    private fun createRequestModelRepository(coreDbHelper: CoreDbHelper, inAppEventHandler: InAppEventHandlerInternal): Repository<RequestModel, SqlSpecification> {
        val requestModelRepository = RequestModelRepository(coreDbHelper)
        return RequestRepositoryProxy(
                requestModelRepository,
                getDependency(dependencies, "displayedIamRepository"),
                getDependency(dependencies, "buttonClickedRepository"),
                getTimestampProvider(),
                getUuidProvider(),
                inAppEventHandler,
                getEventServiceProvider())
    }

    private fun createRequestModelMappers(): List<Mapper<RequestModel, RequestModel>> {
        val mappers: MutableList<Mapper<RequestModel, RequestModel>> = ArrayList()
        mappers.add(MobileEngageHeaderMapper(getRequestContext(), getClientServiceProvider(), getEventServiceProvider(), getMessageInboxServiceProvider()))
        return mappers
    }

    private fun initializeActivityLifecycleWatchdog() {
        val applicationStartActions = arrayOf<ActivityLifecycleAction>(
                DeviceInfoStartAction(getClientServiceInternal(), getDeviceInfoPayloadStorage(), getDeviceInfo())
        )
        val activityCreatedActions = arrayOf<ActivityLifecycleAction>(
                DeepLinkAction(getDeepLinkInternal())
        )
        val initializationActions = arrayOf<ActivityLifecycleAction?>(
                FetchGeofencesAction(getGeofenceInternal()),
                FetchRemoteConfigAction(getConfigInternal()),
                InAppStartAction(getEventServiceInternal(), getContactTokenStorage())
        )

        ActivityLifecycleWatchdog(
                applicationStartActions,
                activityCreatedActions,
                initializationActions).also {
            addDependency(dependencies, it)
        }
    }

    private fun initializeInAppPresenter(application: Application) {
        IamJsBridgeFactory(
                getUiHandler()
        ).also {
            addDependency(dependencies, it)
        }
        OverlayInAppPresenter(
                getCoreSdkHandler(),
                getUiHandler(),
                IamStaticWebViewProvider(application),
                getInAppInternal(),
                IamDialogProvider(),
                getDependency(dependencies, "buttonClickedRepository"),
                getDependency(dependencies, "displayedIamRepository"),
                getTimestampProvider(),
                getCurrentActivityProvider(),
                getIamJsBridgeFactory()

        ).also {
            addDependency(dependencies, it)
        }
    }

    private fun initializeResponseHandlers() {
        val responseHandlers: MutableList<AbstractResponseHandler?> = ArrayList()
        responseHandlers.add(VisitorIdResponseHandler(getDependency(dependencies), getPredictServiceProvider()))
        responseHandlers.add(XPResponseHandler(getDependency(dependencies), getPredictServiceProvider()))
        responseHandlers.add(MobileEngageTokenResponseHandler("refreshToken", getRefreshContactTokenStorage(), getClientServiceProvider(), getEventServiceProvider(), getMessageInboxServiceProvider()))
        responseHandlers.add(getDependency<MobileEngageTokenResponseHandler>("contactTokenResponseHandler"))
        responseHandlers.add(MobileEngageClientStateResponseHandler(getClientStateStorage(), getClientServiceProvider(), getEventServiceProvider(), getMessageInboxServiceProvider()))
        responseHandlers.add(ClientInfoResponseHandler(getDeviceInfo(), getDeviceInfoPayloadStorage()))
        responseHandlers.add(InAppMessageResponseHandler(getOverlayInAppPresenter()))
        responseHandlers.add(InAppCleanUpResponseHandler(
                getDependency(dependencies, "displayedIamRepository"),
                getDependency(dependencies, "buttonClickedRepository"),
                getEventServiceProvider()
        ))
        getResponseHandlersProcessor().addResponseHandlers(responseHandlers)
    }

    override fun getCoreCompletionHandler(): DefaultCoreCompletionHandler {
        if (getDependency<DefaultCoreCompletionHandler?>(dependencies) == null) {
            DefaultCoreCompletionHandler(HashMap()).also {
                addDependency(dependencies, it)
            }
        }
        return getDependency(dependencies)
    }

    override fun getInbox(): InboxApi = getDependency(dependencies, "defaultInstance")

    override fun getLoggingInbox(): InboxApi = getDependency(dependencies, "loggingInstance")

    override fun getMessageInbox(): MessageInboxApi = getDependency(dependencies, "defaultInstance")

    override fun getLoggingMessageInbox(): MessageInboxApi = getDependency(dependencies, "loggingInstance")

    override fun getInApp(): InAppApi = getDependency(dependencies, "defaultInstance")

    override fun getLoggingInApp(): InAppApi = getDependency(dependencies, "loggingInstance")

    override fun getPush(): PushApi = getDependency(dependencies, "defaultInstance")

    override fun getLoggingPush(): PushApi = getDependency(dependencies, "loggingInstance")

    override fun getPredict(): PredictApi = getDependency(dependencies, "defaultInstance")

    override fun getLoggingPredict(): PredictApi = getDependency(dependencies, "loggingInstance")

    override fun getConfig(): ConfigApi = getDependency(dependencies)

    override fun getGeofence(): GeofenceApi = getDependency(dependencies, "defaultInstance")

    override fun getLoggingGeofence(): GeofenceApi = getDependency(dependencies, "loggingInstance")

    override fun getConfigInternal(): ConfigInternal = getDependency(dependencies)

    override fun getMobileEngageInternal(): MobileEngageInternal = getDependency(dependencies, "defaultInstance")

    override fun getLoggingMobileEngageInternal(): MobileEngageInternal = getDependency(dependencies, "loggingInstance")

    override fun getClientServiceInternal(): ClientServiceInternal = getDependency(dependencies, "defaultInstance")

    override fun getLoggingClientServiceInternal(): ClientServiceInternal = getDependency(dependencies, "loggingInstance")

    override fun getInboxInternal(): InboxInternal = getDependency(dependencies, "defaultInstance")

    override fun getLoggingInboxInternal(): InboxInternal = getDependency(dependencies, "loggingInstance")

    override fun getMessageInboxInternal(): MessageInboxInternal = getDependency(dependencies, "defaultInstance")

    override fun getLoggingMessageInboxInternal(): MessageInboxInternal = getDependency(dependencies, "loggingInstance")

    override fun getInAppInternal(): InAppInternal = getDependency(dependencies, "defaultInstance")

    override fun getLoggingInAppInternal(): InAppInternal = getDependency(dependencies, "loggingInstance")

    override fun getDeepLinkInternal(): DeepLinkInternal = getDependency(dependencies, "defaultInstance")

    override fun getLoggingDeepLinkInternal(): DeepLinkInternal = getDependency(dependencies, "loggingInstance")

    override fun getPushInternal(): PushInternal = getDependency(dependencies, "defaultInstance")

    override fun getLoggingPushInternal(): PushInternal = getDependency(dependencies, "loggingInstance")

    override fun getEventServiceInternal(): EventServiceInternal = getDependency(dependencies, "defaultInstance")

    override fun getLoggingEventServiceInternal(): EventServiceInternal = getDependency(dependencies, "loggingInstance")

    override fun getRefreshTokenInternal(): RefreshTokenInternal = getDependency(dependencies)

    override fun getRequestContext(): MobileEngageRequestContext = getDependency(dependencies)

    override fun getOverlayInAppPresenter(): OverlayInAppPresenter = getDependency(dependencies)

    override fun getDeviceInfoPayloadStorage(): StringStorage = getDependency(dependencies, MobileEngageStorageKey.DEVICE_INFO_HASH.key)

    override fun getContactFieldValueStorage(): StringStorage = getDependency(dependencies, MobileEngageStorageKey.CONTACT_FIELD_VALUE.key)

    override fun getContactTokenStorage(): StringStorage = getDependency(dependencies, MobileEngageStorageKey.CONTACT_TOKEN.key)

    override fun getClientStateStorage(): StringStorage = getDependency(dependencies, MobileEngageStorageKey.CLIENT_STATE.key)

    override fun getPushTokenStorage(): StringStorage = getDependency(dependencies, MobileEngageStorageKey.PUSH_TOKEN.key)

    override fun getRefreshContactTokenStorage(): StringStorage = getDependency(dependencies, MobileEngageStorageKey.REFRESH_TOKEN.key)

    override fun getLogLevelStorage(): StringStorage = getDependency(dependencies, CoreStorageKey.LOG_LEVEL.key)

    override fun getResponseHandlersProcessor(): ResponseHandlersProcessor = getDependency(dependencies)

    override fun getNotificationCache(): NotificationCache = getDependency(dependencies)

    override fun getPushTokenProvider(): PushTokenProvider = getDependency(dependencies)

    override fun getClientServiceProvider(): ServiceEndpointProvider = getDependency(dependencies, Endpoint.ME_V3_CLIENT_HOST)

    override fun getEventServiceProvider(): ServiceEndpointProvider = getDependency(dependencies, Endpoint.ME_V3_EVENT_HOST)

    override fun getDeepLinkServiceProvider(): ServiceEndpointProvider = getDependency(dependencies, Endpoint.DEEP_LINK)

    override fun getInboxServiceProvider(): ServiceEndpointProvider = getDependency(dependencies, Endpoint.INBOX_BASE)

    override fun getMessageInboxServiceProvider(): ServiceEndpointProvider = getDependency(dependencies, Endpoint.ME_V3_INBOX_HOST)

    override fun getMobileEngageV2ServiceProvider(): ServiceEndpointProvider = getDependency(dependencies, Endpoint.ME_BASE_V2)

    override fun getNotificationInformationListenerProvider(): NotificationInformationListenerProvider = getDependency(dependencies, "notificationInformationListenerProvider")

    override fun getSilentNotificationInformationListenerProvider(): SilentNotificationInformationListenerProvider = getDependency(dependencies, "silentNotificationInformationListenerProvider")

    override fun getClientServiceStorage(): StringStorage = getDependency(dependencies, MobileEngageStorageKey.CLIENT_SERVICE_URL.key)

    override fun getEventServiceStorage(): StringStorage = getDependency(dependencies, MobileEngageStorageKey.EVENT_SERVICE_URL.key)

    override fun getDeepLinkServiceStorage(): StringStorage = getDependency(dependencies, MobileEngageStorageKey.DEEPLINK_SERVICE_URL.key)

    override fun getInboxServiceStorage(): StringStorage = getDependency(dependencies, MobileEngageStorageKey.INBOX_SERVICE_URL.key)

    override fun getMessageInboxServiceStorage(): StringStorage = getDependency(dependencies, MobileEngageStorageKey.MESSAGE_INBOX_SERVICE_URL.key)

    override fun getMobileEngageV2ServiceStorage(): StringStorage = getDependency(dependencies, MobileEngageStorageKey.ME_V2_SERVICE_URL.key)

    override fun getNotificationActionCommandFactory(): ActionCommandFactory = getDependency(dependencies, "notificationActionCommandFactory")

    override fun getSilentMessageActionCommandFactory(): ActionCommandFactory = getDependency(dependencies, "silentMessageActionCommandFactory")

    override fun getNotificationEventHandlerProvider(): EventHandlerProvider = getDependency(dependencies, "notificationEventHandlerProvider")

    override fun getSilentMessageEventHandlerProvider(): EventHandlerProvider = getDependency(dependencies, "silentMessageEventHandlerProvider")

    override fun getGeofenceEventHandlerProvider(): EventHandlerProvider = getDependency(dependencies, "geofenceEventHandlerProvider")

    override fun getCurrentActivityProvider(): CurrentActivityProvider = getDependency(dependencies)

    override fun getGeofenceInternal(): GeofenceInternal = getDependency(dependencies, "defaultInstance")

    override fun getLoggingGeofenceInternal(): GeofenceInternal = getDependency(dependencies, "loggingInstance")

    override fun getCoreSdkHandler(): Handler = getDependency(dependencies, "coreSdkHandler")

    override fun getUiHandler(): Handler = getDependency(dependencies, "uiHandler")

    override fun getActivityLifecycleWatchdog(): ActivityLifecycleWatchdog = getDependency(dependencies)

    override fun getCurrentActivityWatchdog(): CurrentActivityWatchdog = getDependency(dependencies)

    override fun getCoreSQLiteDatabase(): CoreSQLiteDatabase = getDependency(dependencies)

    override fun getDeviceInfo(): DeviceInfo = getDependency(dependencies)

    override fun getShardRepository(): Repository<ShardModel, SqlSpecification> = getDependency(dependencies, "shardModelRepository")

    override fun getTimestampProvider(): TimestampProvider = getDependency(dependencies)

    override fun getUuidProvider(): UUIDProvider = getDependency(dependencies)

    override fun getLogShardTrigger(): Runnable = getDependency(dependencies, "logShardTrigger")

    override fun getLogger(): Logger = getDependency(dependencies)

    override fun getRestClient(): RestClient = getDependency(dependencies)

    override fun getFileDownloader(): FileDownloader = getDependency(dependencies)

    override fun getPredictInternal(): PredictInternal = getDependency(dependencies, "defaultInstance")

    override fun getLoggingPredictInternal(): PredictInternal = getDependency(dependencies, "loggingInstance")

    override fun getPredictShardTrigger(): Runnable = getDependency(dependencies, "predictShardTrigger")

    override fun getPredictServiceProvider(): ServiceEndpointProvider = getDependency(dependencies, com.emarsys.predict.endpoint.Endpoint.PREDICT_BASE_URL)

    override fun getPredictServiceStorage(): StringStorage = getDependency(dependencies, PredictStorageKey.PREDICT_SERVICE_URL.key)

    override fun getButtonClickedRepository(): Repository<ButtonClicked, SqlSpecification> = getDependency(dependencies, "buttonClickedRepository")

    override fun getDisplayedIamRepository(): Repository<DisplayedIam, SqlSpecification> = getDependency(dependencies, "displayedIamRepository")

    override fun getKeyValueStore(): KeyValueStore = getDependency(dependencies)

    override fun getContactTokenResponseHandler(): MobileEngageTokenResponseHandler = getDependency(dependencies, "contactTokenResponseHandler")

    override fun getWebViewProvider(): WebViewProvider = getDependency(dependencies)

    override fun getInlineInAppWebViewFactory(): InlineInAppWebViewFactory = getDependency(dependencies)

    override fun getIamJsBridgeFactory(): IamJsBridgeFactory = getDependency(dependencies)

    private fun createPublicKey(): PublicKey {
        val publicKeySpec = X509EncodedKeySpec(
                Base64.decode(PUBLIC_KEY, 0)
        )
        val keyFactory = KeyFactory.getInstance("EC")
        return keyFactory.generatePublic(publicKeySpec)
    }
}