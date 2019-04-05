package com.emarsys.mobileengage.responsehandler;

import com.emarsys.core.device.DeviceInfo;
import com.emarsys.core.response.AbstractResponseHandler;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.storage.Storage;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.endpoint.Endpoint;

public class ClientInfoResponseHandler extends AbstractResponseHandler {

    private final DeviceInfo deviceInfo;
    private final Storage<Integer> deviceInfoHashStorage;

    public ClientInfoResponseHandler(DeviceInfo deviceInfo, Storage<Integer> deviceInfoHashStorage) {
        Assert.notNull(deviceInfo, "DeviceInfo must not be null!");
        Assert.notNull(deviceInfoHashStorage, "DeviceInfoHashStorage must not be null!");

        this.deviceInfo = deviceInfo;
        this.deviceInfoHashStorage = deviceInfoHashStorage;
    }

    @Override
    protected boolean shouldHandleResponse(ResponseModel responseModel) {
        String url = responseModel.getRequestModel().getUrl().toString();
        return url.startsWith(Endpoint.ME_V3_CLIENT_HOST) && url.endsWith("/client");
    }

    @Override
    protected void handleResponse(ResponseModel responseModel) {
        deviceInfoHashStorage.set(deviceInfo.getHash());
    }
}
