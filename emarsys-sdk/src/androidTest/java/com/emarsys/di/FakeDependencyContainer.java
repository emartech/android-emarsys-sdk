package com.emarsys.di;

import android.os.Handler;

import com.emarsys.core.DefaultCoreCompletionHandler;
import com.emarsys.core.RunnerProxy;
import com.emarsys.core.activity.ActivityLifecycleWatchdog;
import com.emarsys.core.activity.CurrentActivityWatchdog;
import com.emarsys.core.database.CoreSQLiteDatabase;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.device.DeviceInfo;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.request.RestClient;
import com.emarsys.core.response.ResponseHandlersProcessor;
import com.emarsys.core.shard.ShardModel;
import com.emarsys.core.storage.Storage;
import com.emarsys.core.util.log.Logger;
import com.emarsys.inapp.InAppApi;
import com.emarsys.inbox.InboxApi;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.RefreshTokenInternal;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.api.NotificationEventHandler;
import com.emarsys.mobileengage.client.ClientServiceInternal;
import com.emarsys.mobileengage.deeplink.DeepLinkInternal;
import com.emarsys.mobileengage.event.EventServiceInternal;
import com.emarsys.mobileengage.iam.InAppInternal;
import com.emarsys.mobileengage.iam.InAppPresenter;
import com.emarsys.mobileengage.inbox.InboxInternal;
import com.emarsys.mobileengage.inbox.model.NotificationCache;
import com.emarsys.mobileengage.push.PushInternal;
import com.emarsys.predict.PredictApi;
import com.emarsys.predict.PredictInternal;
import com.emarsys.push.PushApi;


public class FakeDependencyContainer implements EmarysDependencyContainer {
    private final Handler coreSdkHandler;
    private final ActivityLifecycleWatchdog activityLifecycleWatchdog;
    private final CurrentActivityWatchdog currentActivityWatchdog;
    private final CoreSQLiteDatabase coreSQLiteDatabase;
    private final DeviceInfo deviceInfo;
    private final Repository<ShardModel, SqlSpecification> shardRepository;
    private final TimestampProvider timestampProvider;
    private final UUIDProvider uuidProvider;
    private final Runnable logShardTrigger;
    private final MobileEngageInternal mobileEngageInternal;
    private final InboxInternal inboxInternal;
    private final InAppInternal inAppInternal;
    private final DeepLinkInternal deepLinkInternal;
    private final EventServiceInternal eventServiceInternal;
    private final ClientServiceInternal clientServiceInternal;
    private final PushInternal pushInternal;
    private final DefaultCoreCompletionHandler completionHandler;
    private final RequestContext requestContext;
    private final InAppPresenter inAppPresenter;
    private final NotificationEventHandler notificationEventHandler;
    private final PredictInternal predictInternal;
    private final Runnable predictShardTrigger;
    private final RunnerProxy runnerProxy;
    private final Logger logger;
    private final RefreshTokenInternal refreshTokenInternal;
    private final Storage<Integer> deviceInfoHashStorage;
    private final Storage<String> contactFieldValueStorage;
    private final Storage<String> contactTokenStorage;
    private final Storage<String> clientStateStorage;
    private final ResponseHandlersProcessor responseHandlersProcessor;
    private final NotificationCache notificationCache;
    private final RestClient restClient;
    private final InboxApi inbox;
    private final InAppApi inApp;
    private final PushApi push;
    private final PredictApi predict;

