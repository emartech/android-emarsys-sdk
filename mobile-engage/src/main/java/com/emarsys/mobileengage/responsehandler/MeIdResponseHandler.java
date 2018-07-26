package com.emarsys.mobileengage.responsehandler;

import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;

import org.json.JSONException;
import org.json.JSONObject;


public class MeIdResponseHandler extends AbstractResponseHandler {

    private static final String ME_ID_KEY = "api_me_id";
    private final String ME_ID_SIGNATURE_KEY = "me_id_signature";

    MeIdStorage meIdStorage;
    MeIdSignatureStorage meIdSignatureStorage;

    public MeIdResponseHandler(MeIdStorage meIdStorage, MeIdSignatureStorage meIdSignatureStorage) {
        Assert.notNull(meIdStorage, "MeIdStorage must not be null!");
        Assert.notNull(meIdSignatureStorage, "MeIdSignatureStorage must not be null!");
        this.meIdStorage = meIdStorage;
        this.meIdSignatureStorage = meIdSignatureStorage;
    }

    @Override
    protected boolean shouldHandleResponse(ResponseModel responseModel) {
        JSONObject body = responseModel.getParsedBody();
        return body != null && body.has(ME_ID_KEY) && body.has(ME_ID_SIGNATURE_KEY);
    }

    @Override
    protected void handleResponse(ResponseModel responseModel) {
        JSONObject body = responseModel.getParsedBody();
        try {
            meIdStorage.set(body.getString(ME_ID_KEY));
            meIdSignatureStorage.set(body.getString(ME_ID_SIGNATURE_KEY));
        } catch (JSONException ignore) {
        }
    }
}
