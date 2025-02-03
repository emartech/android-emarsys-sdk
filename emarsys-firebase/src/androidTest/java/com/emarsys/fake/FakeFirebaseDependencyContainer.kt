package com.emarsys.fake

import android.app.Activity
import android.content.ClipboardManager
import android.content.SharedPreferences
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.activity.ActivityLifecycleActionRegistry
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.activity.TransitionSafeCurrentActivityWatchdog
import com.emarsys.core.app.AppLifecycleObserver
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.connection.ConnectionWatchDog
import com.emarsys.core.crypto.Crypto
import com.emarsys.core.database.CoreSQLiteDatabase
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.activity.CurrentActivityProvider
import com.emarsys.core.provider.clientid.ClientIdProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.RestClient
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseHandlersProcessor
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.storage.KeyValueStore
import com.emarsys.core.storage.Storage
import com.emarsys.core.util.FileDownloader
import com.emarsys.core.util.log.Logger
import com.emarsys.core.worker.Worker
import com.emarsys.mobileengage.MobileEngageInternal
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.client.ClientServiceInternal
import com.emarsys.mobileengage.deeplink.DeepLinkInternal
import com.emarsys.mobileengage.di.MobileEngageComponent
import com.emarsys.mobileengage.event.CacheableEventHandler
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.geofence.GeofenceInternal
import com.emarsys.mobileengage.iam.InAppEventHandlerInternal
import com.emarsys.mobileengage.iam.InAppInternal
import com.emarsys.mobileengage.iam.OverlayInAppPresenter
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridgeFactory
import com.emarsys.mobileengage.iam.jsbridge.JSCommandFactoryProvider
import com.emarsys.mobileengage.iam.jsbridge.OnAppEventListener
import com.emarsys.mobileengage.iam.jsbridge.OnCloseListener
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam
import com.emarsys.mobileengage.iam.webview.IamWebViewFactory
import com.emarsys.mobileengage.inbox.MessageInboxInternal
import com.emarsys.mobileengage.notification.ActionCommandFactory
import com.emarsys.mobileengage.push.NotificationInformationListenerProvider
import com.emarsys.mobileengage.push.PushInternal
import com.emarsys.mobileengage.push.PushTokenProvider
import com.emarsys.mobileengage.push.SilentNotificationInformationListenerProvider
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.mobileengage.responsehandler.MobileEngageTokenResponseHandler
import com.emarsys.mobileengage.service.mapper.RemoteMessageMapperFactory
import com.emarsys.mobileengage.session.MobileEngageSession
import com.emarsys.mobileengage.session.SessionIdHolder
import com.emarsys.mobileengage.util.RequestModelHelper
import com.google.android.gms.location.FusedLocationProviderClient
import io.mockk.mockk

