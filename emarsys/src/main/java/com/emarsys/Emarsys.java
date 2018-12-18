package com.emarsys;

import android.app.Activity;
import android.content.Intent;

import com.emarsys.config.EmarsysConfig;
import com.emarsys.core.api.experimental.FlipperFeature;
import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.api.result.ResultListener;
import com.emarsys.core.api.result.Try;
import com.emarsys.core.database.DatabaseContract;
import com.emarsys.core.database.trigger.TriggerEvent;
import com.emarsys.core.database.trigger.TriggerType;
import com.emarsys.core.di.DependencyInjection;
import com.emarsys.core.experimental.ExperimentalFeatures;
import com.emarsys.core.util.Assert;
import com.emarsys.di.DefaultEmarsysDependencyContainer;
import com.emarsys.di.EmarysDependencyContainer;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.api.EventHandler;
import com.emarsys.mobileengage.api.inbox.Notification;
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus;
import com.emarsys.mobileengage.deeplink.DeepLinkInternal;
import com.emarsys.mobileengage.iam.InAppInternal;
import com.emarsys.mobileengage.inbox.InboxInternal;
import com.emarsys.predict.PredictInternal;
import com.emarsys.predict.api.model.CartItem;

import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Emarsys {

    public static void setup(@NonNull EmarsysConfig config) {
        Assert.notNull(config, "Config must not be null!");

        for (FlipperFeature feature : config.getExperimentalFeatures()) {
            ExperimentalFeatures.enableFeature(feature);
        }

        DependencyInjection.setup(new DefaultEmarsysDependencyContainer(config));

        initializeInAppInternal(config);

        registerWatchDogs(config);

        registerDatabaseTriggers();
    }

    public static void setCustomer(@NonNull String customerId) {
        setCustomer(customerId, null);
    }

    public static void setCustomer(
            @NonNull String customerId,
            @NonNull CompletionListener completionListener) {
        Assert.notNull(customerId, "CustomerId must not be null!");

        getMobileEngageInternal().appLogin(customerId, completionListener);
        getPredictInternal().setCustomer(customerId);
    }

    public static void setAnonymousCustomer() {
        setAnonymousCustomer(null);
    }

    public static void setAnonymousCustomer(@NonNull CompletionListener completionListener) {
        getMobileEngageInternal().appLogin(completionListener);
    }

    public static void clearCustomer() {
        clearCustomer(null);
    }

    public static void clearCustomer(@NonNull CompletionListener completionListener) {
        getMobileEngageInternal().appLogout(completionListener);
        getPredictInternal().clearCustomer();
    }

    public static void trackDeepLink(@NonNull Activity activity, @NonNull Intent intent) {
        trackDeepLink(activity, intent, null);
    }

    public static void trackDeepLink(@NonNull Activity activity, @NonNull Intent intent, CompletionListener completionListener) {
        Assert.notNull(activity, "Activity must not be null!");
        Assert.notNull(intent, "Intent must not be null!");

        getDeepLinkInternal().trackDeepLinkOpen(activity, intent, completionListener);
    }

    public static void trackCustomEvent(
            @NonNull String eventName,
            @Nullable Map<String, String> eventAttributes) {
        trackCustomEvent(eventName, eventAttributes, null);
    }

    public static void trackCustomEvent(
            @NonNull String eventName,
            @Nullable Map<String, String> eventAttributes,
            @NonNull CompletionListener completionListener) {
        Assert.notNull(eventName, "EventName must not be null!");

        getMobileEngageInternal().trackCustomEvent(eventName, eventAttributes, completionListener);
    }

    public static class Push {

        public static void trackMessageOpen(@NonNull Intent intent) {
            trackMessageOpen(intent, null);
        }

        public static void trackMessageOpen(
                @NonNull Intent intent,
                @NonNull CompletionListener completionListener) {
            Assert.notNull(intent, "Intent must not be null!");

            getMobileEngageInternal().trackMessageOpen(intent, completionListener);
        }

        public static void setPushToken(@NonNull String pushToken) {
            Assert.notNull(pushToken, "PushToken must not be null!");

            getMobileEngageInternal().setPushToken(pushToken);
        }
    }

    public static class Inbox {

        public static void fetchNotifications(
                @NonNull ResultListener<Try<NotificationInboxStatus>> resultListener) {
            Assert.notNull(resultListener, "ResultListener must not be null!");
            getInboxInternal().fetchNotifications(resultListener);
        }

        public static void trackNotificationOpen(@NonNull Notification notification) {
            Assert.notNull(notification, "Notification must not be null!");
            getInboxInternal().trackNotificationOpen(notification, null);
        }

        public static void trackNotificationOpen(
                @NonNull Notification message,
                @NonNull CompletionListener resultListener) {
            Assert.notNull(message, "Message must not be null!");
            Assert.notNull(resultListener, "ResultListener must not be null!");
            getInboxInternal().trackNotificationOpen(message, resultListener);
        }

        public static void resetBadgeCount() {
            getInboxInternal().resetBadgeCount(null);
        }

        public static void resetBadgeCount(@NonNull CompletionListener resultListener) {
            Assert.notNull(resultListener, "ResultListener must not be null!");
            getInboxInternal().resetBadgeCount(resultListener);
        }

        public static void purgeNotificationCache() {
            getInboxInternal().purgeNotificationCache();
        }
    }

    public static class InApp {

        public static void pause() {
            getInAppInternal().pause();
        }

        public static void resume() {
            getInAppInternal().resume();
        }

        public static boolean isPaused() {
            return getInAppInternal().isPaused();
        }

        public static void setEventHandler(@NonNull EventHandler eventHandler) {
            Assert.notNull(eventHandler, "EventHandler must not be null!");

            getInAppInternal().setEventHandler(eventHandler);
        }
    }

    public static class Predict {

        public static void trackCart(@NonNull List<CartItem> items) {
            Assert.notNull(items, "Items must not be null!");
            Assert.elementsNotNull(items, "Item elements must not be null!");

            getPredictInternal().trackCart(items);
        }

        public static void trackPurchase(@NonNull String orderId, @NonNull List<CartItem> items) {
            Assert.notNull(orderId, "OrderId must not be null!");
            Assert.notNull(items, "Items must not be null!");
            Assert.elementsNotNull(items, "Item elements must not be null!");

            getPredictInternal().trackPurchase(orderId, items);
        }

        public static void trackItemView(@NonNull String itemId) {
            Assert.notNull(itemId, "ItemId must not be null!");

            getPredictInternal().trackItemView(itemId);
        }

        public static void trackCategoryView(@NonNull String categoryPath) {
            Assert.notNull(categoryPath, "CategoryPath must not be null!");

            getPredictInternal().trackCategoryView(categoryPath);
        }

        public static void trackSearchTerm(@NonNull String searchTerm) {
            Assert.notNull(searchTerm, "SearchTerm must not be null!");

            getPredictInternal().trackSearchTerm(searchTerm);
        }
    }

    private static EmarysDependencyContainer getContainer() {
        return DependencyInjection.getContainer();
    }

    private static MobileEngageInternal getMobileEngageInternal() {
        return getContainer().getMobileEngageInternal();
    }

    private static InboxInternal getInboxInternal() {
        return getContainer().getInboxInternal();
    }

    private static InAppInternal getInAppInternal() {
        return getContainer().getInAppInternal();
    }

    private static DeepLinkInternal getDeepLinkInternal() {
        return getContainer().getDeepLinkInternal();
    }

    private static PredictInternal getPredictInternal() {
        return getContainer().getPredictInternal();
    }

    private static void initializeInAppInternal(@NonNull EmarsysConfig config) {
        EventHandler inAppEventHandler = config.getInAppEventHandler();

        if (inAppEventHandler != null) {
            getInAppInternal().setEventHandler(inAppEventHandler);
        }
    }

    private static void registerWatchDogs(EmarsysConfig config) {
        config.getApplication().registerActivityLifecycleCallbacks(getContainer().getActivityLifecycleWatchdog());
        config.getApplication().registerActivityLifecycleCallbacks(getContainer().getCurrentActivityWatchdog());
    }

    private static void registerDatabaseTriggers() {
        getContainer().getCoreSQLiteDatabase().registerTrigger(
                DatabaseContract.SHARD_TABLE_NAME,
                TriggerType.AFTER,
                TriggerEvent.INSERT,
                getContainer().getPredictShardTrigger());

        getContainer().getCoreSQLiteDatabase().registerTrigger(
                DatabaseContract.SHARD_TABLE_NAME,
                TriggerType.AFTER,
                TriggerEvent.INSERT,
                getContainer().getLogShardTrigger());
    }
}