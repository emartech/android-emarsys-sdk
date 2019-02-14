package com.emarsys.mobileengage.responsehandler;

import com.emarsys.core.response.AbstractResponseHandler;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.storage.Storage;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.util.RequestUrlUtils;

public class MobileEngageClientStateResponseHandler extends AbstractResponseHandler {

    private static final String X_CLIENT_STATE = "X-CLIENT-STATE";

    private final Storage<String> clientStateStorage;

    public MobileEngageClientStateResponseHandler(Storage<String> clientStateStorage) {
        Assert.notNull(clientStateStorage, "ClientStateStorage must not be null!");

        this.clientStateStorage = clientStateStorage;
    }

    @Override
    protected boolean shouldHandleResponse(ResponseModel responseModel) {
        boolean isMobileEngageRequest = RequestUrlUtils.isMobileEngageRequest(responseModel.getRequestModel());
        boolean hasClientState = getClientState(responseModel) != null;

        return isMobileEngageRequest && hasClientState;
    }

    @Override
    protected void handleResponse(ResponseModel responseModel) {
        clientStateStorage.set(getClientState(responseModel));
    }

    private String getClientState(ResponseModel responseModel) {
        return responseModel.getHeaders().get(X_CLIENT_STATE);
    }
}
