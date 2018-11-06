package com.emarsys.mobileengage;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.emarsys.core.DefaultCoreCompletionHandler;
import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.experimental.ExperimentalFeatures;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.TimestampUtils;
import com.emarsys.core.util.log.CoreTopic;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.api.experimental.MobileEngageFeature;
import com.emarsys.mobileengage.event.applogin.AppLoginParameters;
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
    final RequestManager manager;
    final DefaultCoreCompletionHandler coreCompletionHandler;
    final Handler uiHandler;
    final RequestContext requestContext;

    public MobileEngageInternal(
            RequestManager manager,
            Handler uiHandler,
            DefaultCoreCompletionHandler coreCompletionHandler,
            RequestContext requestContext
    ) {
        Assert.notNull(manager, "Manager must not be null!");
        Assert.notNull(requestContext, "RequestContext must not be null!");
        Assert.notNull(coreCompletionHandler, "CoreCompletionHandler must not be null!");
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Arguments: requestContext %s, manager %s, coreCompletionHandler %s", requestContext, manager, coreCompletionHandler);

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


    String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Argument: %s", pushToken);
        this.pushToken = pushToken;
        if (requestContext.getAppLoginParameters() != null) {
            sendAppLogin(null);
        }
    }

    public String appLogin(CompletionListener completionListener) {
        requestContext.setAppLoginParameters(new AppLoginParameters());
        return sendAppLogin(completionListener);
    }

    public String appLogin(String contactFieldValue, CompletionListener completionListener) {
        requestContext.setAppLoginParameters(new AppLoginParameters(requestContext.getContactFieldId(), contactFieldValue));
        return sendAppLogin(completionListener);
    }

    public String appLogin(int contactFieldId, String contactFieldValue) {
        requestContext.setAppLoginParameters(new AppLoginParameters(contactFieldId, contactFieldValue));
        return sendAppLogin(null);
    }

    public String appLogin() {
        requestContext.setAppLoginParameters(new AppLoginParameters());
        return sendAppLogin(null);
    }

    String sendAppLogin(CompletionListener completionListener) {
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

        manager.submit(model, completionListener);
        return model.getId();
    }

    public String appLogout(CompletionListener completionListener) {
        requestContext.setAppLoginParameters(null);

        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Called");

        RequestModel model = new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUUIDProvider())
                .url(ME_LOGOUT_V2)
                .payload(RequestPayloadUtils.createBasePayload(requestContext))
                .headers(RequestHeaderUtils.createBaseHeaders_V2(requestContext))
                .build();

        manager.submit(model, completionListener);
        requestContext.getMeIdStorage().remove();
        requestContext.getAppLoginStorage().remove();
        return model.getId();
    }

    public String trackCustomEvent(String eventName, Map<String, String> eventAttributes, CompletionListener completionListener) {
        if (ExperimentalFeatures.isFeatureEnabled(MobileEngageFeature.IN_APP_MESSAGING)) {
            return trackCustomEvent_V3(eventName, eventAttributes, completionListener);
        } else {
            return trackCustomEvent_V2(eventName, eventAttributes, completionListener);
        }
    }

    private String trackCustomEvent_V2(String eventName, Map<String, String> eventAttributes, CompletionListener completionListener) {
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Arguments: eventName %s, eventAttributes %s", eventName, eventAttributes);

        Map<String, Object> payload = RequestPayloadUtils.createBasePayload(requestContext);
        if (eventAttributes != null && !eventAttributes.isEmpty()) {
            payload.put("attributes", eventAttributes);
        }
        RequestModel model = new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUUIDProvider())
                .url(RequestUrlUtils.createEventUrl_V2(eventName))
                .payload(payload)
                .headers(RequestHeaderUtils.createBaseHeaders_V2(requestContext))
                .build();

        manager.submit(model, completionListener);
        return model.getId();
    }

    private String trackCustomEvent_V3(String eventName, Map<String, String> eventAttributes, CompletionListener completionListener) {
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

        RequestModel model = new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUUIDProvider())
                .url(RequestUrlUtils.createEventUrl_V3(requestContext.getMeIdStorage().get()))
                .payload(payload)
                .headers(RequestHeaderUtils.createBaseHeaders_V3(requestContext))
                .build();

        manager.submit(model, completionListener);
        return model.getId();
    }

    public String trackInternalCustomEvent(
            String eventName,
            Map<String, String> eventAttributes,
            CompletionListener completionListener) {
        Assert.notNull(eventName, "EventName must not be null!");
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Arguments: eventName %s, eventAttributes %s", eventName, eventAttributes);

        if (requestContext.getMeIdStorage().get() != null && requestContext.getMeIdSignatureStorage().get() != null) {
            RequestModel model = RequestModelUtils.createInternalCustomEvent(
                    eventName,
                    eventAttributes,
                    requestContext);

            manager.submit(model, completionListener);
            return model.getId();
        } else {
            return requestContext.getUUIDProvider().provideId();
        }
    }

    public String trackMessageOpen(Intent intent, CompletionListener completionListener) {
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Argument: %s", intent);
        Assert.notNull(intent, "Intent must not be null!");

        String messageId = getMessageId(intent);
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "MessageId %s", messageId);

        return handleMessageOpen(messageId, completionListener);
    }

    String getMessageId(Intent intent) {
        String sid = null;
        Bundle payload = intent.getBundleExtra("payload");
        if (payload != null) {
            String customData = payload.getString("u");
            if (customData != null) {
                try {
                    sid = new JSONObject(customData).getString("sid");
                } catch (JSONException e) {
                }
            }
        }
        return sid;
    }

    private String handleMessageOpen(String messageId, final CompletionListener completionListener) {
        if (messageId != null) {
            if (ExperimentalFeatures.isFeatureEnabled(MobileEngageFeature.TRACK_MESSAGE_OPEN_V3)) {
                return handleMessageOpen_V3(messageId, completionListener);
            } else {
                return handleMessageOpen_V2(messageId, completionListener);
            }
        } else {
            final String uuid = requestContext.getUUIDProvider().provideId();
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                         Throwable cause = new IllegalArgumentException("No messageId found!");
                         EMSLogger.log(CoreTopic.NETWORKING, "Argument: %s", cause);
                         completionListener.onCompleted(cause);
                }
            });
            return uuid;
        }
    }

    private String handleMessageOpen_V2(String messageId, CompletionListener completionListener) {
        Map<String, Object> payload = RequestPayloadUtils.createBasePayload(requestContext);
        payload.put("sid", messageId);
        RequestModel model = new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUUIDProvider())
                .url(RequestUrlUtils.createEventUrl_V2("message_open"))
                .payload(payload)
                .headers(RequestHeaderUtils.createBaseHeaders_V2(requestContext))
                .build();

        manager.submit(model, completionListener);
        return model.getId();
    }

    private String handleMessageOpen_V3(String messageId, CompletionListener completionListener) {
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("sid", messageId);
        return trackInternalCustomEvent("message_open", attributes, completionListener);
    }

    private boolean shouldDoAppLogin(Integer storedHashCode, int currentHashCode, MeIdStorage meIdStorage) {
        boolean result = storedHashCode == null || currentHashCode != storedHashCode;

        if (ExperimentalFeatures.isFeatureEnabled(MobileEngageFeature.IN_APP_MESSAGING)) {
            result = result || meIdStorage.get() == null;
        }

        return result;
    }

}
