package com.emarsys;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.emarsys.config.EmarsysConfig;
import com.emarsys.core.RunnerProxy;
import com.emarsys.core.api.experimental.FlipperFeature;
import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.api.result.ResultListener;
import com.emarsys.core.api.result.Try;
import com.emarsys.core.database.DatabaseContract;
import com.emarsys.core.database.trigger.TriggerEvent;
import com.emarsys.core.database.trigger.TriggerType;
import com.emarsys.core.device.DeviceInfo;
import com.emarsys.core.di.DependencyInjection;
import com.emarsys.core.feature.FeatureRegistry;
import com.emarsys.core.util.Assert;
import com.emarsys.di.DefaultEmarsysDependencyContainer;
import com.emarsys.di.EmarysDependencyContainer;
import com.emarsys.inapp.InAppApi;
import com.emarsys.inbox.InboxApi;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.api.EventHandler;
import com.emarsys.mobileengage.api.inbox.Notification;
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus;
import com.emarsys.mobileengage.client.ClientServiceInternal;
import com.emarsys.mobileengage.deeplink.DeepLinkInternal;
import com.emarsys.mobileengage.event.EventServiceInternal;
import com.emarsys.predict.PredictApi;
import com.emarsys.predict.PredictInternal;
import com.emarsys.predict.api.model.CartItem;
import com.emarsys.predict.api.model.Logic;
import com.emarsys.predict.api.model.Product;
import com.emarsys.predict.api.model.RecommendationFilter;
import com.emarsys.push.PushApi;

import java.util.List;
import java.util.Map;

import static com.emarsys.feature.InnerFeature.MOBILE_ENGAGE;
import static com.emarsys.feature.InnerFeature.PREDICT;

public class Emarsys {

    private static PushApi push;
    private static InAppApi inApp;
    private static PredictApi predict;
    private static InboxApi inbox;

    public static void setup(@NonNull EmarsysConfig config) {
        Assert.notNull(config, "Config must not be null!");

        for (FlipperFeature feature : config.getExperimentalFeatures()) {
            FeatureRegistry.enableFeature(feature);
        }

        DependencyInjection.setup(new DefaultEmarsysDependencyContainer(config));

        inbox = getContainer().getInbox();
        inApp = getContainer().getInApp();
        push = getContainer().getPush();
        predict = getContainer().getPredict();

        initializeInAppInternal(config);

        registerWatchDogs(config);

        registerDatabaseTriggers();

        initializeContact();
    }

