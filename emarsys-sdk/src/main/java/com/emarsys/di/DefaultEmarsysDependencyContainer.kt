package com.emarsys.di

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.location.LocationManager
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
import com.emarsys.core.di.Container
import com.emarsys.core.di.Container.addDependency
import com.emarsys.core.di.Container.getDependency
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.feature.FeatureRegistry
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
import com.emarsys.feature.InnerFeature
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
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClickedRepository
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIamRepository
import com.emarsys.mobileengage.iam.model.requestRepositoryProxy.RequestRepositoryProxy
import com.emarsys.mobileengage.iam.webview.IamWebViewProvider
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
import com.google.android.gms.location.GeofencingClient
import org.json.JSONObject
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*

open class DefaultEmarsysDependencyContainer(emarsysConfig: EmarsysConfig, onCompleted: (() -> Unit)? = null) : EmarsysDependencyContainer {

    companion object {
        private const val EMARSYS_SHARED_PREFERENCES_NAME = "emarsys_shared_preferences"
        private const val GEOFENCE_LIMIT = 100
        private const val PUBLIC_KEY = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAELjWEUIBX9zlm1OI4gF1hMCBLzpaBwgs9HlmSIBAqP4MDGy4ibOOV3FVDrnAY0Q34LZTbPBlp3gRNZJ19UoSy2Q=="
    }

    init {
        initializeFeatures(emarsysConfig)
        initializeDependencies(emarsysConfig, onCompleted)
    }

