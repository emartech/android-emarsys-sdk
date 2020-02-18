package com.emarsys.config;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.emarsys.core.api.experimental.FlipperFeature;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.api.NotificationEventHandler;
import com.emarsys.mobileengage.api.event.EventHandler;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Objects;

public class EmarsysConfig {

    private final Application application;
    private final String mobileEngageApplicationCode;
    private final int contactFieldId;
    private final String predictMerchantId;
    @Deprecated
    private final EventHandler inAppEventHandler;
    @Deprecated
    private final EventHandler notificationEventHandler;
    private final FlipperFeature[] experimentalFeatures;
    private final boolean automaticPushTokenSending;

    EmarsysConfig(Application application,
                  String mobileEngageApplicationCode,
                  Integer contactFieldId,
                  String predictMerchantId,
                  EventHandler inAppEventHandler,
                  EventHandler notificationEventHandler,
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

    /**
     * @deprecated will be removed in 3.0.0
     */
    @Deprecated
    public com.emarsys.mobileengage.api.EventHandler getInAppEventHandler() {
        if (inAppEventHandler == null) {
            return null;
        }
        return new com.emarsys.mobileengage.api.EventHandler() {
            @Override
            public void handleEvent(String eventName, @Nullable JSONObject payload) {
                inAppEventHandler.handleEvent(null, eventName, payload);
            }
        };
    }

    /**
     * @deprecated will be removed in 3.0.0
     */
    @Deprecated
    public NotificationEventHandler getNotificationEventHandler() {
        if (notificationEventHandler == null) {
            return null;
        }
        return new NotificationEventHandler() {
            @Override
            public void handleEvent(Context context, String eventName, @Nullable JSONObject payload) {
                notificationEventHandler.handleEvent(context, eventName, payload);
            }
        };
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
                Arrays.equals(experimentalFeatures, config.experimentalFeatures);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(application, mobileEngageApplicationCode, contactFieldId, predictMerchantId, inAppEventHandler, notificationEventHandler, automaticPushTokenSending);
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
        private EventHandler notificationEventHandler;
        private FlipperFeature[] experimentalFeatures;
        private boolean automaticPushTokenSending = true;

        public Builder from(final EmarsysConfig baseConfig) {
            Assert.notNull(baseConfig, "BaseConfig must not be null");
            application = baseConfig.getApplication();
            mobileEngageApplicationCode = baseConfig.getMobileEngageApplicationCode();
            contactFieldId = baseConfig.getContactFieldId();
            predictMerchantId = baseConfig.getPredictMerchantId();
            defaultInAppEventHandler = new EventHandler() {
                @Override
                public void handleEvent(@NonNull Context context, @NonNull String eventName, @Nullable JSONObject payload) {
                    baseConfig.getInAppEventHandler().handleEvent(eventName, payload);

                }
            };
            notificationEventHandler = new EventHandler() {
                @Override
                public void handleEvent(@NonNull Context context, @NonNull String eventName, @Nullable JSONObject payload) {
                    baseConfig.getNotificationEventHandler().handleEvent(context, eventName, payload);

                }
            };
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
        /**
         * @deprecated will be removed in 3.0.0, use Emarsys.inapp.setEventHandler(EventHandler) instead.
         */
        @Deprecated
        public Builder inAppEventHandler(final com.emarsys.mobileengage.api.EventHandler inAppEventHandler) {
            this.defaultInAppEventHandler = new EventHandler() {
                @Override
                public void handleEvent(@NonNull Context context, @NonNull String eventName, @Nullable JSONObject payload) {
                    inAppEventHandler.handleEvent(eventName, payload);
                }
            };
            return this;
        }

        /**
         * @deprecated will be removed in 3.0.0, use Emarsys.push.setNotificationEventHandler(EventHandler) instead.
         */
        @Deprecated
        public Builder notificationEventHandler(final NotificationEventHandler notificationEventHandler) {
            this.notificationEventHandler = new EventHandler() {
                @Override
                public void handleEvent(@NonNull Context context, @NonNull String eventName, @Nullable JSONObject payload) {
                    notificationEventHandler.handleEvent(context, eventName, payload);
                }
            };
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
                    experimentalFeatures,
                    automaticPushTokenSending);
        }
    }
}