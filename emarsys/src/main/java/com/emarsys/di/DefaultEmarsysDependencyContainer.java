package com.emarsys.di;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.emarsys.config.EmarsysConfig;
import com.emarsys.core.DefaultCoreCompletionHandler;
import com.emarsys.core.DeviceInfo;
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
import com.emarsys.core.database.repository.log.LogRepository;
import com.emarsys.core.database.trigger.TriggerKey;
import com.emarsys.core.experimental.ExperimentalFeatures;
import com.emarsys.core.provider.activity.CurrentActivityProvider;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.RestClient;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.request.model.RequestModelRepository;
import com.emarsys.core.response.AbstractResponseHandler;
import com.emarsys.core.shard.ShardModel;
import com.emarsys.core.shard.ShardModelRepository;
import com.emarsys.core.storage.DefaultKeyValueStore;
import com.emarsys.core.storage.KeyValueStore;
import com.emarsys.core.worker.DefaultWorker;
import com.emarsys.core.worker.Worker;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.api.NotificationEventHandler;
import com.emarsys.mobileengage.api.experimental.MobileEngageFeature;
import com.emarsys.mobileengage.config.OreoConfig;
import com.emarsys.mobileengage.database.MobileEngageDbHelper;
import com.emarsys.mobileengage.deeplink.DeepLinkAction;
import com.emarsys.mobileengage.deeplink.DeepLinkInternal;
import com.emarsys.mobileengage.experimental.MobileEngageExperimentalFeatures;
import com.emarsys.mobileengage.iam.DoNotDisturbProvider;
import com.emarsys.mobileengage.iam.InAppInternal;
import com.emarsys.mobileengage.iam.InAppPresenter;
import com.emarsys.mobileengage.iam.InAppStartAction;
import com.emarsys.mobileengage.iam.dialog.IamDialogProvider;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClickedRepository;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIamRepository;
import com.emarsys.mobileengage.iam.model.requestRepositoryProxy.RequestRepositoryProxy;
import com.emarsys.mobileengage.iam.webview.IamWebViewProvider;
import com.emarsys.mobileengage.inbox.InboxInternal;
import com.emarsys.mobileengage.inbox.InboxInternalProvider;
import com.emarsys.mobileengage.log.LogRepositoryProxy;
import com.emarsys.mobileengage.log.handler.IamMetricsLogHandler;
import com.emarsys.mobileengage.responsehandler.InAppCleanUpResponseHandler;
import com.emarsys.mobileengage.responsehandler.InAppMessageResponseHandler;
import com.emarsys.mobileengage.responsehandler.MeIdResponseHandler;
import com.emarsys.mobileengage.storage.AppLoginStorage;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.util.RequestHeaderUtils;
import com.emarsys.predict.PredictInternal;
import com.emarsys.predict.response.VisitorIdResponseHandler;
import com.emarsys.predict.shard.PredictShardListChunker;
import com.emarsys.predict.shard.PredictShardListMerger;
import com.emarsys.predict.shard.PredictShardTrigger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultEmarsysDependencyContainer implements EmarysDependencyContainer {

    private static final String EMARSYS_SHARED_PREFERENCES_NAME = "emarsys_shared_preferences";

    private MobileEngageInternal mobileEngageInternal;
    private InboxInternal inboxInternal;
    private InAppInternal inAppInternal;
    private DeepLinkInternal deepLinkInternal;
    private PredictInternal predictInternal;
    private Handler coreSdkHandler;
    private RequestContext requestContext;
    private DefaultCoreCompletionHandler completionHandler;
    private InAppPresenter inAppPresenter;
    private NotificationEventHandler notificationEventHandler;
    private OreoConfig oreoConfig;
    private CoreSQLiteDatabase coreDatabase;
    private Runnable predictShardTrigger;

    private Handler uiHandler;
    private TimestampProvider timestampProvider;
    private DoNotDisturbProvider doNotDisturbProvider;
    private AppLoginStorage appLoginStorage;
    private MeIdStorage meIdStorage;
    private MeIdSignatureStorage meIdSignatureStorage;
    private DeviceInfo deviceInfo;
    private RequestManager requestManager;
    private ButtonClickedRepository buttonClickedRepository;
    private DisplayedIamRepository displayedIamRepository;
    private Repository<RequestModel, SqlSpecification> requestModelRepository;
    private Repository<ShardModel, SqlSpecification> shardModelRepository;
    private RestClient restClient;
    private Repository<Map<String, Object>, SqlSpecification> logRepositoryProxy;
    private Application application;
    private ActivityLifecycleWatchdog activityLifecycleWatchdog;
    private CurrentActivityWatchdog currentActivityWatchdog;
    private UUIDProvider uuidProvider;
    private KeyValueStore sharedPrefsKeyStore;
    private CurrentActivityProvider currentActivityProvider;

    public DefaultEmarsysDependencyContainer(EmarsysConfig emarsysConfig) {
        initializeDependencies(emarsysConfig);
        initializeInstances();
        initializeInAppPresenter();
        initializeResponseHandlers();
        initializeActivityLifecycleWatchdog();
    }

    @Override
    public MobileEngageInternal getMobileEngageInternal() {
        return mobileEngageInternal;
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
    public RequestContext getRequestContext() {
        return requestContext;
    }

    @Override
    public DefaultCoreCompletionHandler getCoreCompletionHandler() {
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
    public InAppPresenter getInAppPresenter() {
        return inAppPresenter;
    }

    @Override
    public NotificationEventHandler getNotificationEventHandler() {
        return notificationEventHandler;
    }

    @Override
    public OreoConfig getOreoConfig() {
        return oreoConfig;
    }

    private void initializeDependencies(EmarsysConfig config) {
        application = config.getApplication();
        oreoConfig = config.getOreoConfig();

        uiHandler = new Handler(Looper.getMainLooper());
        coreSdkHandler = new CoreSdkHandlerProvider().provideHandler();
        timestampProvider = new TimestampProvider();
        uuidProvider = new UUIDProvider();
        doNotDisturbProvider = new DoNotDisturbProvider();
        appLoginStorage = new AppLoginStorage(application);
        meIdStorage = new MeIdStorage(application);
        meIdSignatureStorage = new MeIdSignatureStorage(application);
        deviceInfo = new DeviceInfo(application);

        currentActivityProvider = new CurrentActivityProvider();
        currentActivityWatchdog = new CurrentActivityWatchdog(currentActivityProvider);

        CoreDbHelper coreDbHelper = new CoreDbHelper(application, new HashMap<TriggerKey, List<Runnable>>());
        coreDatabase = coreDbHelper.getWritableCoreDatabase();
        MobileEngageDbHelper mobileEngageDbHelper = new MobileEngageDbHelper(application, new HashMap<TriggerKey, List<Runnable>>());

        buttonClickedRepository = new ButtonClickedRepository(mobileEngageDbHelper);
        displayedIamRepository = new DisplayedIamRepository(mobileEngageDbHelper);

        requestModelRepository = createRequestModelRepository(coreDbHelper);
        shardModelRepository = new ShardModelRepository(coreDbHelper);

        completionHandler = new DefaultCoreCompletionHandler(new ArrayList<AbstractResponseHandler>(), new HashMap<String, CompletionListener>());

        Repository<Map<String, Object>, SqlSpecification> logRepository = new LogRepository(application);
        List<com.emarsys.core.handler.Handler<Map<String, Object>, Map<String, Object>>> logHandlers = Arrays.<com.emarsys.core.handler.Handler<Map<String, Object>, Map<String, Object>>>asList(
                new IamMetricsLogHandler(new HashMap<String, Map<String, Object>>())
        );
        logRepositoryProxy = new LogRepositoryProxy(logRepository, logHandlers);
        restClient = new RestClient(logRepositoryProxy, new ConnectionProvider(), timestampProvider);

        ConnectionWatchDog connectionWatchDog = new ConnectionWatchDog(application, coreSdkHandler);
        Worker worker = new DefaultWorker(
                requestModelRepository,
                connectionWatchDog,
                uiHandler,
                coreSdkHandler,
                completionHandler,
                restClient);

        requestManager = new RequestManager(
                coreSdkHandler,
                requestModelRepository,
                shardModelRepository,
                worker,
                restClient);

        requestContext = new RequestContext(
                config.getApplicationCode(),
                config.getApplicationPassword(),
                config.getContactFieldId(),
                deviceInfo,
                appLoginStorage,
                meIdStorage,
                meIdSignatureStorage,
                timestampProvider,
                uuidProvider);

        requestManager.setDefaultHeaders(RequestHeaderUtils.createDefaultHeaders(requestContext));

        SharedPreferences prefs = application.getSharedPreferences(EMARSYS_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        sharedPrefsKeyStore = new DefaultKeyValueStore(prefs);
        notificationEventHandler = config.getNotificationEventHandler();

        predictShardTrigger = new PredictShardTrigger(
                shardModelRepository,
                new PredictShardListChunker(),
                new PredictShardListMerger(
                        config.getPredictMerchantId(),
                        sharedPrefsKeyStore,
                        timestampProvider,
                        uuidProvider),
                requestManager);
    }

    private Repository<RequestModel, SqlSpecification> createRequestModelRepository(CoreDbHelper coreDbHelper) {
        RequestModelRepository requestModelRepository = new RequestModelRepository(coreDbHelper);
        if (MobileEngageExperimentalFeatures.isV3Enabled()) {
            return new RequestRepositoryProxy(
                    deviceInfo,
                    requestModelRepository,
                    displayedIamRepository,
                    buttonClickedRepository,
                    timestampProvider,
                    doNotDisturbProvider);
        } else {
            return requestModelRepository;
        }
    }

    private void initializeInstances() {
        mobileEngageInternal = new MobileEngageInternal(
                requestManager,
                uiHandler,
                completionHandler,
                requestContext
        );
        inboxInternal = new InboxInternalProvider().provideInboxInternal(
                ExperimentalFeatures.isFeatureEnabled(MobileEngageFeature.USER_CENTRIC_INBOX),
                requestManager,
                restClient,
                requestContext
        );
        inAppInternal = new InAppInternal();
        deepLinkInternal = new DeepLinkInternal(requestManager, requestContext);
        predictInternal = new PredictInternal(sharedPrefsKeyStore, requestManager, uuidProvider, timestampProvider);
    }

    private void initializeActivityLifecycleWatchdog() {
        ActivityLifecycleAction[] applicationStartActions = null;
        if (ExperimentalFeatures.isFeatureEnabled(MobileEngageFeature.IN_APP_MESSAGING)) {
            applicationStartActions = new ActivityLifecycleAction[]{
                    new InAppStartAction(mobileEngageInternal)
            };
        }

        ActivityLifecycleAction[] activityCreatedActions = new ActivityLifecycleAction[]{
                new DeepLinkAction(deepLinkInternal)
        };

        activityLifecycleWatchdog = new ActivityLifecycleWatchdog(
                applicationStartActions,
                activityCreatedActions);
    }

    private void initializeInAppPresenter() {
        inAppPresenter = new InAppPresenter(
                coreSdkHandler,
                new IamWebViewProvider(),
                inAppInternal,
                new IamDialogProvider(),
                buttonClickedRepository,
                displayedIamRepository,
                timestampProvider,
                mobileEngageInternal,
                currentActivityProvider);
    }

    private void initializeResponseHandlers() {
        List<AbstractResponseHandler> responseHandlers = new ArrayList<>();

        responseHandlers.add(new VisitorIdResponseHandler(sharedPrefsKeyStore));

        if (MobileEngageExperimentalFeatures.isV3Enabled()) {
            responseHandlers.add(new MeIdResponseHandler(
                    meIdStorage,
                    meIdSignatureStorage));
        }

        if (ExperimentalFeatures.isFeatureEnabled(MobileEngageFeature.IN_APP_MESSAGING)) {
            responseHandlers.add(new InAppMessageResponseHandler(
                    inAppPresenter,
                    logRepositoryProxy,
                    timestampProvider));

            responseHandlers.add(new InAppCleanUpResponseHandler(
                    displayedIamRepository,
                    buttonClickedRepository
            ));
        }

        completionHandler.addResponseHandlers(responseHandlers);
    }
}