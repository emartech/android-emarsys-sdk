package com.emarsys;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
import com.emarsys.mobileengage.inbox.InboxInternal;
import com.emarsys.predict.PredictInternal;
import com.emarsys.predict.api.model.CartItem;

import java.util.List;
import java.util.Map;

public class Emarsys {

    public static void setup(@NonNull EmarsysConfig config) {
        Assert.notNull(config, "Config must not be null!");

        for (FlipperFeature feature : config.getExperimentalFeatures()) {
            ExperimentalFeatures.enableFeature(feature);
        }
        InApp.resume();

        DependencyInjection.setup(new DefaultEmarsysDependencyContainer(config));

        getContainer().getCoreSQLiteDatabase().registerTrigger(
                DatabaseContract.SHARD_TABLE_NAME,
                TriggerType.AFTER,
                TriggerEvent.INSERT,
                getContainer().getPredictShardTrigger());
    }

    public static void setCustomer(@NonNull String customerId) {
        Assert.notNull(customerId, "CustomerId must not be null!");

        getMobileEngageInternal().appLogin(customerId);
        getPredictInternal().setCustomer(customerId);
    }

    public static void setCustomer(
            @NonNull String customerId,
            @NonNull CompletionListener resultListener) {
    }

    public static void clearCustomer() {
        getMobileEngageInternal().appLogout();
        getPredictInternal().clearCustomer();
    }

    public static void clearCustomer(@NonNull CompletionListener resultListener) {
    }

    public static void trackDeepLink(@NonNull Activity activity, @NonNull Intent intent) {

    }

    public static void trackCustomEvent(
            @NonNull String eventName,
            @Nullable Map<String, String> eventAttributes) {
        Assert.notNull(eventName, "EventName must not be null!");
    }

    public static void trackCustomEvent(
            @NonNull String eventName,
            @Nullable Map<String, String> eventAttributes,
            @NonNull CompletionListener resultListener) {
    }

    public static class Push {

        public static void trackMessageOpen(@NonNull Intent intent) {
        }

        public static void trackMessageOpen(
                @NonNull Intent intent,
                @NonNull CompletionListener resultListener) {
        }

        public static void setPushToken(@NonNull String pushToken) {
            getMobileEngageInternal().setPushToken(pushToken);
        }
    }

    public static class Inbox {

        public static void fetchNotifications(
                @NonNull ResultListener<Try<NotificationInboxStatus>> resultListener) {
            Assert.notNull(resultListener, "ResultListener must not be null!");
            getInboxInternal().fetchNotifications(resultListener);
        }

        public static void trackNotificationOpen(@NonNull Notification message) {
            Assert.notNull(message, "Message must not be null!");
            getInboxInternal().trackNotificationOpen(message, null);
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

        private static boolean enabled;

        public static void pause() {
            InApp.enabled = false;
        }

        public static void resume() {
            InApp.enabled = true;
        }

        public static boolean isPaused() {
            return !InApp.enabled;
        }

        public static void setEventHandler(@NonNull EventHandler eventHandler) {
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

    private static PredictInternal getPredictInternal() {
        return getContainer().getPredictInternal();
    }
}