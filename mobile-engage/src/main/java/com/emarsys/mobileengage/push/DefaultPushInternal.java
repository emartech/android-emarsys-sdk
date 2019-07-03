package com.emarsys.mobileengage.push;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.event.EventServiceInternal;
import com.emarsys.mobileengage.request.RequestModelFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class DefaultPushInternal implements PushInternal {
    private final RequestManager requestManager;
    private final Handler uiHandler;
    private final RequestModelFactory requestModelFactory;
    private final EventServiceInternal eventServiceInternal;

    public DefaultPushInternal(RequestManager requestManager, Handler uiHandler, RequestModelFactory requestModelFactory, EventServiceInternal eventServiceInternal) {
        this.requestManager = requestManager;
        this.uiHandler = uiHandler;
        this.requestModelFactory = requestModelFactory;
        this.eventServiceInternal = eventServiceInternal;
    }

    @Override
    public void setPushToken(String pushToken, CompletionListener completionListener) {
        if (pushToken != null) {
            RequestModel requestModel = requestModelFactory.createSetPushTokenRequest(pushToken);

            requestManager.submit(requestModel, completionListener);
        }
    }

    @Override
    public void clearPushToken(CompletionListener completionListener) {
        RequestModel requestModel = requestModelFactory.createRemovePushTokenRequest();

        requestManager.submit(requestModel, completionListener);
    }


    @Override
    public void trackMessageOpen(Intent intent, final CompletionListener completionListener) {
        Assert.notNull(intent, "Intent must not be null!");

        String messageId = getMessageId(intent);

        if (messageId != null) {
            handleMessageOpen(completionListener, messageId);
        } else {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    completionListener.onCompleted(new IllegalArgumentException("No messageId found!"));
                }
            });
        }
    }

    String getMessageId(Intent intent) {
        String sid = null;
        Bundle payload = intent.getBundleExtra("payload");
        if (payload != null) {
            String customData = payload.getString("u");
            if (customData != null) {
                try {
                    sid = new JSONObject(customData).getString("sid");
                } catch (JSONException ignore) {
                }
            }
        }
        return sid;
    }

    private void handleMessageOpen(CompletionListener completionListener, String messageId) {
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("sid", messageId);
        attributes.put("origin", "main");
        eventServiceInternal.trackInternalCustomEvent("push:click", attributes, completionListener);
    }

}
