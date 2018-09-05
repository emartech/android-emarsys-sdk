package com.emarsys;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.emarsys.config.EmarsysConfig;
import com.emarsys.core.util.Assert;
import com.emarsys.di.DefaultDependencyContainer;
import com.emarsys.di.DependencyContainer;
import com.emarsys.di.DependencyInjection;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.api.EventHandler;
import com.emarsys.mobileengage.api.inbox.Notification;
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus;
import com.emarsys.predict.PredictInternal;
import com.emarsys.predict.api.model.CartItem;
import com.emarsys.result.CompletionListener;
import com.emarsys.result.ResultListener;
import com.emarsys.result.Try;

import java.util.List;
import java.util.Map;

public class Emarsys {

    private static MobileEngageInternal mobileEngageInternal;
    private static PredictInternal predictInternal;
    private static DependencyContainer container;

    public static void setup(@NonNull EmarsysConfig config) {
        Assert.notNull(config, "Config must not be null!");

        DependencyInjection.setup(new DefaultDependencyContainer(config));
        container = DependencyInjection.getContainer();
        initializeFields();
    }

    public static void setCustomer(@NonNull String customerId) {
        Assert.notNull(customerId, "CustomerId must not be null!");

        mobileEngageInternal.appLogin(3, customerId);
        predictInternal.setCustomer(customerId);
    }

    public static void setCustomer(
            @NonNull String customerId,
            @NonNull CompletionListener resultListener) {
    }

    public static void clearCustomer() {

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

        }
    }

    public static class Inbox {

        public static void fetchNotifications(
                @NonNull ResultListener<Try<NotificationInboxStatus>> resultListener) {
        }

        public static String trackNotificationOpen(@NonNull Notification message) {
            return null;
        }

        public static String trackNotificationOpen(
                @NonNull Notification message,
                @NonNull CompletionListener resultListener) {
            return null;
        }

        public static void resetBadgeCount() {
        }

        public static void resetBadgeCount(@NonNull CompletionListener resultListener) {
        }

        public static void purgeNotificationCache() {

        }

    }

    public static class InApp {

        public static void pause() {
        }

        public static void resume() {
        }

        public static boolean isPaused() {
            return false;
        }

        public static void setEventHandler(@NonNull EventHandler eventHandler) {
        }

    }

    public static class Predict {

        public static void trackCart(@NonNull List<CartItem> items) {
        }

        public static void trackPurchase(@NonNull String orderId, @NonNull List<CartItem> items) {
        }

        public static void trackItemView(@NonNull String itemId) {
        }

        public static void trackCategoryView(@NonNull String categoryPath) {
        }

        public static void trackSearchTerm(@NonNull String term) {
        }

    }

    private static void initializeFields() {
        mobileEngageInternal = container.getMobileEngageInternal();
        predictInternal = container.getPredictInternal();
    }

}