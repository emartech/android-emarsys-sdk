package com.emarsys.mobileengage;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.TimestampUtils;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.event.applogin.AppLoginParameters;
import com.emarsys.mobileengage.experimental.MobileEngageExperimental;
import com.emarsys.mobileengage.experimental.MobileEngageFeature;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.util.RequestHeaderUtils;
import com.emarsys.mobileengage.util.RequestModelUtils;
import com.emarsys.mobileengage.util.RequestPayloadUtils;
import com.emarsys.mobileengage.util.RequestUrlUtils;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.emarsys.mobileengage.endpoint.Endpoint.ME_LOGOUT_V2;

public class MobileEngageInternal {
    public static final String MOBILEENGAGE_SDK_VERSION = BuildConfig.VERSION_NAME;

    String pushToken;
    final MobileEngageConfig config;
    final RequestManager manager;
    final MobileEngageCoreCompletionHandler coreCompletionHandler;
    final Handler uiHandler;
    final RequestContext requestContext;

    public MobileEngageInternal(
            MobileEngageConfig config,
            RequestManager manager,
            Handler uiHandler,
            MobileEngageCoreCompletionHandler coreCompletionHandler,
            RequestContext requestContext
    ) {
        Assert.notNull(config, "Config must not be null!");
        Assert.notNull(manager, "Manager must not be null!");
        Assert.notNull(requestContext, "RequestContext must not be null!");
        Assert.notNull(coreCompletionHandler, "CoreCompletionHandler must not be null!");
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Arguments: config %s, manager %s, coreCompletionHandler %s", config, manager, coreCompletionHandler);

        this.config = config;
        this.manager = manager;
        this.requestContext = requestContext;
        this.uiHandler = uiHandler;
        this.coreCompletionHandler = coreCompletionHandler;
        try {
            this.pushToken = FirebaseInstanceId.getInstance().getToken();
        } catch (Exception ignore) {
        }
    }

    RequestManager getManager() {
        return manager;
    }

    public RequestContext getRequestContext() {
        return requestContext;
    }

    @Nullable
    String getPushToken() {
        return pushToken;
    }

    void setPushToken(@NonNull String pushToken) {
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Argument: %s", pushToken);
        this.pushToken = pushToken;
        if (requestContext.getAppLoginParameters() != null) {
            sendAppLogin();
        }
    }

    @NonNull
    public String appLogin(int contactFieldId, @NonNull String contactFieldValue) {
        requestContext.setAppLoginParameters(new AppLoginParameters(contactFieldId, contactFieldValue));
        return sendAppLogin();
    }

    @NonNull
    public String appLogin() {
        requestContext.setAppLoginParameters(new AppLoginParameters());
        return sendAppLogin();
    }

    @NonNull
    String sendAppLogin() {
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Called");

        RequestModel model = RequestModelUtils.createAppLogin_V2(
                requestContext,
                pushToken);

        Integer storedHashCode = requestContext.getAppLoginStorage().get();
        int currentHashCode = model.getPayload().hashCode();

        if (!shouldDoAppLogin(storedHashCode, currentHashCode, requestContext.getMeIdStorage())) {
            model = RequestModelUtils.createLastMobileActivity(requestContext);
        } else {
            requestContext.getAppLoginStorage().set(currentHashCode);
        }

        MobileEngageUtils.incrementIdlingResource();
        manager.submit(model);
        return model.getId();
    }

    @NonNull
    public String appLogout() {
        requestContext.setAppLoginParameters(null);

        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Called");

        RequestModel model = new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getRequestIdProvider())
                .url(ME_LOGOUT_V2)
                .payload(RequestPayloadUtils.createBasePayload(requestContext))
                .headers(RequestHeaderUtils.createBaseHeaders_V2(config))
                .build();

