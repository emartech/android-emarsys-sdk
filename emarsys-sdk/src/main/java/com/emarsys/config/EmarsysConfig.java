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
    private final String applicationCode;
    private final String applicationPassword;
    private final int contactFieldId;
    private final String predictMerchantId;
    private final boolean idlingResourceEnabled;
    private final EventHandler inAppEventHandler;
    private final NotificationEventHandler notificationEventHandler;
    private final FlipperFeature[] experimentalFeatures;

    EmarsysConfig(Application application,
                  String applicationCode,
                  String applicationPassword,
                  Integer contactFieldId,
                  String predictMerchantId,
                  boolean idlingResourceEnabled,
                  EventHandler inAppEventHandler,
                  NotificationEventHandler notificationEventHandler,
                  FlipperFeature[] experimentalFeatures) {
        Assert.notNull(application, "Application must not be null");
        Assert.notNull(applicationCode, "ApplicationCode must not be null");
        Assert.notNull(applicationPassword, "ApplicationPassword must not be null");
        Assert.notNull(contactFieldId, "ContactFieldId must not be null");
        Assert.notNull(experimentalFeatures, "ExperimentalFeatures must not be null");
        Assert.elementsNotNull(experimentalFeatures, "ExperimentalFeatures must not contain null elements!");

        this.application = application;
        this.applicationCode = applicationCode;
        this.applicationPassword = applicationPassword;
        this.contactFieldId = contactFieldId;
        this.predictMerchantId = predictMerchantId;
        this.idlingResourceEnabled = idlingResourceEnabled;
        this.inAppEventHandler = inAppEventHandler;
        this.notificationEventHandler = notificationEventHandler;
        this.experimentalFeatures = experimentalFeatures;
    }

    public Application getApplication() {
        return application;
    }

    public String getApplicationCode() {
        return applicationCode;
    }

    public String getApplicationPassword() {
        return applicationPassword;
    }

    public int getContactFieldId() {
        return contactFieldId;
    }

    public String getPredictMerchantId() {
        return predictMerchantId;
    }

    public boolean isIdlingResourceEnabled() {
        return idlingResourceEnabled;
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
        if (idlingResourceEnabled != config.idlingResourceEnabled) return false;
        if (application != null ? !application.equals(config.application) : config.application != null)
            return false;
        if (applicationCode != null ? !applicationCode.equals(config.applicationCode) : config.applicationCode != null)
            return false;
        if (applicationPassword != null ? !applicationPassword.equals(config.applicationPassword) : config.applicationPassword != null)
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
        result = 31 * result + (applicationCode != null ? applicationCode.hashCode() : 0);
        result = 31 * result + (applicationPassword != null ? applicationPassword.hashCode() : 0);
        result = 31 * result + contactFieldId;
        result = 31 * result + (predictMerchantId != null ? predictMerchantId.hashCode() : 0);
        result = 31 * result + (idlingResourceEnabled ? 1 : 0);
        result = 31 * result + (inAppEventHandler != null ? inAppEventHandler.hashCode() : 0);
        result = 31 * result + (notificationEventHandler != null ? notificationEventHandler.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(experimentalFeatures);
        return result;
    }

    @Override
    public String toString() {
        return "EmarsysConfig{" +
                "application=" + application +
                ", applicationCode='" + applicationCode + '\'' +
                ", applicationPassword='" + applicationPassword + '\'' +
                ", contactFieldId=" + contactFieldId +
                ", predictMerchantId='" + predictMerchantId + '\'' +
                ", idlingResourceEnabled=" + idlingResourceEnabled +
                ", inAppEventHandler=" + inAppEventHandler +
                ", notificationEventHandler=" + notificationEventHandler +
                ", experimentalFeatures=" + Arrays.toString(experimentalFeatures) +
                '}';
    }

    public static class Builder {
        private Application application;
        private String applicationCode;
        private String applicationPassword;
        private Integer contactFieldId;
        private String predictMerchantId;
        private boolean idlingResourceEnabled;
        private EventHandler defaultInAppEventHandler;
        private NotificationEventHandler notificationEventHandler;
        private FlipperFeature[] experimentalFeatures;

        public Builder from(EmarsysConfig baseConfig) {
            Assert.notNull(baseConfig, "BaseConfig must not be null");
            application = baseConfig.getApplication();
            applicationCode = baseConfig.getApplicationCode();
            applicationPassword = baseConfig.getApplicationPassword();
            contactFieldId = baseConfig.getContactFieldId();
            predictMerchantId = baseConfig.getPredictMerchantId();
            idlingResourceEnabled = baseConfig.isIdlingResourceEnabled();
            defaultInAppEventHandler = baseConfig.getInAppEventHandler();
            notificationEventHandler = baseConfig.getNotificationEventHandler();
            experimentalFeatures = baseConfig.getExperimentalFeatures();
            return this;
        }

        public Builder application(@NonNull Application application) {
            this.application = application;
            return this;
        }

        public Builder mobileEngageCredentials(@NonNull String applicationCode,
                                               @NonNull String applicationPassword) {
            this.applicationCode = applicationCode;
            this.applicationPassword = applicationPassword;
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

        public Builder enableIdlingResource(boolean enabled) {
            idlingResourceEnabled = enabled;
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
                    applicationCode,
                    applicationPassword,
                    contactFieldId,
                    predictMerchantId,
                    idlingResourceEnabled,
                    defaultInAppEventHandler,
                    notificationEventHandler,
                    experimentalFeatures
            );
        }
    }
}