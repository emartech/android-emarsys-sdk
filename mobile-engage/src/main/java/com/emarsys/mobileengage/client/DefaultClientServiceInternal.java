package com.emarsys.mobileengage.client;

import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory;

public class DefaultClientServiceInternal implements ClientServiceInternal {
    private final MobileEngageRequestModelFactory requestModelFactory;
    private final RequestManager requestManager;

    public DefaultClientServiceInternal(RequestManager requestManager, MobileEngageRequestModelFactory requestModelFactory) {
        this.requestModelFactory = requestModelFactory;
        this.requestManager = requestManager;
    }

    @Override
    public void trackDeviceInfo() {
        RequestModel requestModel = requestModelFactory.createTrackDeviceInfoRequest();

        requestManager.submit(requestModel, null);
    }
}
