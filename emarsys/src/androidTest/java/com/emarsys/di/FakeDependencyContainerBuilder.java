package com.emarsys.di;

import android.os.Handler;

import com.emarsys.core.activity.ActivityLifecycleWatchdog;
import com.emarsys.core.activity.CurrentActivityWatchdog;
import com.emarsys.core.database.CoreSQLiteDatabase;
import com.emarsys.mobileengage.MobileEngageCoreCompletionHandler;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.api.NotificationEventHandler;
import com.emarsys.mobileengage.config.OreoConfig;
import com.emarsys.mobileengage.deeplink.DeepLinkInternal;
import com.emarsys.mobileengage.iam.InAppPresenter;
import com.emarsys.mobileengage.inbox.InboxInternal;
import com.emarsys.predict.PredictInternal;

public class FakeDependencyContainerBuilder {
    private Handler coreSdkHandler;
    private ActivityLifecycleWatchdog activityLifecycleWatchdog;
    private CurrentActivityWatchdog currentActivityWatchdog;
    private CoreSQLiteDatabase coreSQLiteDatabase;
    private MobileEngageInternal mobileEngageInternal;
    private InboxInternal inboxInternal;
    private DeepLinkInternal deepLinkInternal;
    private MobileEngageCoreCompletionHandler completionHandler;
    private RequestContext requestContext;
    private InAppPresenter inAppPresenter;
    private NotificationEventHandler notificationEventHandler;
    private OreoConfig oreoConfig;
    private PredictInternal predictInternal;
    private Runnable predictShardTrigger;

    public FakeDependencyContainerBuilder setCoreSdkHandler(Handler coreSdkHandler) {
        this.coreSdkHandler = coreSdkHandler;
        return this;
    }

    public FakeDependencyContainerBuilder setActivityLifecycleWatchdog(ActivityLifecycleWatchdog activityLifecycleWatchdog) {
        this.activityLifecycleWatchdog = activityLifecycleWatchdog;
        return this;
    }

    public FakeDependencyContainerBuilder setCurrentActivityWatchdog(CurrentActivityWatchdog currentActivityWatchdog) {
        this.currentActivityWatchdog = currentActivityWatchdog;
        return this;
    }

    public FakeDependencyContainerBuilder setCoreSQLiteDatabase(CoreSQLiteDatabase coreSQLiteDatabase) {
        this.coreSQLiteDatabase = coreSQLiteDatabase;
        return this;
    }

    public FakeDependencyContainerBuilder setMobileEngageInternal(MobileEngageInternal mobileEngageInternal) {
        this.mobileEngageInternal = mobileEngageInternal;
        return this;
    }

    public FakeDependencyContainerBuilder setInboxInternal(InboxInternal inboxInternal) {
        this.inboxInternal = inboxInternal;
        return this;
    }

    public FakeDependencyContainerBuilder setDeepLinkInternal(DeepLinkInternal deepLinkInternal) {
        this.deepLinkInternal = deepLinkInternal;
        return this;
    }

    public FakeDependencyContainerBuilder setCompletionHandler(MobileEngageCoreCompletionHandler completionHandler) {
        this.completionHandler = completionHandler;
        return this;
    }

    public FakeDependencyContainerBuilder setRequestContext(RequestContext requestContext) {
        this.requestContext = requestContext;
        return this;
    }

    public FakeDependencyContainerBuilder setInAppPresenter(InAppPresenter inAppPresenter) {
        this.inAppPresenter = inAppPresenter;
        return this;
    }

    public FakeDependencyContainerBuilder setNotificationEventHandler(NotificationEventHandler notificationEventHandler) {
        this.notificationEventHandler = notificationEventHandler;
        return this;
    }

    public FakeDependencyContainerBuilder setOreoConfig(OreoConfig oreoConfig) {
        this.oreoConfig = oreoConfig;
        return this;
    }

    public FakeDependencyContainerBuilder setPredictInternal(PredictInternal predictInternal) {
        this.predictInternal = predictInternal;
        return this;
    }

    public FakeDependencyContainerBuilder setPredictShardTrigger(Runnable predictShardTrigger) {
        this.predictShardTrigger = predictShardTrigger;
        return this;
    }

    public FakeDependencyContainer build() {
        return new FakeDependencyContainer(coreSdkHandler, activityLifecycleWatchdog, currentActivityWatchdog, coreSQLiteDatabase, mobileEngageInternal, inboxInternal, deepLinkInternal, completionHandler, requestContext, inAppPresenter, notificationEventHandler, oreoConfig, predictInternal, predictShardTrigger);
    }
}