        MobileEngageUtils.incrementIdlingResource();
        manager.submit(model);
        requestContext.getMeIdStorage().remove();
        requestContext.getAppLoginStorage().remove();
        return model.getId();
    }

    @NonNull
    public String trackCustomEvent(@NonNull String eventName, @Nullable Map<String, String> eventAttributes) {
        if (MobileEngageExperimental.isFeatureEnabled(MobileEngageFeature.IN_APP_MESSAGING)) {
            return trackCustomEvent_V3(eventName, eventAttributes);
        } else {
            return trackCustomEvent_V2(eventName, eventAttributes);
        }
    }

    @NonNull
    String trackCustomEvent_V2(@NonNull String eventName, @Nullable Map<String, String> eventAttributes) {
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Arguments: eventName %s, eventAttributes %s", eventName, eventAttributes);

        Map<String, Object> payload = RequestPayloadUtils.createBasePayload(requestContext);
        if (eventAttributes != null && !eventAttributes.isEmpty()) {
            payload.put("attributes", eventAttributes);
        }
        RequestModel model = new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getRequestIdProvider())
                .url(RequestUrlUtils.createEventUrl_V2(eventName))
                .payload(payload)
                .headers(RequestHeaderUtils.createBaseHeaders_V2(config))
                .build();

        MobileEngageUtils.incrementIdlingResource();
        manager.submit(model);
        return model.getId();
    }

    @NonNull
    String trackCustomEvent_V3(@NonNull String eventName, @Nullable Map<String, String> eventAttributes) {
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Arguments: eventName %s, eventAttributes %s", eventName, eventAttributes);

        Map<String, Object> event = new HashMap<>();
        event.put("type", "custom");
        event.put("name", eventName);
        event.put("timestamp", TimestampUtils.formatTimestampWithUTC(requestContext.getTimestampProvider().provideTimestamp()));
        if (eventAttributes != null && !eventAttributes.isEmpty()) {
            event.put("attributes", eventAttributes);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("clicks", new ArrayList<>());
        payload.put("viewed_messages", new ArrayList<>());
        payload.put("events", Collections.singletonList(event));

        RequestModel model = new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getRequestIdProvider())
                .url(RequestUrlUtils.createEventUrl_V3(requestContext.getMeIdStorage().get()))
                .payload(payload)
                .headers(RequestHeaderUtils.createBaseHeaders_V3(requestContext))
                .build();

        MobileEngageUtils.incrementIdlingResource();
        manager.submit(model);
        return model.getId();
    }

    @NonNull
    public String trackInternalCustomEvent(@NonNull String eventName, @Nullable Map<String, String> eventAttributes) {
        Assert.notNull(eventName, "EventName must not be null!");
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Arguments: eventName %s, eventAttributes %s", eventName, eventAttributes);

        if (requestContext.getMeIdStorage().get() != null && requestContext.getMeIdSignatureStorage().get() != null) {
            RequestModel model = RequestModelUtils.createInternalCustomEvent(
                    eventName,
                    eventAttributes,
                    requestContext);

            MobileEngageUtils.incrementIdlingResource();
            manager.submit(model);
            return model.getId();
        } else {
            return requestContext.getRequestIdProvider().provideId();
        }
    }

    @NonNull
    public String trackMessageOpen(Intent intent) {
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Argument: %s", intent);

        String messageId = getMessageId(intent);
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "MessageId %s", messageId);

        return handleMessageOpen(messageId);
    }

    String getMessageId(Intent intent) {
        String sid = null;
        Bundle payload = intent.getBundleExtra("payload");
        if (payload != null) {
            String customData = payload.getString("u");
            try {
                sid = new JSONObject(customData).getString("sid");
            } catch (JSONException e) {
            }
        }
        return sid;
    }

    @NonNull
    private String handleMessageOpen(String messageId) {
        if (messageId != null) {
            if (MobileEngageExperimental.isFeatureEnabled(MobileEngageFeature.IN_APP_MESSAGING)) {
                return handleMessageOpen_V3(messageId);
            } else {
                return handleMessageOpen_V2(messageId);
            }
        } else {
            final String uuid = requestContext.getRequestIdProvider().provideId();
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    coreCompletionHandler.onError(uuid, new IllegalArgumentException("No messageId found!"));
                }
            });
            return uuid;
        }
    }

    private String handleMessageOpen_V2(String messageId) {
        Map<String, Object> payload = RequestPayloadUtils.createBasePayload(requestContext);
        payload.put("sid", messageId);
        RequestModel model = new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getRequestIdProvider())
                .url(RequestUrlUtils.createEventUrl_V2("message_open"))
                .payload(payload)
                .headers(RequestHeaderUtils.createBaseHeaders_V2(config))
                .build();

        MobileEngageUtils.incrementIdlingResource();
        manager.submit(model);
        return model.getId();
    }

    private String handleMessageOpen_V3(String messageId) {
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("sid", messageId);
        return trackInternalCustomEvent("message_open", attributes);
    }

    private boolean shouldDoAppLogin(Integer storedHashCode, int currentHashCode, MeIdStorage meIdStorage) {
        boolean result = storedHashCode == null || currentHashCode != storedHashCode;

        if (MobileEngageExperimental.isFeatureEnabled(MobileEngageFeature.IN_APP_MESSAGING)) {
            result = result || meIdStorage.get() == null;
        }

        return result;
    }

}
