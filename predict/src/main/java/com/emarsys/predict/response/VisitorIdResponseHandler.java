package com.emarsys.predict.response;

import com.emarsys.core.response.AbstractResponseHandler;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.storage.KeyValueStore;
import com.emarsys.core.util.Assert;
import com.emarsys.predict.PredictInternal;

public class VisitorIdResponseHandler extends AbstractResponseHandler {

    private static final String CDV = "cdv";

    private final KeyValueStore keyValueStore;

    public VisitorIdResponseHandler(KeyValueStore keyValueStore) {
         Assert.notNull(keyValueStore, "KeyValueStore must not be null!");
        this.keyValueStore = keyValueStore;
    }

    @Override
    protected boolean shouldHandleResponse(ResponseModel responseModel) {
        boolean isPredictUrl = responseModel.getRequestModel().getUrl().toString().startsWith(PredictInternal.BASE_URL);
        boolean hasVisitorIdCookie = responseModel.getCookies().get("cdv") != null;
        return isPredictUrl && hasVisitorIdCookie;
    }

    @Override
    protected void handleResponse(ResponseModel responseModel) {
        String visitorId = responseModel.getCookies().get(CDV).getValue();
        keyValueStore.putString(PredictInternal.VISITOR_ID_KEY, visitorId);
    }
}
