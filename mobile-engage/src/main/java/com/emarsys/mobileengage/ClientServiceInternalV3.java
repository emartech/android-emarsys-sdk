package com.emarsys.mobileengage;

import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.mobileengage.request.RequestModelFactory;

public class ClientServiceInternalV3 implements ClientServiceInternal {
    RequestModelFactory requestModelFactory;
    RequestManager requestManager;

    public ClientServiceInternalV3(RequestManager requestManager, RequestModelFactory requestModelFactory) {
        this.requestModelFactory = requestModelFactory;
        this.requestManager = requestManager;
    }

    @Override
    public void trackDeviceInfo() {
        RequestModel requestModel = requestModelFactory.createTrackDeviceInfoRequest();

        requestManager.submit(requestModel, null);
    }
}