class FakeFirebaseDependencyContainer(
    override val concurrentHandlerHolder: ConcurrentHandlerHolder = ConcurrentHandlerHolderFactory.create(),
    override val mobileEngageInternal: MobileEngageInternal = mockk(relaxed = true),
    override val loggingMobileEngageInternal: MobileEngageInternal = mockk(relaxed = true),
    override val clientServiceInternal: ClientServiceInternal = mockk(relaxed = true),
    override val loggingClientServiceInternal: ClientServiceInternal = mockk(relaxed = true),
    override val messageInboxInternal: MessageInboxInternal = mockk(relaxed = true),
    override val loggingMessageInboxInternal: MessageInboxInternal = mockk(relaxed = true),
    override val inAppInternal: InAppInternal = mockk(relaxed = true),
    override val loggingInAppInternal: InAppInternal = mockk(relaxed = true),
    override val deepLinkInternal: DeepLinkInternal = mockk(relaxed = true),
    override val pushInternal: PushInternal = mockk(relaxed = true),
    override val loggingPushInternal: PushInternal = mockk(relaxed = true),
    override val eventServiceInternal: EventServiceInternal = mockk(relaxed = true),
    override val loggingEventServiceInternal: EventServiceInternal = mockk(relaxed = true),
    override val inAppEventHandlerInternal: InAppEventHandlerInternal = mockk(relaxed = true),
    override val requestContext: MobileEngageRequestContext = mockk(relaxed = true),
    override val clipboardManager: ClipboardManager = mockk(relaxed = true),
    override val overlayInAppPresenter: OverlayInAppPresenter = mockk(relaxed = true),
    override val deviceInfoPayloadStorage: Storage<String?> = mockk(relaxed = true),
    override val contactFieldValueStorage: Storage<String?> = mockk(relaxed = true),
    override val contactTokenStorage: Storage<String?> = mockk(relaxed = true),
    override val clientStateStorage: Storage<String?> = mockk(relaxed = true),
    override val pushTokenStorage: Storage<String?> = mockk(relaxed = true),
    override val refreshTokenStorage: Storage<String?> = mockk(relaxed = true),
    override val clientServiceStorage: Storage<String?> = mockk(relaxed = true),
    override val eventServiceStorage: Storage<String?> = mockk(relaxed = true),
    override val deepLinkServiceStorage: Storage<String?> = mockk(relaxed = true),
    override val messageInboxServiceStorage: Storage<String?> = mockk(relaxed = true),
    override val deviceEventStateStorage: Storage<String?> = mockk(relaxed = true),
    override val responseHandlersProcessor: ResponseHandlersProcessor = mockk(relaxed = true),
    override val pushTokenProvider: PushTokenProvider = mockk(relaxed = true),
    override val clientServiceEndpointProvider: ServiceEndpointProvider = mockk(relaxed = true),
    override val eventServiceEndpointProvider: ServiceEndpointProvider = mockk(relaxed = true),
    override val deepLinkServiceProvider: ServiceEndpointProvider = mockk(relaxed = true),
    override val messageInboxServiceProvider: ServiceEndpointProvider = mockk(relaxed = true),
    override val notificationInformationListenerProvider: NotificationInformationListenerProvider = mockk(relaxed = true),
    override val silentNotificationInformationListenerProvider: SilentNotificationInformationListenerProvider = mockk(relaxed = true),
    override val notificationActionCommandFactory: ActionCommandFactory = mockk(relaxed = true),
    override val silentMessageActionCommandFactory: ActionCommandFactory = mockk(relaxed = true),
    override val notificationCacheableEventHandler: CacheableEventHandler = mockk(relaxed = true),
    override val silentMessageCacheableEventHandler: CacheableEventHandler = mockk(relaxed = true),
    override val onEventActionCacheableEventHandler: CacheableEventHandler = mockk(relaxed = true),
    override val geofenceCacheableEventHandler: CacheableEventHandler = mockk(relaxed = true),
    override val currentActivityProvider: CurrentActivityProvider = mockk(relaxed = true),
    override val geofenceInternal: GeofenceInternal = mockk(relaxed = true),
    override val loggingGeofenceInternal: GeofenceInternal = mockk(relaxed = true),
    override val buttonClickedRepository: Repository<ButtonClicked, SqlSpecification> = mockk(relaxed = true),
    override val displayedIamRepository: Repository<DisplayedIam, SqlSpecification> = mockk(relaxed = true),
    override val contactTokenResponseHandler: MobileEngageTokenResponseHandler = mockk(relaxed = true),
    override val webViewFactory: IamWebViewFactory = mockk(relaxed = true),
    override val iamJsBridgeFactory: IamJsBridgeFactory = mockk(relaxed = true),
    override val appLifecycleObserver: AppLifecycleObserver = mockk(relaxed = true),
    override val requestModelHelper: RequestModelHelper = mockk(relaxed = true),
    override val sessionIdHolder: SessionIdHolder = mockk(relaxed = true),
    override val mobileEngageRequestModelFactory: MobileEngageRequestModelFactory = mockk(relaxed = true),
    override val mobileEngageSession: MobileEngageSession = mockk(relaxed = true),
    override val activityLifecycleWatchdog: ActivityLifecycleWatchdog = mockk(relaxed = true),
    override val coreSQLiteDatabase: CoreSQLiteDatabase = mockk(relaxed = true),
    override val deviceInfo: DeviceInfo = mockk(relaxed = true),
    override val shardRepository: Repository<ShardModel, SqlSpecification> = mockk(relaxed = true),
    override val timestampProvider: TimestampProvider = mockk(relaxed = true),
    override val uuidProvider: UUIDProvider = mockk(relaxed = true),
    override val logShardTrigger: Runnable = mockk(relaxed = true),
    override val logger: Logger = mockk(relaxed = true),
    override val restClient: RestClient = mockk(relaxed = true),
    override val fileDownloader: FileDownloader = mockk(relaxed = true),
    override val keyValueStore: KeyValueStore = mockk(relaxed = true),
    override val sharedPreferences: SharedPreferences = mockk(relaxed = true),
    override val clientIdProvider: ClientIdProvider = mockk(relaxed = true),
    override val coreDbHelper: CoreDbHelper = mockk(relaxed = true),
    override val clientIdStorage: Storage<String?> = mockk(relaxed = true),
    override val logLevelStorage: Storage<String?> = mockk(relaxed = true),
    override val crypto: Crypto = mockk(relaxed = true),
    override val requestManager: RequestManager = mockk(relaxed = true),
    override val worker: Worker = mockk(relaxed = true),
    override val requestModelRepository: Repository<RequestModel, SqlSpecification> = mockk(relaxed = true),
    override val connectionWatchdog: ConnectionWatchDog = mockk(relaxed = true),
    override val coreCompletionHandler: CoreCompletionHandler = mockk(relaxed = true),
    override val geofenceInitialEnterTriggerEnabledStorage: Storage<Boolean?> = mockk(relaxed = true),
    override val fusedLocationProviderClient: FusedLocationProviderClient = mockk(relaxed = true),
    override val activityLifecycleActionRegistry: ActivityLifecycleActionRegistry = mockk(relaxed = true),
    override val notificationOpenedActivityClass: Class<*> = Activity::class.java,
    override val jsCommandFactoryProvider: JSCommandFactoryProvider = mockk(relaxed = true),
    override val jsOnCloseListener: OnCloseListener = mockk(relaxed = true),
    override val jsOnAppEventListener: OnAppEventListener = mockk(relaxed = true),
    override val remoteMessageMapperFactory: RemoteMessageMapperFactory = mockk(relaxed = true),
    override val transitionSafeCurrentActivityWatchdog: TransitionSafeCurrentActivityWatchdog = mockk(relaxed = true),
    override val sharedPreferencesV3: SharedPreferences = mockk(relaxed = true)
) : MobileEngageComponent