package com.emarsys.config;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.emarsys.core.api.experimental.FlipperFeature;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.api.EventHandler;
import com.emarsys.mobileengage.api.NotificationEventHandler;

import java.util.Arrays;

public class EmarsysConfig {

    private final Application application;
    private final String mobileEngageApplicationCode;
    private final int contactFieldId;
    private final String predictMerchantId;
    private final EventHandler inAppEventHandler;
    private final NotificationEventHandler notificationEventHandler;
    private final FlipperFeature[] experimentalFeatures;

    EmarsysConfig(Application application,
                  String mobileEngageApplicationCode,
                  Integer contactFieldId,
                  String predictMerchantId,
                  EventHandler inAppEventHandler,
                  NotificationEventHandler notificationEventHandler,
                  FlipperFeature[] experimentalFeatures) {
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
        this.experimentalFeatures = experimentalFeatures;
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

    public FlipperFeature[] getExperimentalFeatures() {
        return experimentalFeatures;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EmarsysConfig config = (EmarsysConfig) o;

        if (contactFieldId != config.contactFieldId) return false;
        if (application != null ? !application.equals(config.application) : config.application != null)
            return false;
        if (mobileEngageApplicationCode != null ? !mobileEngageApplicationCode.equals(config.mobileEngageApplicationCode) : config.mobileEngageApplicationCode != null)
            return false;
        if (predictMerchantId != null ? !predictMerchantId.equals(config.predictMerchantId) : config.predictMerchantId != null)
            return false;
        if (inAppEventHandler != null ? !inAppEventHandler.equals(config.inAppEventHandler) : config.inAppEventHandler != null)
            return false;
        if (notificationEventHandler != null ? !notificationEventHandler.equals(config.notificationEventHandler) : config.notificationEventHandler != null)
            return false;
        return Arrays.equals(experimentalFeatures, config.experimentalFeatures);
    }

    @Override
    public int hashCode() {
        int result = application != null ? application.hashCode() : 0;
        result = 31 * result + (mobileEngageApplicationCode != null ? mobileEngageApplicationCode.hashCode() : 0);
        result = 31 * result + contactFieldId;
        result = 31 * result + (predictMerchantId != null ? predictMerchantId.hashCode() : 0);
        result = 31 * result + (inAppEventHandler != null ? inAppEventHandler.hashCode() : 0);
        result = 31 * result + (notificationEventHandler != null ? notificationEventHandler.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(experimentalFeatures);
        return result;
    }

    @Override
    public String toString() {
        return "EmarsysConfig{" +
                "application=" + application +
                ", applicationCode='" + mobileEngageApplicationCode + '\'' +
                ", contactFieldId=" + contactFieldId +
                ", predictMerchantId='" + predictMerchantId + '\'' +
                ", inAppEventHandler=" + inAppEventHandler +
                ", notificationEventHandler=" + notificationEventHandler +
                ", experimentalFeatures=" + Arrays.toString(experimentalFeatures) +
                '}';
    }

    public static class Builder {
        private Application application;
        private String mobileEngageApplicationCode;
        private Integer contactFieldId;
        private String predictMerchantId;
        private EventHandler defaultInAppEventHandler;
        private NotificationEventHandler notificationEventHandler;
        private FlipperFeature[] experimentalFeatures;

        public Builder from(EmarsysConfig baseConfig) {
            Assert.notNull(baseConfig, "BaseConfig must not be null");
            application = baseConfig.getApplication();
            mobileEngageApplicationCode = baseConfig.getMobileEngageApplicationCode();
            contactFieldId = baseConfig.getContactFieldId();
            predictMerchantId = baseConfig.getPredictMerchantId();
            defaultInAppEventHandler = baseConfig.getInAppEventHandler();
            notificationEventHandler = baseConfig.getNotificationEventHandler();
            experimentalFeatures = baseConfig.getExperimentalFeatures();
            return this;
        }

        public Builder application(@NonNull Application application) {
            this.application = application;
            return this;
        }

        public Builder mobileEngageApplicationCode(@NonNull String mobileEngageApplicationCode) {
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

        public Builder inAppEventHandler(EventHandler inAppEventHandler) {
            this.defaultInAppEventHandler = inAppEventHandler;
            return this;
        }

        public Builder notificationEventHandler(NotificationEventHandler notificationEventHandler) {
            this.notificationEventHandler = notificationEventHandler;
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
                    experimentalFeatures
            );
        }
    }
}