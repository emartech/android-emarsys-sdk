package com.emarsys.fake

import android.app.Activity
import android.content.ClipboardManager
import android.content.SharedPreferences
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.activity.ActivityLifecycleActionRegistry
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.activity.CurrentActivityWatchdog
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
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
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
import org.mockito.kotlin.mock

class FakeHuaweiDependencyContainer(
    override val concurrentHandlerHolder: ConcurrentHandlerHolder = ConcurrentHandlerHolderFactory.create(),
    override val mobileEngageInternal: MobileEngageInternal = mock(),
    override val loggingMobileEngageInternal: MobileEngageInternal = mock(),
    override val clientServiceInternal: ClientServiceInternal = mock(),
    override val loggingClientServiceInternal: ClientServiceInternal = mock(),
    override val messageInboxInternal: MessageInboxInternal = mock(),
    override val loggingMessageInboxInternal: MessageInboxInternal = mock(),
    override val inAppInternal: InAppInternal = mock(),
    override val loggingInAppInternal: InAppInternal = mock(),
    override val deepLinkInternal: DeepLinkInternal = mock(),
    override val pushInternal: PushInternal = mock(),
    override val loggingPushInternal: PushInternal = mock(),
    override val eventServiceInternal: EventServiceInternal = mock(),
    override val loggingEventServiceInternal: EventServiceInternal = mock(),
    override val inAppEventHandlerInternal: InAppEventHandlerInternal = mock(),
    override val requestContext: MobileEngageRequestContext = mock(),
    override val clipboardManager: ClipboardManager = mock(),
    override val overlayInAppPresenter: OverlayInAppPresenter = mock(),
    override val deviceInfoPayloadStorage: Storage<String?> = mock(),
    override val contactFieldValueStorage: Storage<String?> = mock(),
    override val contactTokenStorage: Storage<String?> = mock(),
    override val clientStateStorage: Storage<String?> = mock(),
    override val pushTokenStorage: Storage<String?> = mock(),
    override val refreshTokenStorage: Storage<String?> = mock(),
    override val clientServiceStorage: Storage<String?> = mock(),
    override val eventServiceStorage: Storage<String?> = mock(),
    override val deepLinkServiceStorage: Storage<String?> = mock(),
    override val messageInboxServiceStorage: Storage<String?> = mock(),
    override val deviceEventStateStorage: Storage<String?> = mock(),
    override val responseHandlersProcessor: ResponseHandlersProcessor = mock(),
    override val pushTokenProvider: PushTokenProvider = mock(),
    override val clientServiceEndpointProvider: ServiceEndpointProvider = mock(),
    override val eventServiceEndpointProvider: ServiceEndpointProvider = mock(),
    override val deepLinkServiceProvider: ServiceEndpointProvider = mock(),
    override val messageInboxServiceProvider: ServiceEndpointProvider = mock(),
    override val notificationInformationListenerProvider: NotificationInformationListenerProvider = mock(),
    override val silentNotificationInformationListenerProvider: SilentNotificationInformationListenerProvider = mock(),
    override val notificationActionCommandFactory: ActionCommandFactory = mock(),
    override val silentMessageActionCommandFactory: ActionCommandFactory = mock(),
    override val notificationCacheableEventHandler: CacheableEventHandler = mock(),
    override val silentMessageCacheableEventHandler: CacheableEventHandler = mock(),
    override val onEventActionCacheableEventHandler: CacheableEventHandler = mock(),
    override val geofenceCacheableEventHandler: CacheableEventHandler = mock(),
    override val currentActivityProvider: CurrentActivityProvider = mock(),
    override val geofenceInternal: GeofenceInternal = mock(),
    override val loggingGeofenceInternal: GeofenceInternal = mock(),
    override val buttonClickedRepository: Repository<ButtonClicked, SqlSpecification> = mock(),
    override val displayedIamRepository: Repository<DisplayedIam, SqlSpecification> = mock(),
    override val contactTokenResponseHandler: MobileEngageTokenResponseHandler = mock(),
    override val webViewFactory: IamWebViewFactory = mock(),
    override val iamJsBridgeFactory: IamJsBridgeFactory = mock(),
    override val appLifecycleObserver: AppLifecycleObserver = mock(),
    override val requestModelHelper: RequestModelHelper = mock(),
    override val sessionIdHolder: SessionIdHolder = mock(),
    override val mobileEngageRequestModelFactory: MobileEngageRequestModelFactory = mock(),
    override val mobileEngageSession: MobileEngageSession = mock(),
    override val activityLifecycleWatchdog: ActivityLifecycleWatchdog = mock(),
    override val currentActivityWatchdog: CurrentActivityWatchdog = mock(),
    override val coreSQLiteDatabase: CoreSQLiteDatabase = mock(),
    override val deviceInfo: DeviceInfo = mock(),
    override val shardRepository: Repository<ShardModel, SqlSpecification> = mock(),
    override val timestampProvider: TimestampProvider = mock(),
    override val uuidProvider: UUIDProvider = mock(),
    override val logShardTrigger: Runnable = mock(),
    override val logger: Logger = mock(),
    override val restClient: RestClient = mock(),
    override val fileDownloader: FileDownloader = mock(),
    override val keyValueStore: KeyValueStore = mock(),
    override val sharedPreferences: SharedPreferences = mock(),
    override val hardwareIdProvider: HardwareIdProvider = mock(),
    override val coreDbHelper: CoreDbHelper = mock(),
    override val hardwareIdStorage: Storage<String?> = mock(),
    override val logLevelStorage: Storage<String?> = mock(),
    override val crypto: Crypto = mock(),
    override val requestManager: RequestManager = mock(),
    override val worker: Worker = mock(),
    override val requestModelRepository: Repository<RequestModel, SqlSpecification> = mock(),
    override val connectionWatchdog: ConnectionWatchDog = mock(),
    override val coreCompletionHandler: CoreCompletionHandler = mock(),
    override val geofenceInitialEnterTriggerEnabledStorage: Storage<Boolean?> = mock(),
    override val fusedLocationProviderClient: FusedLocationProviderClient = mock(),
    override val activityLifecycleActionRegistry: ActivityLifecycleActionRegistry = mock(),
    override val notificationOpenedActivityClass: Class<*> = Activity::class.java,
    override val jsCommandFactoryProvider: JSCommandFactoryProvider = mock(),
    override val jsOnCloseListener: OnCloseListener = mock(),
    override val jsOnAppEventListener: OnAppEventListener = mock(),
    override val remoteMessageMapperFactory: RemoteMessageMapperFactory = mock(),
    override val transitionSafeCurrentActivityWatchdog: TransitionSafeCurrentActivityWatchdog = mock(),
) : MobileEngageComponent