package com.emarsys.config;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.emarsys.core.api.experimental.FlipperFeature;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.api.EventHandler;
import com.emarsys.mobileengage.api.NotificationEventHandler;

import java.util.Arrays;
import java.util.Objects;

public class EmarsysConfig {

    private final Application application;
    private final String mobileEngageApplicationCode;
    private final int contactFieldId;
    private final String predictMerchantId;
    private final EventHandler inAppEventHandler;
    private final NotificationEventHandler notificationEventHandler;
    private final NotificationEventHandler silentMessageEventHandler;
    private final FlipperFeature[] experimentalFeatures;
    private final boolean automaticPushTokenSending;

    EmarsysConfig(Application application,
                  String mobileEngageApplicationCode,
                  Integer contactFieldId,
                  String predictMerchantId,
                  EventHandler inAppEventHandler,
                  NotificationEventHandler notificationEventHandler,
                  NotificationEventHandler silentMessageEventHandler,
                  FlipperFeature[] experimentalFeatures,
                  boolean automaticPushTokenSending) {
        Assert.notNull(application, "Application must not be null");
        Assert.notNull(contactFieldId, "ContactFieldId must not be null");
        Assert.notNull(experimentalFeatures, "ExperimentalFeatures must not be null");
        Assert.elementsNotNull(experimentalFeatures, "ExperimentalFeatures must not contain null elements!");

        this.application = application;
        this.mobileEngageApplicationCode = mobileEngageApplicationCode;
        this.contactFieldId = contactFieldId;
        this.predictMerchantId = predictMerchantId;
        this.inAppEventHandler = inAppEventHandler;
        this.notificationEventHandler = notificationEventHandler;
        this.silentMessageEventHandler = silentMessageEventHandler;
        this.experimentalFeatures = experimentalFeatures;
        this.automaticPushTokenSending = automaticPushTokenSending;
    }

    public Application getApplication() {
        return application;
    }

    public String getMobileEngageApplicationCode() {
        return mobileEngageApplicationCode;
    }

    public int getContactFieldId() {
        return contactFieldId;
    }

    public String getPredictMerchantId() {
        return predictMerchantId;
    }

    public EventHandler getInAppEventHandler() {
        return inAppEventHandler;
    }

    public NotificationEventHandler getNotificationEventHandler() {
        return notificationEventHandler;
    }

    public NotificationEventHandler getSilentMessageEventHandler() {
        return silentMessageEventHandler;
    }

    public FlipperFeature[] getExperimentalFeatures() {
        return experimentalFeatures;
    }

    public boolean isAutomaticPushTokenSendingEnabled() {
        return automaticPushTokenSending;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmarsysConfig config = (EmarsysConfig) o;
        return contactFieldId == config.contactFieldId &&
                automaticPushTokenSending == config.automaticPushTokenSending &&
                Objects.equals(application, config.application) &&
                Objects.equals(mobileEngageApplicationCode, config.mobileEngageApplicationCode) &&
                Objects.equals(predictMerchantId, config.predictMerchantId) &&
                Objects.equals(inAppEventHandler, config.inAppEventHandler) &&
                Objects.equals(notificationEventHandler, config.notificationEventHandler) &&
                Objects.equals(silentMessageEventHandler, config.silentMessageEventHandler) &&
                Arrays.equals(experimentalFeatures, config.experimentalFeatures);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(application, mobileEngageApplicationCode, contactFieldId, predictMerchantId, inAppEventHandler, notificationEventHandler, silentMessageEventHandler, automaticPushTokenSending);
        result = 31 * result + Arrays.hashCode(experimentalFeatures);
        return result;
    }

    @Override
    public String toString() {
        return "EmarsysConfig{" +
                "application=" + application +
                ", mobileEngageApplicationCode='" + mobileEngageApplicationCode + '\'' +
                ", contactFieldId=" + contactFieldId +
                ", predictMerchantId='" + predictMerchantId + '\'' +
                ", inAppEventHandler=" + inAppEventHandler +
                ", notificationEventHandler=" + notificationEventHandler +
                ", silentMessageEventHandler=" + silentMessageEventHandler +
                ", experimentalFeatures=" + Arrays.toString(experimentalFeatures) +
                ", automaticPushTokenSending=" + automaticPushTokenSending +
                '}';
    }

    public static class Builder {
        private Application application;
        private String mobileEngageApplicationCode;
        private Integer contactFieldId;
        private String predictMerchantId;
        private EventHandler defaultInAppEventHandler;
        private NotificationEventHandler notificationEventHandler;
        private NotificationEventHandler silentMessageEventHandler;
        private FlipperFeature[] experimentalFeatures;
        private boolean automaticPushTokenSending = true;

        public Builder from(EmarsysConfig baseConfig) {
            Assert.notNull(baseConfig, "BaseConfig must not be null");
            application = baseConfig.getApplication();
            mobileEngageApplicationCode = baseConfig.getMobileEngageApplicationCode();
            contactFieldId = baseConfig.getContactFieldId();
            predictMerchantId = baseConfig.getPredictMerchantId();
            defaultInAppEventHandler = baseConfig.getInAppEventHandler();
            notificationEventHandler = baseConfig.getNotificationEventHandler();
            silentMessageEventHandler = baseConfig.getSilentMessageEventHandler();
            experimentalFeatures = baseConfig.getExperimentalFeatures();
            automaticPushTokenSending = baseConfig.isAutomaticPushTokenSendingEnabled();
            return this;
        }

        public Builder application(@NonNull Application application) {
            this.application = application;
            return this;
        }

        public Builder mobileEngageApplicationCode(@Nullable String mobileEngageApplicationCode) {
            this.mobileEngageApplicationCode = mobileEngageApplicationCode;
            return this;
        }

        public Builder contactFieldId(int contactFieldId) {
            this.contactFieldId = contactFieldId;
            return this;
        }

        public Builder predictMerchantId(@Nullable String predictMerchantId) {
            this.predictMerchantId = predictMerchantId;
            return this;
        }

        public Builder enableExperimentalFeatures(@NonNull FlipperFeature... experimentalFeatures) {
            this.experimentalFeatures = experimentalFeatures;
            return this;
        }

        public Builder disableAutomaticPushTokenSending() {
            this.automaticPushTokenSending = false;
            return this;
        }

        public Builder inAppEventHandler(EventHandler inAppEventHandler) {
            this.defaultInAppEventHandler = inAppEventHandler;
            return this;
        }

        public Builder notificationEventHandler(NotificationEventHandler notificationEventHandler) {
            this.notificationEventHandler = notificationEventHandler;
            return this;
        }

        public Builder silentMessageEventHandler(NotificationEventHandler silentMessageEventHandler) {
            this.silentMessageEventHandler = silentMessageEventHandler;
            return this;
        }

        public EmarsysConfig build() {
            experimentalFeatures = experimentalFeatures == null ? new FlipperFeature[]{} : experimentalFeatures;

            return new EmarsysConfig(
                    application,
                    mobileEngageApplicationCode,
                    contactFieldId,
                    predictMerchantId,
                    defaultInAppEventHandler,
                    notificationEventHandler,
                    silentMessageEventHandler,
                    experimentalFeatures,
                    automaticPushTokenSending);
        }
    }
}