    public static void setContact(@NonNull final String contactId) {
        getRunnerProxy().logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(contactId, "ContactId must not be null!");

                if (FeatureRegistry.isFeatureEnabled(MOBILE_ENGAGE) ||
                        (!FeatureRegistry.isFeatureEnabled(MOBILE_ENGAGE) && !FeatureRegistry.isFeatureEnabled(PREDICT))) {
                    getMobileEngageInternal().setContact(contactId, null);
                }
                if (FeatureRegistry.isFeatureEnabled(PREDICT)) {
                    getPredictInternal().setContact(contactId);
                }
            }
        });

    }

    public static void setContact(
            @NonNull final String contactId,
            @NonNull final CompletionListener completionListener) {
        getRunnerProxy().logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(contactId, "ContactId must not be null!");
                Assert.notNull(completionListener, "CompletionListener must not be null!");

                if (FeatureRegistry.isFeatureEnabled(MOBILE_ENGAGE) ||
                        (!FeatureRegistry.isFeatureEnabled(MOBILE_ENGAGE) && !FeatureRegistry.isFeatureEnabled(PREDICT))) {
                    getMobileEngageInternal().setContact(contactId, completionListener);
                }
                if (FeatureRegistry.isFeatureEnabled(PREDICT)) {
                    getPredictInternal().setContact(contactId);
                }
            }
        });
    }

    public static void clearContact() {
        getRunnerProxy().logException(new Runnable() {
            @Override
            public void run() {
                if (FeatureRegistry.isFeatureEnabled(MOBILE_ENGAGE) ||
                        (!FeatureRegistry.isFeatureEnabled(MOBILE_ENGAGE) && !FeatureRegistry.isFeatureEnabled(PREDICT))) {
                    getMobileEngageInternal().clearContact(null);
                }
                if (FeatureRegistry.isFeatureEnabled(PREDICT)) {
                    getPredictInternal().clearContact();
                }
            }
        });
    }

    public static void clearContact(@NonNull final CompletionListener completionListener) {
        getRunnerProxy().logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(completionListener, "CompletionListener must not be null!");

                if (FeatureRegistry.isFeatureEnabled(MOBILE_ENGAGE) ||
                        (!FeatureRegistry.isFeatureEnabled(MOBILE_ENGAGE) && !FeatureRegistry.isFeatureEnabled(PREDICT))) {
                    getMobileEngageInternal().clearContact(completionListener);
                }
                if (FeatureRegistry.isFeatureEnabled(PREDICT)) {
                    getPredictInternal().clearContact();
                }
            }
        });

    }

    public static void trackDeepLink(@NonNull final Activity activity,
                                     @NonNull final Intent intent) {
        getRunnerProxy().logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(activity, "Activity must not be null!");
                Assert.notNull(intent, "Intent must not be null!");

                getDeepLinkInternal().trackDeepLinkOpen(activity, intent, null);
            }
        });
    }

    public static void trackDeepLink(@NonNull final Activity activity,
                                     @NonNull final Intent intent,
                                     @NonNull final CompletionListener completionListener) {
        getRunnerProxy().logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(activity, "Activity must not be null!");
                Assert.notNull(intent, "Intent must not be null!");
                Assert.notNull(completionListener, "CompletionListener must not be null!");

                getDeepLinkInternal().trackDeepLinkOpen(activity, intent, completionListener);
            }
        });
    }

    public static void trackCustomEvent(
            @NonNull final String eventName,
            @Nullable final Map<String, String> eventAttributes) {
        getRunnerProxy().logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(eventName, "EventName must not be null!");

                getEventServiceInternal().trackCustomEvent(eventName, eventAttributes, null);
            }
        });
    }

    public static void trackCustomEvent(
            @NonNull final String eventName,
            @Nullable final Map<String, String> eventAttributes,
            @NonNull final CompletionListener completionListener) {
        getRunnerProxy().logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(eventName, "EventName must not be null!");
                Assert.notNull(completionListener, "CompletionListener must not be null!");

                getEventServiceInternal().trackCustomEvent(eventName, eventAttributes, completionListener);
            }
        });
    }

    static PushApi getPush() {
        return push;
    }

    static InAppApi getInApp() {
        return inApp;
    }

    static InboxApi getInbox() {
        return inbox;
    }

    static PredictApi getPredict() {
        return predict;
    }

    public static class Push {

        public static void trackMessageOpen(@NonNull final Intent intent) {
            push.trackMessageOpen(intent);
        }

        public static void trackMessageOpen(
                @NonNull final Intent intent,
                @NonNull final CompletionListener completionListener) {
            push.trackMessageOpen(intent, completionListener);
        }

        public static void setPushToken(@NonNull final String pushToken) {
            push.setPushToken(pushToken);
        }

        public static void setPushToken(
                @NonNull final String pushToken,
                @NonNull final CompletionListener completionListener) {
            push.setPushToken(pushToken, completionListener);
        }

        public static void clearPushToken() {
            push.clearPushToken();
        }

        public static void clearPushToken(final CompletionListener completionListener) {
            push.clearPushToken(completionListener);
        }
    }

    public static class Predict {

        public static void trackCart(@NonNull final List<CartItem> items) {
            predict.trackCart(items);
        }

        public static void trackPurchase(@NonNull final String orderId,
                                         @NonNull final List<CartItem> items) {
            predict.trackPurchase(orderId, items);
        }

        public static void trackItemView(@NonNull final String itemId) {
            predict.trackItemView(itemId);
        }

        public static void trackCategoryView(@NonNull final String categoryPath) {
            predict.trackCategoryView(categoryPath);
        }

        public static void trackSearchTerm(@NonNull final String searchTerm) {
            predict.trackSearchTerm(searchTerm);
        }

        public static void recommendProducts(@NonNull final Logic recommendationLogic, @NonNull final ResultListener<Try<List<Product>>> resultListener) {
            predict.recommendProducts(recommendationLogic, resultListener);
        }

        public static void recommendProducts(@NonNull final Logic recommendationLogic, @NonNull final Integer limit, @NonNull final ResultListener<Try<List<Product>>> resultListener) {
            predict.recommendProducts(recommendationLogic, limit, resultListener);
        }

        public static void recommendProducts(@NonNull final Logic recommendationLogic, @NonNull final List<RecommendationFilter> recommendationFilters, @NonNull ResultListener<Try<List<Product>>> resultListener) {
            predict.recommendProducts(recommendationLogic, recommendationFilters, resultListener);
        }

        public static void recommendProducts(@NonNull final Logic recommendationLogic, @NonNull final Integer limit, @NonNull final List<RecommendationFilter> recommendationFilters, @NonNull ResultListener<Try<List<Product>>> resultListener) {
            predict.recommendProducts(recommendationLogic, limit, recommendationFilters, resultListener);
        }
    }

    public static class InApp {

        public static void pause() {
            inApp.pause();
        }

        public static void resume() {
            inApp.resume();
        }

        public static boolean isPaused() {
            return inApp.isPaused();
        }

        public static void setEventHandler(@NonNull EventHandler eventHandler) {
            inApp.setEventHandler(eventHandler);
        }
    }

    public static class Inbox {

        public static void fetchNotifications(@NonNull ResultListener<Try<NotificationInboxStatus>> resultListener) {
            inbox.fetchNotifications(resultListener);
        }


        public static void trackNotificationOpen(@NonNull Notification notification) {
            inbox.trackNotificationOpen(notification);
        }

        public static void trackNotificationOpen(@NonNull Notification notification, @NonNull CompletionListener completionListener) {
            inbox.trackNotificationOpen(notification, completionListener);
        }

        public static void resetBadgeCount() {
            inbox.resetBadgeCount();
        }

        public static void resetBadgeCount(@NonNull CompletionListener completionListener) {
            inbox.resetBadgeCount(completionListener);
        }
    }

    private static EmarysDependencyContainer getContainer() {
        return DependencyInjection.getContainer();
    }

    private static MobileEngageInternal getMobileEngageInternal() {
        return getContainer().getMobileEngageInternal();
    }

    private static ClientServiceInternal getClientServiceInternal() {
        return getContainer().getClientServiceInternal();
    }

    private static EventServiceInternal getEventServiceInternal() {
        return getContainer().getEventServiceInternal();
    }

    private static DeepLinkInternal getDeepLinkInternal() {
        return getContainer().getDeepLinkInternal();
    }

    private static PredictInternal getPredictInternal() {
        return getContainer().getPredictInternal();
    }

    private static RunnerProxy getRunnerProxy() {
        return getContainer().getRunnerProxy();
    }

    private static void initializeInAppInternal(@NonNull EmarsysConfig config) {
        EventHandler inAppEventHandler = config.getInAppEventHandler();

        if (inAppEventHandler != null) {
            inApp.setEventHandler(inAppEventHandler);
        }
    }

    private static void registerWatchDogs(EmarsysConfig config) {
        config.getApplication().registerActivityLifecycleCallbacks(getContainer().getActivityLifecycleWatchdog());
        config.getApplication().registerActivityLifecycleCallbacks(getContainer().getCurrentActivityWatchdog());
    }

    private static void registerDatabaseTriggers() {
        if (FeatureRegistry.isFeatureEnabled(PREDICT)) {
            getContainer().getCoreSQLiteDatabase().registerTrigger(
                    DatabaseContract.SHARD_TABLE_NAME,
                    TriggerType.AFTER,
                    TriggerEvent.INSERT,
                    getContainer().getPredictShardTrigger());
        }

        getContainer().getCoreSQLiteDatabase().registerTrigger(
                DatabaseContract.SHARD_TABLE_NAME,
                TriggerType.AFTER,
                TriggerEvent.INSERT,
                getContainer().getLogShardTrigger());
    }

    private static void initializeContact() {
        Integer deviceInfoHash = getContainer().getDeviceInfoHashStorage().get();
        String contactToken = getContainer().getContactTokenStorage().get();
        String contactFieldValue = getContainer().getContactFieldValueStorage().get();
        String clientState = getContainer().getClientStateStorage().get();
        DeviceInfo deviceInfo = getContainer().getDeviceInfo();

        if (contactToken == null && contactFieldValue == null) {
            if (clientState == null || deviceInfoHash != null && !deviceInfoHash.equals(deviceInfo.getHash())) {
                getClientServiceInternal().trackDeviceInfo();
            }
            getMobileEngageInternal().setContact(null, null);
        }
    }
}