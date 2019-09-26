package com.emarsys;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.emarsys.config.ConfigApi;
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
import com.emarsys.di.EmarsysDependencyInjection;
import com.emarsys.di.EmarysDependencyContainer;
import com.emarsys.inapp.InAppApi;
import com.emarsys.inbox.InboxApi;
import com.emarsys.mobileengage.api.EventHandler;
import com.emarsys.mobileengage.api.inbox.Notification;
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus;
import com.emarsys.predict.PredictApi;
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

    private static ConfigApi config;

    public static void setup(@NonNull EmarsysConfig emarsysConfig) {
        Assert.notNull(emarsysConfig, "Config must not be null!");

        for (FlipperFeature feature : emarsysConfig.getExperimentalFeatures()) {
            FeatureRegistry.enableFeature(feature);
        }

        DependencyInjection.setup(new DefaultEmarsysDependencyContainer(emarsysConfig));

        config = getContainer().getConfig();

        initializeApplicationCode(emarsysConfig);
        initializeInAppInternal(emarsysConfig);

        registerWatchDogs(emarsysConfig);

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
                    EmarsysDependencyInjection.mobileEngageInternal().setContact(contactId, null);
                }
                if (FeatureRegistry.isFeatureEnabled(PREDICT)) {
                    EmarsysDependencyInjection.predictInternal().setContact(contactId);
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
                    EmarsysDependencyInjection.mobileEngageInternal().setContact(contactId, completionListener);
                }
                if (FeatureRegistry.isFeatureEnabled(PREDICT)) {
                    EmarsysDependencyInjection.predictInternal().setContact(contactId);
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
                    EmarsysDependencyInjection.mobileEngageInternal().clearContact(null);
                }
                if (FeatureRegistry.isFeatureEnabled(PREDICT)) {
                    EmarsysDependencyInjection.predictInternal().clearContact();
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
                    EmarsysDependencyInjection.mobileEngageInternal().clearContact(completionListener);
                }
                if (FeatureRegistry.isFeatureEnabled(PREDICT)) {
                    EmarsysDependencyInjection.predictInternal().clearContact();
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

                EmarsysDependencyInjection.deepLinkInternal().trackDeepLinkOpen(activity, intent, null);
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

                EmarsysDependencyInjection.deepLinkInternal().trackDeepLinkOpen(activity, intent, completionListener);
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

                EmarsysDependencyInjection.eventServiceInternal().trackCustomEvent(eventName, eventAttributes, null);
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

                EmarsysDependencyInjection.eventServiceInternal().trackCustomEvent(eventName, eventAttributes, completionListener);
            }
        });
    }

    static PushApi getPush() {
        return EmarsysDependencyInjection.push();
    }

    static InAppApi getInApp() {
        return EmarsysDependencyInjection.inApp();
    }

    static InboxApi getInbox() {
        return EmarsysDependencyInjection.inbox();
    }

    static PredictApi getPredict() {
        return EmarsysDependencyInjection.predict();
    }

    public static class Config {

        @NonNull
        public static int getContactFieldId() {
            return config.getContactFieldId();
        }

        public static void changeApplicationCode(@Nullable String applicationCode) {
            config.changeApplicationCode(applicationCode, null);
        }

        public static void changeApplicationCode(@Nullable String applicationCode, @NonNull CompletionListener completionListener) {
            config.changeApplicationCode(applicationCode, completionListener);
        }

        @Nullable
        public static String getApplicationCode() {
            return config.getApplicationCode();
        }

        public static void changeMerchantId(@Nullable String merchantId) {
            config.changeMerchantId(merchantId);
        }

        @Nullable
        public static String getMerchantId() {
            return config.getMerchantId();
        }

    }

    public static class Push {

        public static void trackMessageOpen(@NonNull final Intent intent) {
            getPush().trackMessageOpen(intent);
        }

        public static void trackMessageOpen(
                @NonNull final Intent intent,
                @NonNull final CompletionListener completionListener) {
            getPush().trackMessageOpen(intent, completionListener);
        }

        public static void setPushToken(@NonNull final String pushToken) {
            getPush().setPushToken(pushToken);
        }

        public static void setPushToken(
                @NonNull final String pushToken,
                @NonNull final CompletionListener completionListener) {
            getPush().setPushToken(pushToken, completionListener);
        }

        public static void clearPushToken() {
            getPush().clearPushToken();
        }

        public static void clearPushToken(final CompletionListener completionListener) {
            getPush().clearPushToken(completionListener);
        }
    }

    public static class Predict {

        public static void trackCart(@NonNull final List<CartItem> items) {
            getPredict().trackCart(items);
        }

        public static void trackPurchase(@NonNull final String orderId,
                                         @NonNull final List<CartItem> items) {
            getPredict().trackPurchase(orderId, items);
        }

        public static void trackItemView(@NonNull final String itemId) {
            getPredict().trackItemView(itemId);
        }

        public static void trackCategoryView(@NonNull final String categoryPath) {
            getPredict().trackCategoryView(categoryPath);
        }

        public static void trackSearchTerm(@NonNull final String searchTerm) {
            getPredict().trackSearchTerm(searchTerm);
        }

        public static void trackTag(@NonNull String tag, @Nullable Map<String, String> attributes) {
            getPredict().trackTag(tag, attributes);
        }

        public static void recommendProducts(@NonNull final Logic recommendationLogic, @NonNull final ResultListener<Try<List<Product>>> resultListener) {
            getPredict().recommendProducts(recommendationLogic, resultListener);
        }

        public static void recommendProducts(@NonNull final Logic recommendationLogic, @NonNull final Integer limit, @NonNull final ResultListener<Try<List<Product>>> resultListener) {
            getPredict().recommendProducts(recommendationLogic, limit, resultListener);
        }

        public static void recommendProducts(@NonNull final Logic recommendationLogic, @NonNull final List<RecommendationFilter> recommendationFilters, @NonNull ResultListener<Try<List<Product>>> resultListener) {
            getPredict().recommendProducts(recommendationLogic, recommendationFilters, resultListener);
        }

        public static void recommendProducts(@NonNull final Logic recommendationLogic, @NonNull final List<RecommendationFilter> recommendationFilters, @NonNull final Integer limit, @NonNull ResultListener<Try<List<Product>>> resultListener) {
            getPredict().recommendProducts(recommendationLogic, limit, recommendationFilters, resultListener);
        }

        public static void trackRecommendationClick(@NonNull final Product product) {
            getPredict().trackRecommendationClick(product);
        }
    }

    public static class InApp {

        public static void pause() {
            getInApp().pause();
        }

        public static void resume() {
            getInApp().resume();
        }

        public static boolean isPaused() {
            return getInApp().isPaused();
        }

        public static void setEventHandler(@NonNull EventHandler eventHandler) {
            getInApp().setEventHandler(eventHandler);
        }
    }

    public static class Inbox {

        public static void fetchNotifications(@NonNull ResultListener<Try<NotificationInboxStatus>> resultListener) {
            getInbox().fetchNotifications(resultListener);
        }


        public static void trackNotificationOpen(@NonNull Notification notification) {
            getInbox().trackNotificationOpen(notification);
        }

        public static void trackNotificationOpen(@NonNull Notification notification, @NonNull CompletionListener completionListener) {
            getInbox().trackNotificationOpen(notification, completionListener);
        }

        public static void resetBadgeCount() {
            getInbox().resetBadgeCount();
        }

        public static void resetBadgeCount(@NonNull CompletionListener completionListener) {
            getInbox().resetBadgeCount(completionListener);
        }
    }

    private static EmarysDependencyContainer getContainer() {
        return DependencyInjection.getContainer();
    }

    private static RunnerProxy getRunnerProxy() {
        return getContainer().getRunnerProxy();
    }

    private static void initializeInAppInternal(@NonNull EmarsysConfig config) {
        EventHandler inAppEventHandler = config.getInAppEventHandler();

        if (inAppEventHandler != null) {
            getInApp().setEventHandler(inAppEventHandler);
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
                EmarsysDependencyInjection.clientServiceInternal().trackDeviceInfo();
            }
            EmarsysDependencyInjection.mobileEngageInternal().setContact(null, null);
        }
    }

    private static void initializeApplicationCode(EmarsysConfig emarsysConfig) {
        getContainer().getRequestContext().getApplicationCodeStorage().set(emarsysConfig.getMobileEngageApplicationCode());
    }
}