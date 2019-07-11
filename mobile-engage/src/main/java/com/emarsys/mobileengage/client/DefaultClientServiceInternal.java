package com.emarsys.mobileengage.client;

import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.mobileengage.request.RequestModelFactory;

public class DefaultClientServiceInternal implements ClientServiceInternal {
    private final RequestModelFactory requestModelFactory;
    private final RequestManager requestManager;

    public DefaultClientServiceInternal(RequestManager requestManager, RequestModelFactory requestModelFactory) {
        this.requestModelFactory = requestModelFactory;
        this.requestManager = requestManager;
    }

    @Override
    public void trackDeviceInfo() {
        RequestModel requestModel = requestModelFactory.createTrackDeviceInfoRequest();

        requestManager.submit(requestModel, null);
    }
}
