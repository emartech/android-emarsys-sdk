package com.emarsys.di;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.NotificationManagerCompat;

import com.emarsys.Emarsys;
import com.emarsys.config.EmarsysConfig;
import com.emarsys.core.DefaultCoreCompletionHandler;
import com.emarsys.core.Mapper;
import com.emarsys.core.RunnerProxy;
import com.emarsys.core.activity.ActivityLifecycleAction;
import com.emarsys.core.activity.ActivityLifecycleWatchdog;
import com.emarsys.core.activity.CurrentActivityWatchdog;
import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.concurrency.CoreSdkHandlerProvider;
import com.emarsys.core.connection.ConnectionProvider;
import com.emarsys.core.connection.ConnectionWatchDog;
import com.emarsys.core.database.CoreSQLiteDatabase;
import com.emarsys.core.database.helper.CoreDbHelper;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.database.trigger.TriggerKey;
import com.emarsys.core.device.DeviceInfo;
import com.emarsys.core.device.LanguageProvider;
import com.emarsys.core.feature.FeatureRegistry;
import com.emarsys.core.notification.NotificationManagerHelper;
import com.emarsys.core.notification.NotificationManagerProxy;
import com.emarsys.core.notification.NotificationSettings;
import com.emarsys.core.provider.activity.CurrentActivityProvider;
import com.emarsys.core.provider.hardwareid.HardwareIdProvider;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.provider.version.VersionProvider;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.RestClient;
import com.emarsys.core.request.factory.CoreCompletionHandlerMiddlewareProvider;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.request.model.RequestModelRepository;
import com.emarsys.core.response.AbstractResponseHandler;
import com.emarsys.core.response.ResponseHandlersProcessor;
import com.emarsys.core.shard.ShardModel;
import com.emarsys.core.shard.ShardModelRepository;
import com.emarsys.core.shard.specification.FilterByShardType;
import com.emarsys.core.storage.DefaultKeyValueStore;
import com.emarsys.core.storage.KeyValueStore;
import com.emarsys.core.storage.Storage;
import com.emarsys.core.storage.StringStorage;
import com.emarsys.core.util.batch.BatchingShardTrigger;
import com.emarsys.core.util.batch.ListChunker;
import com.emarsys.core.util.log.LogShardListMerger;
import com.emarsys.core.util.log.Logger;
import com.emarsys.core.util.predicate.ListSizeAtLeast;
import com.emarsys.core.worker.DefaultWorker;
import com.emarsys.core.worker.Worker;
import com.emarsys.feature.InnerFeature;
import com.emarsys.inapp.InAppApi;
import com.emarsys.inapp.InAppProxy;
import com.emarsys.inbox.InboxApi;
import com.emarsys.inbox.InboxProxy;
import com.emarsys.mobileengage.DefaultMobileEngageInternal;
import com.emarsys.mobileengage.LoggingMobileEngageInternal;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.MobileEngageRefreshTokenInternal;
import com.emarsys.mobileengage.MobileEngageRequestContext;
import com.emarsys.mobileengage.RefreshTokenInternal;
import com.emarsys.mobileengage.api.NotificationEventHandler;
import com.emarsys.mobileengage.client.ClientServiceInternal;
import com.emarsys.mobileengage.client.DefaultClientServiceInternal;
import com.emarsys.mobileengage.client.LoggingClientServiceInternal;
import com.emarsys.mobileengage.deeplink.DeepLinkAction;
import com.emarsys.mobileengage.deeplink.DeepLinkInternal;
import com.emarsys.mobileengage.deeplink.DefaultDeepLinkInternal;
import com.emarsys.mobileengage.deeplink.LoggingDeepLinkInternal;
import com.emarsys.mobileengage.device.DeviceInfoStartAction;
import com.emarsys.mobileengage.event.DefaultEventServiceInternal;
import com.emarsys.mobileengage.event.EventServiceInternal;
import com.emarsys.mobileengage.event.LoggingEventServiceInternal;
import com.emarsys.mobileengage.iam.DefaultInAppInternal;
import com.emarsys.mobileengage.iam.InAppEventHandlerInternal;
import com.emarsys.mobileengage.iam.InAppInternal;
import com.emarsys.mobileengage.iam.InAppPresenter;
import com.emarsys.mobileengage.iam.InAppStartAction;
import com.emarsys.mobileengage.iam.LoggingInAppInternal;
import com.emarsys.mobileengage.iam.dialog.IamDialogProvider;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClickedRepository;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIamRepository;
import com.emarsys.mobileengage.iam.model.requestRepositoryProxy.RequestRepositoryProxy;
import com.emarsys.mobileengage.iam.webview.IamWebViewProvider;
import com.emarsys.mobileengage.inbox.InboxInternal;
import com.emarsys.mobileengage.inbox.InboxInternalProvider;
import com.emarsys.mobileengage.inbox.model.NotificationCache;
import com.emarsys.mobileengage.push.DefaultPushInternal;
import com.emarsys.mobileengage.push.LoggingPushInternal;
import com.emarsys.mobileengage.push.PushInternal;
import com.emarsys.mobileengage.request.CoreCompletionHandlerRefreshTokenProxyProvider;
import com.emarsys.mobileengage.request.MobileEngageHeaderMapper;
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory;
import com.emarsys.mobileengage.responsehandler.ClientInfoResponseHandler;
import com.emarsys.mobileengage.responsehandler.InAppCleanUpResponseHandler;
import com.emarsys.mobileengage.responsehandler.InAppMessageResponseHandler;
import com.emarsys.mobileengage.responsehandler.MobileEngageClientStateResponseHandler;
import com.emarsys.mobileengage.responsehandler.MobileEngageTokenResponseHandler;
import com.emarsys.mobileengage.storage.DeviceInfoHashStorage;
import com.emarsys.mobileengage.storage.MobileEngageStorageKey;
import com.emarsys.mobileengage.util.RequestHeaderUtils;
import com.emarsys.predict.DefaultPredictInternal;
import com.emarsys.predict.LoggingPredictInternal;
import com.emarsys.predict.PredictApi;
import com.emarsys.predict.PredictInternal;
import com.emarsys.predict.PredictProxy;
import com.emarsys.predict.request.PredictHeaderFactory;
import com.emarsys.predict.request.PredictRequestContext;
import com.emarsys.predict.request.PredictRequestModelFactory;
import com.emarsys.predict.response.VisitorIdResponseHandler;
import com.emarsys.predict.shard.PredictShardListMerger;
import com.emarsys.push.PushApi;
import com.emarsys.push.PushProxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DefaultEmarsysDependencyContainer implements EmarysDependencyContainer {

    private static final String EMARSYS_SHARED_PREFERENCES_NAME = "emarsys_shared_preferences";

    private MobileEngageInternal mobileEngageInternal;
    private InboxInternal inboxInternal;
    private InAppEventHandlerInternal inAppEventHandler;
    private DeepLinkInternal deepLinkInternal;
    private PredictInternal predictInternal;
    private PushInternal pushInternal;
    private ClientServiceInternal clientServiceInternal;

    private Handler coreSdkHandler;
    private DeviceInfo deviceInfo;
    private Repository<ShardModel, SqlSpecification> shardModelRepository;
    private TimestampProvider timestampProvider;
    private UUIDProvider uuidProvider;
    private Runnable logShardTrigger;
    private MobileEngageRequestContext requestContext;
    private DefaultCoreCompletionHandler completionHandler;
    private InAppPresenter inAppPresenter;
    private NotificationEventHandler notificationEventHandler;
    private CoreSQLiteDatabase coreDatabase;
    private Runnable predictShardTrigger;

    private Handler uiHandler;
    private Storage<Integer> deviceInfoHashStorage;
    private Storage<String> contactTokenStorage;
    private Storage<String> refreshTokenStorage;
    private Storage<String> clientStateStorage;
    private Storage<String> contactFieldValueStorage;
    private RequestManager requestManager;
    private MobileEngageRequestModelFactory requestModelFactory;
    private ButtonClickedRepository buttonClickedRepository;
    private DisplayedIamRepository displayedIamRepository;
    private Repository<RequestModel, SqlSpecification> requestModelRepository;
    private RestClient restClient;
    private Application application;
    private ActivityLifecycleWatchdog activityLifecycleWatchdog;
    private CurrentActivityWatchdog currentActivityWatchdog;
    private KeyValueStore sharedPrefsKeyStore;
    private CurrentActivityProvider currentActivityProvider;
    private RunnerProxy runnerProxy;
    private Logger logger;
    private MobileEngageRefreshTokenInternal refreshTokenInternal;
    private ResponseHandlersProcessor responseHandlersProcessor;
    private EventServiceInternal eventServiceInternal;
    private InAppInternal inAppInternal;
    private MobileEngageTokenResponseHandler contactTokenResponseHandler;
    private NotificationCache notificationCache;
    private InboxApi inboxApi;
    private InAppApi inAppApi;
    private PushApi pushApi;
    private PredictApi predictApi;
    private PredictRequestContext predictRequestContext;

    public DefaultEmarsysDependencyContainer(EmarsysConfig emarsysConfig) {
        initializeFeatures(emarsysConfig);
        initializeDependencies(emarsysConfig);
        initializeInAppPresenter(emarsysConfig);
        initializeResponseHandlers();
        initializeActivityLifecycleWatchdog();
    }

    @Override
    public MobileEngageInternal getMobileEngageInternal() {
        return mobileEngageInternal;
    }

    @Override
    public RefreshTokenInternal getRefreshTokenInternal() {
        return refreshTokenInternal;
    }

    @Override
    public ClientServiceInternal getClientServiceInternal() {
        return clientServiceInternal;
    }

    @Override
    public InboxInternal getInboxInternal() {
        return inboxInternal;
    }

    @Override
    public InAppInternal getInAppInternal() {
        return inAppInternal;
    }

    @Override
    public DeepLinkInternal getDeepLinkInternal() {
        return deepLinkInternal;
    }

    @Override
    public PredictInternal getPredictInternal() {
        return predictInternal;
    }

    @Override
    public Runnable getPredictShardTrigger() {
        return predictShardTrigger;
    }

    @Override
    public Handler getCoreSdkHandler() {
        return coreSdkHandler;
    }

    @Override
    public MobileEngageRequestContext getRequestContext() {
        return requestContext;
    }

    @Override
    public DefaultCoreCompletionHandler getCoreCompletionHandler() {
        if (completionHandler == null) {
            completionHandler = new DefaultCoreCompletionHandler(new HashMap<String, CompletionListener>());
        }
        return completionHandler;
    }

    @Override
    public ActivityLifecycleWatchdog getActivityLifecycleWatchdog() {
        return activityLifecycleWatchdog;
    }

    @Override
    public CurrentActivityWatchdog getCurrentActivityWatchdog() {
        return currentActivityWatchdog;
    }

    @Override
    public CoreSQLiteDatabase getCoreSQLiteDatabase() {
        return coreDatabase;
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    @Override
    public Repository<ShardModel, SqlSpecification> getShardRepository() {
        return shardModelRepository;
    }

    @Override
    public TimestampProvider getTimestampProvider() {
        return timestampProvider;
    }

    @Override
    public UUIDProvider getUuidProvider() {
        return uuidProvider;
    }

    @Override
    public Runnable getLogShardTrigger() {
        return logShardTrigger;
    }

    @Override
    public InAppPresenter getInAppPresenter() {
        return inAppPresenter;
    }

    @Override
    public NotificationEventHandler getNotificationEventHandler() {
        return notificationEventHandler;
    }

    @Override
    public Storage<Integer> getDeviceInfoHashStorage() {
        return deviceInfoHashStorage;
    }

    @Override
    public Storage<String> getContactFieldValueStorage() {
        return contactFieldValueStorage;
    }

    @Override
    public Storage<String> getContactTokenStorage() {
        return contactTokenStorage;
    }

    @Override
    public Storage<String> getClientStateStorage() {
        return clientStateStorage;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public RunnerProxy getRunnerProxy() {
        return runnerProxy;
    }

    @Override
    public ResponseHandlersProcessor getResponseHandlersProcessor() {
        return responseHandlersProcessor;
    }

    @Override
    public NotificationCache getNotificationCache() {
        return notificationCache;
    }

    @Override
    public PushInternal getPushInternal() {
        return pushInternal;
    }

    @Override
    public EventServiceInternal getEventServiceInternal() {
        return eventServiceInternal;
    }

    @Override
    public RestClient getRestClient() {
        return restClient;
    }

    private void initializeFeatures(EmarsysConfig emarsysConfig) {
        if (emarsysConfig.getMobileEngageApplicationCode() != null) {
            FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE);
        }

        if (emarsysConfig.getPredictMerchantId() != null) {
            FeatureRegistry.enableFeature(InnerFeature.PREDICT);
        }
    }

    private void initializeDependencies(EmarsysConfig config) {
        application = config.getApplication();
        runnerProxy = new RunnerProxy();
        SharedPreferences prefs = application.getSharedPreferences(EMARSYS_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        uiHandler = new Handler(Looper.getMainLooper());
        coreSdkHandler = new CoreSdkHandlerProvider().provideHandler();
        timestampProvider = new TimestampProvider();
        uuidProvider = new UUIDProvider();
        deviceInfoHashStorage = new DeviceInfoHashStorage(prefs);
        contactTokenStorage = new StringStorage(MobileEngageStorageKey.CONTACT_TOKEN, prefs);
        refreshTokenStorage = new StringStorage(MobileEngageStorageKey.REFRESH_TOKEN, prefs);
        clientStateStorage = new StringStorage(MobileEngageStorageKey.CLIENT_STATE, prefs);
        contactFieldValueStorage = new StringStorage(MobileEngageStorageKey.CONTACT_FIELD_VALUE, prefs);

        responseHandlersProcessor = new ResponseHandlersProcessor(new ArrayList<AbstractResponseHandler>());

        LanguageProvider languageProvider = new LanguageProvider();
        HardwareIdProvider hardwareIdProvider = new HardwareIdProvider(application, prefs);
        VersionProvider versionProvider = new VersionProvider();

        NotificationManager notificationManager = (NotificationManager) application.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(application);
        NotificationManagerProxy notificationManagerProxy = new NotificationManagerProxy(notificationManager, notificationManagerCompat);
        NotificationSettings notificationSettings = new NotificationManagerHelper(notificationManagerProxy);
        deviceInfo = new DeviceInfo(application, hardwareIdProvider, versionProvider, languageProvider, notificationSettings, config.isAutomaticPushTokenSendingEnabled());

        currentActivityProvider = new CurrentActivityProvider();
        currentActivityWatchdog = new CurrentActivityWatchdog(currentActivityProvider);

        CoreDbHelper coreDbHelper = new CoreDbHelper(application, new HashMap<TriggerKey, List<Runnable>>());
        coreDatabase = coreDbHelper.getWritableCoreDatabase();

        buttonClickedRepository = new ButtonClickedRepository(coreDbHelper);
        displayedIamRepository = new DisplayedIamRepository(coreDbHelper);

        requestContext = new MobileEngageRequestContext(
                config.getMobileEngageApplicationCode(),
                config.getContactFieldId(),
                getDeviceInfo(),
                timestampProvider,
                uuidProvider,
                clientStateStorage,
                contactTokenStorage,
                refreshTokenStorage,
                contactFieldValueStorage);

        inAppEventHandler = new InAppEventHandlerInternal();

        requestModelRepository = createRequestModelRepository(coreDbHelper);
        shardModelRepository = new ShardModelRepository(coreDbHelper);

        restClient = new RestClient(new ConnectionProvider(), timestampProvider, getResponseHandlersProcessor(), createRequestModelMappers());

        requestModelFactory = new MobileEngageRequestModelFactory(requestContext);

        contactTokenResponseHandler = new MobileEngageTokenResponseHandler("contactToken", contactTokenStorage);

        notificationCache = new NotificationCache();

        refreshTokenInternal = new MobileEngageRefreshTokenInternal(
                contactTokenResponseHandler,
                getRestClient(),
                requestModelFactory);

        ConnectionWatchDog connectionWatchDog = new ConnectionWatchDog(application, coreSdkHandler);
        CoreCompletionHandlerMiddlewareProvider coreCompletionHandlerMiddlewareProvider = new CoreCompletionHandlerMiddlewareProvider(
                getCoreCompletionHandler(),
                requestModelRepository,
                uiHandler,
                coreSdkHandler
        );

        CoreCompletionHandlerRefreshTokenProxyProvider coreCompletionHandlerRefreshTokenProxyProvider = new CoreCompletionHandlerRefreshTokenProxyProvider(
                coreCompletionHandlerMiddlewareProvider,
                refreshTokenInternal,
                getRestClient(),
                contactTokenStorage
        );
        Worker worker = new DefaultWorker(
                requestModelRepository,
                connectionWatchDog,
                uiHandler,
                getCoreCompletionHandler(),
                getRestClient(),
                coreCompletionHandlerRefreshTokenProxyProvider);

        requestManager = new RequestManager(
                coreSdkHandler,
                requestModelRepository,
                shardModelRepository,
                worker,
                getRestClient(),
                getCoreCompletionHandler(),
                getCoreCompletionHandler());

        requestManager.setDefaultHeaders(RequestHeaderUtils.createDefaultHeaders(requestContext));

        sharedPrefsKeyStore = new DefaultKeyValueStore(prefs);
        notificationEventHandler = config.getNotificationEventHandler();
        logShardTrigger = new BatchingShardTrigger(
                shardModelRepository,
                new ListSizeAtLeast<ShardModel>(10),
                new FilterByShardType(FilterByShardType.SHARD_TYPE_LOG),
                new ListChunker<ShardModel>(10),
                new LogShardListMerger(timestampProvider, uuidProvider, getDeviceInfo(), config.getMobileEngageApplicationCode(), config.getPredictMerchantId()),
                requestManager,
                BatchingShardTrigger.RequestStrategy.TRANSIENT);

        if (FeatureRegistry.isFeatureEnabled(InnerFeature.PREDICT)) {
            predictRequestContext = new PredictRequestContext(config.getPredictMerchantId(), deviceInfo, timestampProvider, uuidProvider, sharedPrefsKeyStore);

            PredictHeaderFactory headerFactory = new PredictHeaderFactory(predictRequestContext);
            PredictRequestModelFactory predictRequestModelFactory = new PredictRequestModelFactory(predictRequestContext, headerFactory);

            predictShardTrigger = new BatchingShardTrigger(
                    shardModelRepository,
                    new ListSizeAtLeast<ShardModel>(1),
                    new FilterByShardType(FilterByShardType.SHARD_TYPE_PREDICT),
                    new ListChunker<ShardModel>(1),
                    new PredictShardListMerger(predictRequestContext),
                    requestManager,
                    BatchingShardTrigger.RequestStrategy.PERSISTENT);
            predictInternal = new DefaultPredictInternal(predictRequestContext, requestManager, predictRequestModelFactory);
        } else {
            predictInternal = new LoggingPredictInternal(Emarsys.Predict.class);
        }

        InboxInternalProvider inboxInternalProvider = new InboxInternalProvider();

        if (FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE)) {
            eventServiceInternal = new DefaultEventServiceInternal(requestManager, requestModelFactory);
            clientServiceInternal = new DefaultClientServiceInternal(requestManager, requestModelFactory);
            deepLinkInternal = new DefaultDeepLinkInternal(requestManager, requestContext);

            pushInternal = new DefaultPushInternal(requestManager, uiHandler, requestModelFactory, eventServiceInternal);
            inAppInternal = new DefaultInAppInternal(inAppEventHandler, eventServiceInternal);
            mobileEngageInternal = new DefaultMobileEngageInternal(requestManager, requestModelFactory, requestContext);

            inboxInternal = inboxInternalProvider.provideInboxInternal(
                    requestManager,
                    requestContext,
                    requestModelFactory
            );
        } else {
            deepLinkInternal = new LoggingDeepLinkInternal(Emarsys.class);
            pushInternal = new LoggingPushInternal(Emarsys.Push.class);
            clientServiceInternal = new LoggingClientServiceInternal(Emarsys.class);
            eventServiceInternal = new LoggingEventServiceInternal(Emarsys.class);
            inAppInternal = new LoggingInAppInternal(Emarsys.InApp.class);
            mobileEngageInternal = new LoggingMobileEngageInternal(Emarsys.class);
            inboxInternal = inboxInternalProvider.provideLoggingInboxInternal(Emarsys.Inbox.class);
        }

        inboxApi = new InboxProxy(runnerProxy, inboxInternal);
        inAppApi = new InAppProxy(runnerProxy, inAppInternal);
        pushApi = new PushProxy(runnerProxy, pushInternal);
        predictApi = new PredictProxy(runnerProxy, predictInternal);

        logger = new Logger(coreSdkHandler, shardModelRepository, timestampProvider, uuidProvider);
    }

    private Repository<RequestModel, SqlSpecification> createRequestModelRepository(CoreDbHelper coreDbHelper) {
        RequestModelRepository requestModelRepository = new RequestModelRepository(coreDbHelper);
        return new RequestRepositoryProxy(
                requestModelRepository,
                displayedIamRepository,
                buttonClickedRepository,
                timestampProvider,
                uuidProvider,
                inAppEventHandler);
    }

    private List<Mapper<RequestModel, RequestModel>> createRequestModelMappers() {
        List<Mapper<RequestModel, RequestModel>> mappers = new ArrayList<>();
        mappers.add(new MobileEngageHeaderMapper(requestContext));
        return mappers;
    }

    private void initializeActivityLifecycleWatchdog() {
        ActivityLifecycleAction[] applicationStartActions = new ActivityLifecycleAction[]{
                new DeviceInfoStartAction(getClientServiceInternal(), deviceInfoHashStorage, getDeviceInfo()),
                new InAppStartAction(eventServiceInternal, contactTokenStorage)
        };

        ActivityLifecycleAction[] activityCreatedActions = new ActivityLifecycleAction[]{
                new DeepLinkAction(deepLinkInternal)
        };

        activityLifecycleWatchdog = new ActivityLifecycleWatchdog(
                applicationStartActions,
                activityCreatedActions);
    }

    private void initializeInAppPresenter(EmarsysConfig emarsysConfig) {
        inAppPresenter = new InAppPresenter(
                coreSdkHandler,
                new IamWebViewProvider(emarsysConfig.getApplication()),
                inAppInternal,
                new IamDialogProvider(),
                buttonClickedRepository,
                displayedIamRepository,
                timestampProvider,
                currentActivityProvider);
    }

    private void initializeResponseHandlers() {

        List<AbstractResponseHandler> responseHandlers = new ArrayList<>();

        responseHandlers.add(new VisitorIdResponseHandler(sharedPrefsKeyStore));

        responseHandlers.add(new MobileEngageTokenResponseHandler("refreshToken", refreshTokenStorage));
        responseHandlers.add(contactTokenResponseHandler);
        responseHandlers.add(new MobileEngageClientStateResponseHandler(getClientStateStorage()));
        responseHandlers.add(new ClientInfoResponseHandler(getDeviceInfo(), getDeviceInfoHashStorage()));

        responseHandlers.add(new InAppMessageResponseHandler(
                inAppPresenter
        ));

        responseHandlers.add(new InAppCleanUpResponseHandler(
                displayedIamRepository,
                buttonClickedRepository
        ));
        responseHandlersProcessor.addResponseHandlers(responseHandlers);
    }

    @Override
    public InboxApi getInbox() {
        return inboxApi;
    }

    @Override
    public InAppApi getInApp() {
        return inAppApi;
    }

    @Override
    public PushApi getPush() {
        return pushApi;
    }

    @Override
    public PredictApi getPredict() {
        return predictApi;
    }
}
