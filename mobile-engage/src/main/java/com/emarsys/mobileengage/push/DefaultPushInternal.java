package com.emarsys.mobileengage.push;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.storage.Storage;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.api.event.EventHandler;
import com.emarsys.mobileengage.event.EventHandlerProvider;
import com.emarsys.mobileengage.event.EventServiceInternal;
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class DefaultPushInternal implements PushInternal {
    private final RequestManager requestManager;
    private final Handler uiHandler;
    private final MobileEngageRequestModelFactory requestModelFactory;
    private final EventServiceInternal eventServiceInternal;
    private final Storage<String> pushTokenStorage;
    private final EventHandlerProvider notificationEventHandlerProvider;
    private final EventHandlerProvider silentMessageEventHandlerProvider;

    public DefaultPushInternal(RequestManager requestManager, Handler uiHandler, MobileEngageRequestModelFactory requestModelFactory,
                               EventServiceInternal eventServiceInternal, Storage<String> pushTokenStorage,
                               EventHandlerProvider notificationEventHandlerProvider, EventHandlerProvider silentMessageEventHandlerProvider) {
        Assert.notNull(notificationEventHandlerProvider, "NotificationEventHandlerProvider must not be null!");
        Assert.notNull(silentMessageEventHandlerProvider, "SilentMessageEventHandlerProvider must not be null!");
        this.requestManager = requestManager;
        this.uiHandler = uiHandler;
        this.requestModelFactory = requestModelFactory;
        this.eventServiceInternal = eventServiceInternal;
        this.pushTokenStorage = pushTokenStorage;
        this.notificationEventHandlerProvider = notificationEventHandlerProvider;
        this.silentMessageEventHandlerProvider = silentMessageEventHandlerProvider;
    }

    @Override
    public void setPushToken(String pushToken, CompletionListener completionListener) {
        if (pushToken != null) {
            RequestModel requestModel = requestModelFactory.createSetPushTokenRequest(pushToken);

            pushTokenStorage.set(pushToken);
            requestManager.submit(requestModel, completionListener);
        }
    }

    @Override
    public void clearPushToken(CompletionListener completionListener) {
        RequestModel requestModel = requestModelFactory.createRemovePushTokenRequest();

        pushTokenStorage.remove();
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
                    if (completionListener != null) {
                        completionListener.onCompleted(new IllegalArgumentException("No messageId found!"));
                    }
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

    @Override
    public void setNotificationEventHandler(EventHandler notificationEventHandler) {
        notificationEventHandlerProvider.setEventHandler(notificationEventHandler);
    }

    @Override
    public void setSilentMessageEventHandler(EventHandler silentMessageEventHandler) {
        silentMessageEventHandlerProvider.setEventHandler(silentMessageEventHandler);
    }
}
