package com.emarsys.mobileengage;

import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.request.RequestModelFactory;

import java.util.Map;

public class EventServiceInternalV3 implements EventServiceInternal {
    private final RequestModelFactory requestModelFactory;
    private final RequestManager requestManager;

    public EventServiceInternalV3(RequestModelFactory requestModelFactory, RequestManager requestManager) {
        Assert.notNull(requestModelFactory, "RequestModelFactory must not be null!");
        Assert.notNull(requestManager, "RequestManager must not be null!");

        this.requestModelFactory = requestModelFactory;
        this.requestManager = requestManager;
    }

    @Override
    public String trackCustomEvent(String eventName, Map<String, String> eventAttributes, CompletionListener completionListener) {
        Assert.notNull(eventName, "EventName must not be null!");

        RequestModel requestModel = requestModelFactory.createCustomEventRequest(eventName, eventAttributes);
        requestManager.submit(requestModel, completionListener);

        return requestModel.getId();
    }

    @Override
    public String trackInternalCustomEvent(String eventName, Map<String, String> eventAttributes, CompletionListener completionListener) {
        Assert.notNull(eventName, "EventName must not be null!");

        RequestModel requestModel = requestModelFactory.createInternalCustomEventRequest(eventName, eventAttributes);

        requestManager.submit(requestModel, completionListener);

        return requestModel.getId();
    }
}
