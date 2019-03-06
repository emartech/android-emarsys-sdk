package com.emarsys.mobileengage;

import android.content.Intent;

import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.util.RequestModelUtils;

import java.util.Map;

public class MobileEngageInternalV3 implements MobileEngageInternal {

    private final RequestManager requestManager;
    private final RequestContext requestContext;

    public MobileEngageInternalV3(RequestManager requestManager, RequestContext requestContext) {
        Assert.notNull(requestManager, "RequestManager must not be null!");
        Assert.notNull(requestContext, "RequestContext must not be null!");

        this.requestManager = requestManager;
        this.requestContext = requestContext;
    }

    @Override
    public void setPushToken(String pushToken, CompletionListener completionListener) {
        if (pushToken != null) {
            RequestModel requestModel = RequestModelUtils.createSetPushTokenRequest(pushToken, requestContext);

            requestManager.submit(requestModel, completionListener);
        }
    }

    @Override
    public void removePushToken(CompletionListener completionListener) {

    }

    @Override
    public String setAnonymousContact(CompletionListener completionListener) {
        return null;
    }

    @Override
    public void setContact(String contactFieldValue, CompletionListener completionListener) {
        Assert.notNull(contactFieldValue, "ContactFieldValue must not be null!");

        RequestModel requestModel = RequestModelUtils.createSetContactRequest(contactFieldValue, requestContext);

        requestManager.submit(requestModel, completionListener);
    }

    @Override
    public String removeContact(CompletionListener completionListener) {
        return null;
    }

    @Override
    public String trackCustomEvent(String eventName, Map<String, String> eventAttributes, CompletionListener completionListener) {
        Assert.notNull(eventName, "EventName must not be null!");

        RequestModel requestModel = RequestModelUtils.createCustomEventRequest(eventName, eventAttributes, requestContext);
        requestManager.submit(requestModel, completionListener);

        return requestModel.getId();
    }

    @Override
    public String trackInternalCustomEvent(String eventName, Map<String, String> eventAttributes, CompletionListener completionListener) {
        Assert.notNull(eventName, "EventName must not be null!");

        RequestModel requestModel = RequestModelUtils.createInternalCustomEventRequest(eventName, eventAttributes, requestContext);
        requestManager.submit(requestModel, completionListener);

        return requestModel.getId();
    }

    @Override
    public String trackMessageOpen(Intent intent, CompletionListener completionListener) {
        return null;
    }

    @Override
    public void trackDeviceInfo() {
        RequestModel requestModel = RequestModelUtils.createTrackDeviceInfoRequest(requestContext);

        requestManager.submit(requestModel, null);
    }
}
