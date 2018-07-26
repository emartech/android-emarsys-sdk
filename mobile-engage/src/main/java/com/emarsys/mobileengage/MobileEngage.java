package com.emarsys.mobileengage;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.emarsys.core.activity.CurrentActivityWatchdog;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.deeplink.DeepLinkInternal;
import com.emarsys.mobileengage.di.DefaultDependencyContainer;
import com.emarsys.mobileengage.di.DependencyContainer;
import com.emarsys.mobileengage.di.DependencyInjection;
import com.emarsys.mobileengage.event.applogin.AppLoginParameters;
import com.emarsys.mobileengage.experimental.FlipperFeature;
import com.emarsys.mobileengage.experimental.MobileEngageExperimental;
import com.emarsys.mobileengage.inbox.InboxInternal;
import com.emarsys.mobileengage.inbox.InboxResultListener;
import com.emarsys.mobileengage.inbox.ResetBadgeCountResultListener;
import com.emarsys.mobileengage.inbox.model.Notification;
import com.emarsys.mobileengage.inbox.model.NotificationInboxStatus;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;

import java.util.Map;

public class MobileEngage {

    static MobileEngageInternal instance;
    static InboxInternal inboxInstance;
    static DeepLinkInternal deepLinkInstance;
    static MobileEngageCoreCompletionHandler completionHandler;
    static Handler coreSdkHandler;
    static MobileEngageConfig config;
    static RequestContext requestContext;
    private static DependencyContainer container;

    public static class Inbox {

        public static void fetchNotifications(@NonNull InboxResultListener<NotificationInboxStatus> resultListener) {
            Assert.notNull(resultListener, "ResultListener must not be null!");
            inboxInstance.fetchNotifications(resultListener);
        }

        public static void resetBadgeCount() {
            resetBadgeCount(null);
        }

        public static void resetBadgeCount(@Nullable ResetBadgeCountResultListener resultListener) {
            inboxInstance.resetBadgeCount(resultListener);
        }

        public static String trackMessageOpen(Notification message) {
            return inboxInstance.trackMessageOpen(message);
        }

        public static void purgeNotificationCache() {
            inboxInstance.purgeNotificationCache();
        }

    }

    public static class InApp {

        private static boolean enabled;

        public static void setPaused(boolean enabled) {
            InApp.enabled = enabled;
        }

        public static boolean isPaused() {
            return InApp.enabled;
        }

    }

    public static void setup(@NonNull MobileEngageConfig config) {
        Assert.notNull(config, "Config must not be null!");
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Argument: %s", config);

        for (FlipperFeature feature : config.getExperimentalFeatures()) {
            MobileEngageExperimental.enableFeature(feature);
        }

        MobileEngage.config = config;
        Application application = config.getApplication();

        DependencyInjection.setup(new DefaultDependencyContainer(config));
        container = DependencyInjection.getContainer();

        initializeFields();

        initializeInApp();

        registerWatchdogs(application);

        MobileEngageUtils.setup(config);
    }

    @NonNull
    public static MobileEngageConfig getConfig() {
        return config;
    }

    public static void setPushToken(@NonNull String pushToken) {
        instance.setPushToken(pushToken);
    }

    public static void setStatusListener(@NonNull MobileEngageStatusListener listener) {
        completionHandler.setStatusListener(listener);
    }

    @NonNull
    public static String appLogin() {
        return instance.appLogin();
    }

    @NonNull
    public static String appLogin(int contactFieldId, @NonNull String contactFieldValue) {
        Assert.notNull(contactFieldValue, "ContactFieldValue must not be null!");
        return instance.appLogin(contactFieldId, contactFieldValue);
    }

    @NonNull
    public static String appLogout() {
        return instance.appLogout();
    }

    @NonNull
    public static String trackCustomEvent(@NonNull String eventName, @Nullable Map<String, String> eventAttributes) {
        Assert.notNull(eventName, "EventName must not be null!");
        return instance.trackCustomEvent(eventName, eventAttributes);
    }

    @NonNull
    public static String trackMessageOpen(@NonNull Intent intent) {
        Assert.notNull(intent, "Intent must not be null!");
        return instance.trackMessageOpen(intent);
    }

    public static void trackDeepLink(@NonNull Activity activity, @NonNull Intent intent) {
        Assert.notNull(activity, "Activity must not be null!");
        Assert.notNull(activity.getIntent(), "Intent from Activity must not be null!");
        Assert.notNull(intent, "Intent must not be null!");
        deepLinkInstance.trackDeepLinkOpen(activity, intent);
    }

    private static void initializeFields() {
        instance = container.getMobileEngageInternal();
        inboxInstance = container.getInboxInternal();
        deepLinkInstance = container.getDeepLinkInternal();
        coreSdkHandler = container.getCoreSdkHandler();
        requestContext = container.getRequestContext();
        completionHandler = container.getCoreCompletionHandler();
    }

    private static void initializeInApp() {
        InApp.setPaused(false);
    }

    private static void registerWatchdogs(Application application) {
        CurrentActivityWatchdog.registerApplication(application);
        application.registerActivityLifecycleCallbacks(container.getActivityLifecycleWatchdog());
    }

}
