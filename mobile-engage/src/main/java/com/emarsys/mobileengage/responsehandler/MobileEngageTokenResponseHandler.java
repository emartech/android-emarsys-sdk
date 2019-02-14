package com.emarsys.mobileengage.responsehandler;

import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.AbstractResponseHandler;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.storage.Storage;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.util.RequestUrlUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class MobileEngageTokenResponseHandler extends AbstractResponseHandler {

    private final String tokenKey;
    private final Storage<String> tokenStorage;

    public MobileEngageTokenResponseHandler(String tokenKey, Storage<String> tokenStorage) {
        Assert.notNull(tokenKey, "TokenKey must not be null!");
        Assert.notNull(tokenStorage, "TokenStorage must not be null!");

        this.tokenKey = tokenKey;
        this.tokenStorage = tokenStorage;
    }

    @Override
    protected boolean shouldHandleResponse(ResponseModel responseModel) {
        JSONObject body = responseModel.getParsedBody();
        RequestModel request = responseModel.getRequestModel();

        return isMobileEngage(request) && hasCorrectBody(body);
    }

    @Override
    protected void handleResponse(ResponseModel responseModel) {
        JSONObject body = responseModel.getParsedBody();
        try {
            tokenStorage.set(body.getString(tokenKey));
        } catch (JSONException ignore) {
        }
    }

    private boolean isMobileEngage(RequestModel requestModel) {
        return RequestUrlUtils.isMobileEngageRequest(requestModel);
    }

    private boolean hasCorrectBody(JSONObject body) {
        return body != null && body.has(tokenKey);
    }
}
