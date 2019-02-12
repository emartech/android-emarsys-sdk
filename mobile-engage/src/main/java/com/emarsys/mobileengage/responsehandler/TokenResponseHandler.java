package com.emarsys.mobileengage.responsehandler;

import com.emarsys.core.response.AbstractResponseHandler;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.storage.ContactTokenStorage;
import com.emarsys.mobileengage.storage.RefreshTokenStorage;

import org.json.JSONException;
import org.json.JSONObject;


public class TokenResponseHandler extends AbstractResponseHandler {

    private static final String REFRESH_TOKEN_KEY = "refreshToken";
    private static final String CONTACT_TOKEN_KEY = "contactToken";

    private final RefreshTokenStorage refreshTokenStorage;
    private final ContactTokenStorage contactTokenStorage;

    public TokenResponseHandler(RefreshTokenStorage refreshTokenStorage, ContactTokenStorage contactTokenStorage) {
        Assert.notNull(refreshTokenStorage, "RefreshTokenStorage must not be null!");
        Assert.notNull(contactTokenStorage, "ContactTokenStorage must not be null!");

        this.refreshTokenStorage = refreshTokenStorage;
        this.contactTokenStorage = contactTokenStorage;
    }

    @Override
    protected boolean shouldHandleResponse(ResponseModel responseModel) {
        JSONObject body = responseModel.getParsedBody();
        return body != null && body.has(CONTACT_TOKEN_KEY) && body.has(REFRESH_TOKEN_KEY);
    }

    @Override
    protected void handleResponse(ResponseModel responseModel) {
        JSONObject body = responseModel.getParsedBody();
        try {
            refreshTokenStorage.set(body.getString(REFRESH_TOKEN_KEY));
            contactTokenStorage.set(body.getString(CONTACT_TOKEN_KEY));
        } catch (JSONException ignore) {
        }
    }
}
