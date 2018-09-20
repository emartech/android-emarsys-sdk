package com.emarsys.core.response;

import com.emarsys.core.util.Assert;

public abstract class AbstractResponseHandler {

    public void processResponse(ResponseModel responseModel) {
        Assert.notNull(responseModel, "ResponseModel must not be null");
        if (shouldHandleResponse(responseModel)) {
            handleResponse(responseModel);
        }
    }

    protected abstract boolean shouldHandleResponse(ResponseModel responseModel);

    protected abstract void handleResponse(ResponseModel responseModel);

}