    public FakeDependencyContainer(
            Handler coreSdkHandler,
            ActivityLifecycleWatchdog activityLifecycleWatchdog,
            CurrentActivityWatchdog currentActivityWatchdog,
            CoreSQLiteDatabase coreSQLiteDatabase,
            DeviceInfo deviceInfo,
            Repository<ShardModel, SqlSpecification> shardRepository,
            TimestampProvider timestampProvider,
            UUIDProvider uuidProvider,
            Runnable logShardTrigger,
            MobileEngageInternal mobileEngageInternal,
            PushInternal pushInternal, InboxInternal inboxInternal,
            InAppInternal inAppInternal,
            RefreshTokenInternal refreshTokenInternal,
            DeepLinkInternal deepLinkInternal,
            EventServiceInternal eventServiceInternal,
            ClientServiceInternal clientServiceInternal,
            DefaultCoreCompletionHandler completionHandler,
            RequestContext requestContext,
            InAppPresenter inAppPresenter,
            NotificationEventHandler notificationEventHandler,
            PredictInternal predictInternal,
            Runnable predictShardTrigger,
            RunnerProxy runnerProxy,
            Logger logger,
            Storage<Integer> deviceInfoHashStorage,
            Storage<String> contactFieldValueStorage,
            Storage<String> contactTokenStorage, Storage<String> clientStateStorage,
            ResponseHandlersProcessor responseHandlersProcessor,
            NotificationCache notificationCache,
            RestClient restClient,
            InboxApi inbox,
            InAppApi inApp,
            PushApi push,
            PredictApi predict) {
        this.coreSdkHandler = coreSdkHandler;
        this.activityLifecycleWatchdog = activityLifecycleWatchdog;
        this.currentActivityWatchdog = currentActivityWatchdog;
        this.coreSQLiteDatabase = coreSQLiteDatabase;
        this.deviceInfo = deviceInfo;
        this.shardRepository = shardRepository;
        this.timestampProvider = timestampProvider;
        this.uuidProvider = uuidProvider;
        this.logShardTrigger = logShardTrigger;
        this.mobileEngageInternal = mobileEngageInternal;
        this.inboxInternal = inboxInternal;
        this.inAppInternal = inAppInternal;
        this.deepLinkInternal = deepLinkInternal;
        this.completionHandler = completionHandler;
        this.requestContext = requestContext;
        this.inAppPresenter = inAppPresenter;
        this.notificationEventHandler = notificationEventHandler;
        this.predictInternal = predictInternal;
        this.predictShardTrigger = predictShardTrigger;
        this.runnerProxy = runnerProxy;
        this.logger = logger;
        this.refreshTokenInternal = refreshTokenInternal;
        this.deviceInfoHashStorage = deviceInfoHashStorage;
        this.contactFieldValueStorage = contactFieldValueStorage;
        this.contactTokenStorage = contactTokenStorage;
        this.clientStateStorage = clientStateStorage;
        this.responseHandlersProcessor = responseHandlersProcessor;
        this.notificationCache = notificationCache;
        this.restClient = restClient;
        this.inbox = inbox;
        this.inApp = inApp;
        this.push = push;
        this.predict = predict;
        this.eventServiceInternal = eventServiceInternal;
        this.clientServiceInternal = clientServiceInternal;
        this.pushInternal = pushInternal;
    }

    @Override
    public Handler getCoreSdkHandler() {
        return coreSdkHandler;
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
        return coreSQLiteDatabase;
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    @Override
    public Repository<ShardModel, SqlSpecification> getShardRepository() {
        return shardRepository;
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
    public PredictInternal getPredictInternal() {
        return predictInternal;
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
    public DeepLinkInternal getDeepLinkInternal() {
        return deepLinkInternal;
    }

    @Override
    public DefaultCoreCompletionHandler getCoreCompletionHandler() {
        return completionHandler;
    }

    @Override
    public RequestContext getRequestContext() {
        return requestContext;
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
    public Runnable getPredictShardTrigger() {
        return predictShardTrigger;
    }

    @Override
    public RunnerProxy getRunnerProxy() {
        return runnerProxy;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public RestClient getRestClient() {
        return restClient;
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
    public ResponseHandlersProcessor getResponseHandlersProcessor() {
        return responseHandlersProcessor;
    }

    @Override
    public NotificationCache getNotificationCache() {
        return notificationCache;
    }


    @Override
    public InboxApi getInbox() {
        return inbox;
    }

    @Override
    public InAppApi getInApp() {
        return inApp;
    }

    @Override
    public PushApi getPush() {
        return push;
    }

    @Override
    public PredictApi getPredict() {
        return predict;
    }
}