    private fun initializeFeatures(emarsysConfig: EmarsysConfig) {
        if (emarsysConfig.mobileEngageApplicationCode != null) {
            FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)
        }
        if (emarsysConfig.predictMerchantId != null) {
            FeatureRegistry.enableFeature(InnerFeature.PREDICT)
        }
    }

    private fun initializeDependencies(config: EmarsysConfig, onCompleted: (() -> Unit)?) {
        val application = config.application
        if (Container.dependencies.isEmpty()) {
            val coreSdkHandler: Handler = CoreSdkHandlerProvider().provideHandler()
            addDependency(coreSdkHandler, "coreSdkHandler")

            addDependency((InApp() as InAppApi).proxyApi(coreSdkHandler), "defaultInstance")

            addDependency((InApp(true) as InAppApi).proxyApi(coreSdkHandler), "loggingInstance")

            addDependency((Push() as PushApi).proxyApi(coreSdkHandler), "defaultInstance")

            addDependency((Push(true) as PushApi).proxyApi(coreSdkHandler), "loggingInstance")

            addDependency((Predict() as PredictApi).proxyApi(coreSdkHandler), "defaultInstance")

            addDependency((Predict(true) as PredictApi).proxyApi(coreSdkHandler), "loggingInstance")

            addDependency((Config() as ConfigApi).proxyApi(coreSdkHandler))

            addDependency((Geofence() as GeofenceApi).proxyApi(coreSdkHandler), "defaultInstance")

            addDependency((Geofence(true) as GeofenceApi).proxyApi(coreSdkHandler), "loggingInstance")

            addDependency((Inbox() as InboxApi).proxyApi(coreSdkHandler), "defaultInstance")

            addDependency((Inbox(true) as InboxApi).proxyApi(coreSdkHandler), "loggingInstance")

            addDependency((MessageInbox() as MessageInboxApi).proxyApi(coreSdkHandler), "defaultInstance")

            addDependency((MessageInbox(true) as MessageInboxApi).proxyApi(coreSdkHandler), "loggingInstance")
            coreSdkHandler.post {
                initializeDependenciesInBackground(application, config)
            }

        }
        getDependency<Handler>("coreSdkHandler")
                .post {
                    initializeInAppPresenter(application)
                    initializeResponseHandlers()
                    initializeActivityLifecycleWatchdog()

                    onCompleted?.invoke()
                }

    }

    private fun initializeDependenciesInBackground(application: Application, config: EmarsysConfig) {
        val prefs = application.getSharedPreferences(EMARSYS_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val uiHandler = Handler(Looper.getMainLooper())

        TimestampProvider().also {
            addDependency(it)
        }
        UUIDProvider().also {
            addDependency(it)
        }
        StringStorage(MobileEngageStorageKey.DEVICE_INFO_HASH, prefs).also {
            addDependency(it, MobileEngageStorageKey.DEVICE_INFO_HASH.key)
        }
        StringStorage(MobileEngageStorageKey.CONTACT_TOKEN, prefs).also {
            addDependency(it, MobileEngageStorageKey.CONTACT_TOKEN.key)
        }
        StringStorage(MobileEngageStorageKey.REFRESH_TOKEN, prefs).also {
            addDependency(it, MobileEngageStorageKey.REFRESH_TOKEN.key)
        }
        StringStorage(MobileEngageStorageKey.CLIENT_STATE, prefs).also {
            addDependency(it, MobileEngageStorageKey.CLIENT_STATE.key)
        }
        StringStorage(MobileEngageStorageKey.CONTACT_FIELD_VALUE, prefs).also {
            addDependency(it, MobileEngageStorageKey.CONTACT_FIELD_VALUE.key)
        }
        val geofenceEnabledStorage = BooleanStorage(MobileEngageStorageKey.GEOFENCE_ENABLED, prefs)

        addDependency(StringStorage(MobileEngageStorageKey.PUSH_TOKEN, prefs), MobileEngageStorageKey.PUSH_TOKEN.key)
        StringStorage(CoreStorageKey.LOG_LEVEL, prefs).also {
            addDependency(it, CoreStorageKey.LOG_LEVEL.key)
        }
        DefaultPushTokenProvider(getPushTokenStorage()).also {
            addDependency(it)
        }
        StringStorage(MobileEngageStorageKey.EVENT_SERVICE_URL, prefs).also {
            addDependency(it, MobileEngageStorageKey.EVENT_SERVICE_URL.key)
        }
        StringStorage(MobileEngageStorageKey.CLIENT_SERVICE_URL, prefs).also {
            addDependency(it, MobileEngageStorageKey.CLIENT_SERVICE_URL.key)
        }
        StringStorage(MobileEngageStorageKey.INBOX_SERVICE_URL, prefs).also {
            addDependency(it, MobileEngageStorageKey.INBOX_SERVICE_URL.key)
        }
        StringStorage(MobileEngageStorageKey.MESSAGE_INBOX_SERVICE_URL, prefs).also {
            addDependency(it, MobileEngageStorageKey.MESSAGE_INBOX_SERVICE_URL.key)
        }
        StringStorage(MobileEngageStorageKey.ME_V2_SERVICE_URL, prefs).also {
            addDependency(it, MobileEngageStorageKey.ME_V2_SERVICE_URL.key)
        }
        StringStorage(MobileEngageStorageKey.DEEPLINK_SERVICE_URL, prefs).also {
            addDependency(it, MobileEngageStorageKey.DEEPLINK_SERVICE_URL.key)
        }
        StringStorage(PredictStorageKey.PREDICT_SERVICE_URL, prefs).also {
            addDependency(it, PredictStorageKey.PREDICT_SERVICE_URL.key)
        }
        ServiceEndpointProvider(getEventServiceStorage(), Endpoint.ME_V3_EVENT_HOST).also {
            addDependency(it, Endpoint.ME_V3_EVENT_HOST)
        }
        ServiceEndpointProvider(getClientServiceStorage(), Endpoint.ME_V3_CLIENT_HOST).also {
            addDependency(it, Endpoint.ME_V3_CLIENT_HOST)
        }
        ServiceEndpointProvider(getInboxServiceStorage(), Endpoint.INBOX_BASE).also {
            addDependency(it, Endpoint.INBOX_BASE)
        }
        ServiceEndpointProvider(getMessageInboxServiceStorage(), Endpoint.ME_V3_INBOX_HOST).also {
            addDependency(it, Endpoint.ME_V3_INBOX_HOST)
        }
        ServiceEndpointProvider(getMobileEngageV2ServiceStorage(), Endpoint.ME_BASE_V2).also {
            addDependency(it, Endpoint.ME_BASE_V2)
        }
        ServiceEndpointProvider(getDeepLinkServiceStorage(), Endpoint.DEEP_LINK).also {
            addDependency(it, Endpoint.DEEP_LINK)
        }
        ServiceEndpointProvider(getPredictServiceStorage(), com.emarsys.predict.endpoint.Endpoint.PREDICT_BASE_URL).also {
            addDependency(it, com.emarsys.predict.endpoint.Endpoint.PREDICT_BASE_URL)
        }
        ResponseHandlersProcessor(ArrayList()).also {
            addDependency(it)
        }
        val hardwareIdStorage: Storage<String> = StringStorage(CoreStorageKey.HARDWARE_ID, prefs).also {
            addDependency(it, CoreStorageKey.HARDWARE_ID.key)
        }
        val languageProvider = LanguageProvider()
        val hardwareIdProvider = HardwareIdProvider(application, hardwareIdStorage)
        val versionProvider = VersionProvider()
        val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationManagerCompat = NotificationManagerCompat.from(application)
        val notificationManagerProxy = NotificationManagerProxy(notificationManager, notificationManagerCompat)
        val notificationSettings: NotificationSettings = NotificationManagerHelper(notificationManagerProxy)
        DeviceInfo(application, hardwareIdProvider, versionProvider, languageProvider, notificationSettings, config.isAutomaticPushTokenSendingEnabled).also {
            addDependency(it)
        }
        CurrentActivityProvider().also {
            addDependency(it)
        }
        CurrentActivityWatchdog(getCurrentActivityProvider()).also {
            addDependency(it)
        }
        val coreDbHelper = CoreDbHelper(application, HashMap())
        coreDbHelper.writableCoreDatabase.also {
            addDependency(it)
        }
        addDependency(ButtonClickedRepository(coreDbHelper) as Repository<ButtonClicked, SqlSpecification>, "buttonClickedRepository")
        DisplayedIamRepository(coreDbHelper).also { addDependency(it as Repository<DisplayedIam, SqlSpecification>, "displayedIamRepository") }
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
            addDependency(it)
        }
        val inAppEventHandler = InAppEventHandlerInternal()
        val requestModelRepository = createRequestModelRepository(coreDbHelper, inAppEventHandler)
        val shardModelRepository = ShardModelRepository(coreDbHelper).also {
            addDependency(it)
        }
        RestClient(ConnectionProvider(), getTimestampProvider(), getResponseHandlersProcessor(), createRequestModelMappers()).also {
            addDependency(it)
        }
        val requestModelFactory = MobileEngageRequestModelFactory(getRequestContext(), getClientServiceProvider(), getEventServiceProvider(), getMobileEngageV2ServiceProvider(), getInboxServiceProvider(), getMessageInboxServiceProvider())
        val emarsysRequestModelFactory = EmarsysRequestModelFactory(getRequestContext())
        val contactTokenResponseHandler = MobileEngageTokenResponseHandler("contactToken", getContactTokenStorage(), getClientServiceProvider(), getEventServiceProvider(), getMessageInboxServiceProvider()).also {
            addDependency(it, "contactTokenResponseHandler")
        }
        NotificationCache().also {
            addDependency(it)
        }
        MobileEngageRefreshTokenInternal(
                contactTokenResponseHandler,
                getRestClient(),
                requestModelFactory).also {
            addDependency(it)
        }
        val connectionWatchDog = ConnectionWatchDog(application, getCoreSdkHandler()).also {
            addDependency(it)
        }
        val coreCompletionHandlerMiddlewareProvider = CoreCompletionHandlerMiddlewareProvider(
                getCoreCompletionHandler(),
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
                getMessageInboxServiceProvider())
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
                getCoreCompletionHandler())
        requestManager.setDefaultHeaders(RequestHeaderUtils.createDefaultHeaders(getRequestContext()))
        val sharedPrefsKeyStore = DefaultKeyValueStore(prefs).also { addDependency(it as KeyValueStore) }
        var notificationEventHandler: EventHandler? = null
        if (config.notificationEventHandler != null) {
            notificationEventHandler = object : EventHandler {
                override fun handleEvent(context: Context, eventName: String, payload: JSONObject?) {
                    config.notificationEventHandler.handleEvent(context, eventName, payload)
                }
            }
        }
        EventHandlerProvider(notificationEventHandler).also {
            addDependency(it, "notificationEventHandlerProvider")
        }
        EventHandlerProvider(null).also {
            addDependency(it, "silentMessageEventHandlerProvider")
        }
        EventHandlerProvider(null).also {
            addDependency(it, "geofenceEventHandlerProvider")
        }
        BatchingShardTrigger(
                getShardRepository(),
                ListSizeAtLeast(10),
                FilterByShardType(FilterByShardType.SHARD_TYPE_LOG),
                ListChunker(10),
                LogShardListMerger(getTimestampProvider(), getUuidProvider(), getDeviceInfo(), config.mobileEngageApplicationCode, config.predictMerchantId),
                requestManager,
                BatchingShardTrigger.RequestStrategy.TRANSIENT).also {
            addDependency(it as Runnable, "logShardTrigger")
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
                BatchingShardTrigger.RequestStrategy.PERSISTENT).also {
            addDependency(it as Runnable, "predictShardTrigger")
        }
        DefaultPredictInternal(predictRequestContext, requestManager, predictRequestModelBuilderProvider, predictResponseMapper).also {
            addDependency(it as PredictInternal, "defaultInstance")
        }
        LoggingPredictInternal(Emarsys.Predict::class.java).also {
            addDependency(it as PredictInternal, "loggingInstance")
        }
        val locationManager = application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val geofenceFilter = GeofenceFilter(GEOFENCE_LIMIT)
        val geofencingClient = GeofencingClient(application)
        DefaultMessageInboxInternal(requestManager, getRequestContext(), requestModelFactory, uiHandler, MessageInboxResponseMapper()).also {
            addDependency(it as MessageInboxInternal, "defaultInstance")
        }
        DefaultMobileEngageInternal(requestManager, requestModelFactory, getRequestContext()).also {
            addDependency(it as MobileEngageInternal, "defaultInstance")
        }
        DefaultEventServiceInternal(requestManager, requestModelFactory).also {
            addDependency(it as EventServiceInternal, "defaultInstance")
        }
        ActionCommandFactory(application.applicationContext, getEventServiceInternal(), getSilentMessageEventHandlerProvider()).also {
            addDependency(it, "silentMessageActionCommandFactory")
        }
        val geofenceActionCommandFactory = ActionCommandFactory(application.applicationContext, getEventServiceInternal(), getGeofenceEventHandlerProvider())
        DefaultGeofenceInternal(requestModelFactory, requestManager, GeofenceResponseMapper(), PermissionChecker(application.applicationContext), locationManager, geofenceFilter, geofencingClient, application, geofenceActionCommandFactory, getGeofenceEventHandlerProvider(), geofenceEnabledStorage).also {
            addDependency(it as GeofenceInternal, "defaultInstance")
        }
        DefaultClientServiceInternal(requestManager, requestModelFactory).also {
            addDependency(it as ClientServiceInternal, "defaultInstance")
        }
        DefaultDeepLinkInternal(requestManager, getRequestContext(), getDeepLinkServiceProvider()).also {
            addDependency(it as DeepLinkInternal, "defaultInstance")
        }
        DefaultPushInternal(requestManager, uiHandler, requestModelFactory, getEventServiceInternal(), getPushTokenStorage(),
                getNotificationEventHandlerProvider(), getSilentMessageEventHandlerProvider()).also {
            addDependency(it as PushInternal, "defaultInstance")
        }
        DefaultInAppInternal(inAppEventHandler, getEventServiceInternal()).also {
            addDependency(it as InAppInternal, "defaultInstance")
        }
        DefaultInboxInternal(requestManager, getRequestContext(), requestModelFactory).also {
            addDependency(it as InboxInternal, "defaultInstance")
        }
        LoggingMobileEngageInternal(Emarsys::class.java).also {
            addDependency(it as MobileEngageInternal, "loggingInstance")
        }
        LoggingDeepLinkInternal(Emarsys::class.java).also {
            addDependency(it as DeepLinkInternal, "loggingInstance")
        }
        LoggingPushInternal(Emarsys.Push::class.java).also {
            addDependency(it as PushInternal, "loggingInstance")
        }
        LoggingClientServiceInternal(Emarsys::class.java).also {
            addDependency(it as ClientServiceInternal, "loggingInstance")
        }
        LoggingEventServiceInternal(Emarsys::class.java).also {
            addDependency(it as EventServiceInternal, "loggingInstance")
        }
        LoggingGeofenceInternal(Emarsys::class.java).also {
            addDependency(it as GeofenceInternal, "loggingInstance")
        }
        LoggingInAppInternal(Emarsys.InApp::class.java).also {
            addDependency(it as InAppInternal, "loggingInstance")
        }
        LoggingInboxInternal(Emarsys::class.java).also { addDependency(it as InboxInternal, "loggingInstance") }

        LoggingMessageInboxInternal(Emarsys::class.java).also {
            addDependency(it, "loggingInstance")
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
                RemoteConfigResponseMapper(RandomProvider()),
                getClientServiceStorage(),
                getEventServiceStorage(),
                getDeepLinkServiceStorage(),
                getInboxServiceStorage(),
                getMobileEngageV2ServiceStorage(),
                getPredictServiceStorage(),
                getMessageInboxServiceStorage(),
                getLogLevelStorage(),
                Crypto(createPublicKey())).also {
            addDependency(it as ConfigInternal)
        }

        Logger(getCoreSdkHandler(), getShardRepository(), getTimestampProvider(), getUuidProvider(), getLogLevelStorage()).also {
            addDependency(it)
        }
        FileDownloader(application.applicationContext).also {
            addDependency(it)
        }
        ActionCommandFactory(application.applicationContext, getEventServiceInternal(), getNotificationEventHandlerProvider()).also {
            addDependency(it, "notificationActionCommandFactory")
        }
    }

    private fun createRequestModelRepository(coreDbHelper: CoreDbHelper, inAppEventHandler: InAppEventHandlerInternal): Repository<RequestModel, SqlSpecification> {
        val requestModelRepository = RequestModelRepository(coreDbHelper)
        return RequestRepositoryProxy(
                requestModelRepository,
                getDependency("displayedIamRepository"),
                getDependency("buttonClickedRepository"),
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
        val applicationStartActions = arrayOf(
                DeviceInfoStartAction(getClientServiceInternal(), getDeviceInfoPayloadStorage(), getDeviceInfo()),
                InAppStartAction(getEventServiceInternal(), getContactTokenStorage())
        )
        val activityCreatedActions = arrayOf<ActivityLifecycleAction>(
                DeepLinkAction(getDeepLinkInternal())
        )
        val initializeActions = arrayOf<ActivityLifecycleAction?>(
                FetchGeofencesAction(getGeofenceInternal()),
                FetchRemoteConfigAction(getConfigInternal())
        )

        ActivityLifecycleWatchdog(
                applicationStartActions,
                activityCreatedActions,
                initializeActions).also {
            addDependency(it)
        }
    }

    private fun initializeInAppPresenter(application: Application) {
        InAppPresenter(
                getCoreSdkHandler(),
                IamWebViewProvider(application),
                getInAppInternal(),
                IamDialogProvider(),
                getDependency("buttonClickedRepository"),
                getDependency("displayedIamRepository"),
                getTimestampProvider(),
                getCurrentActivityProvider()).also {
            addDependency(it)
        }
    }

    private fun initializeResponseHandlers() {
        val responseHandlers: MutableList<AbstractResponseHandler?> = ArrayList()
        responseHandlers.add(VisitorIdResponseHandler(getDependency(), getPredictServiceProvider()))
        responseHandlers.add(XPResponseHandler(getDependency(), getPredictServiceProvider()))
        responseHandlers.add(MobileEngageTokenResponseHandler("refreshToken", getRefreshContactTokenStorage(), getClientServiceProvider(), getEventServiceProvider(), getMessageInboxServiceProvider()))
        responseHandlers.add(getDependency<MobileEngageTokenResponseHandler>("contactTokenResponseHandler"))
        responseHandlers.add(MobileEngageClientStateResponseHandler(getClientStateStorage(), getClientServiceProvider(), getEventServiceProvider(), getMessageInboxServiceProvider()))
        responseHandlers.add(ClientInfoResponseHandler(getDeviceInfo(), getDeviceInfoPayloadStorage()))
        responseHandlers.add(InAppMessageResponseHandler(getInAppPresenter()))
        responseHandlers.add(InAppCleanUpResponseHandler(
                getDependency("displayedIamRepository"),
                getDependency("buttonClickedRepository"),
                getEventServiceProvider()
        ))
        getResponseHandlersProcessor().addResponseHandlers(responseHandlers)
    }

    override fun getCoreCompletionHandler(): DefaultCoreCompletionHandler {
        if (getDependency<DefaultCoreCompletionHandler?>() == null) {
            DefaultCoreCompletionHandler(HashMap()).also {
                addDependency(it)
            }
        }
        return getDependency()
    }

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

    override fun getEventServiceInternal(): EventServiceInternal = getDependency("defaultInstance")

    override fun getLoggingEventServiceInternal(): EventServiceInternal = getDependency("loggingInstance")

    override fun getRefreshTokenInternal(): RefreshTokenInternal = getDependency<MobileEngageRefreshTokenInternal>()

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

    override fun getPushTokenProvider(): PushTokenProvider = getDependency<DefaultPushTokenProvider>()

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

    override fun getCoreSdkHandler(): Handler = getDependency("coreSdkHandler")

    override fun getActivityLifecycleWatchdog(): ActivityLifecycleWatchdog = getDependency()

    override fun getCurrentActivityWatchdog(): CurrentActivityWatchdog = getDependency()

    override fun getCoreSQLiteDatabase(): CoreSQLiteDatabase = getDependency()

    override fun getDeviceInfo(): DeviceInfo = getDependency()

    override fun getShardRepository(): Repository<ShardModel, SqlSpecification> = getDependency<ShardModelRepository>()

    override fun getTimestampProvider(): TimestampProvider = getDependency()

    override fun getUuidProvider(): UUIDProvider = getDependency()

    override fun getLogShardTrigger(): Runnable = getDependency("logShardTrigger")

    override fun getLogger(): Logger = getDependency()

    override fun getRestClient(): RestClient = getDependency()

    override fun getFileDownloader(): FileDownloader = getDependency()

    override fun getPredictInternal(): PredictInternal = getDependency("defaultInstance")

    override fun getLoggingPredictInternal(): PredictInternal = getDependency("loggingInstance")

    override fun getPredictShardTrigger(): Runnable = getDependency("predictShardTrigger")

    override fun getPredictServiceProvider(): ServiceEndpointProvider = getDependency(com.emarsys.predict.endpoint.Endpoint.PREDICT_BASE_URL)

    override fun getPredictServiceStorage(): StringStorage = getDependency(PredictStorageKey.PREDICT_SERVICE_URL.key)

    override fun getButtonClickedRepository(): Repository<ButtonClicked, SqlSpecification> = getDependency("buttonClickedRepository")

    override fun getDisplayedIamRepository(): Repository<DisplayedIam, SqlSpecification> = getDependency("displayedIamRepository")

    override fun getKeyValueStore(): KeyValueStore = getDependency()

    override fun getContactTokenResponseHandler(): MobileEngageTokenResponseHandler = getDependency("contactTokenResponseHandler")

    private fun createPublicKey(): PublicKey {
        val publicKeySpec = X509EncodedKeySpec(
                Base64.decode(PUBLIC_KEY, 0)
        )
        val keyFactory = KeyFactory.getInstance("EC")
        return keyFactory.generatePublic(publicKeySpec)
    }
}