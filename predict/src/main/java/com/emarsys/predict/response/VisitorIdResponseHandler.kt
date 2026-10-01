package com.emarsys.predict.response;

import com.emarsys.core.endpoint.ServiceEndpointProvider;
import com.emarsys.core.response.AbstractResponseHandler;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.storage.KeyValueStore;
import com.emarsys.core.util.Assert;
import com.emarsys.predict.DefaultPredictInternal;

public class VisitorIdResponseHandler extends AbstractResponseHandler {

    private static final String CDV = "cdv";

    private final KeyValueStore keyValueStore;
    private final ServiceEndpointProvider predictServiceEndpointProvider;

    public VisitorIdResponseHandler(KeyValueStore keyValueStore, ServiceEndpointProvider predictServiceEndpointProvider) {
        Assert.notNull(keyValueStore, "KeyValueStore must not be null!");
        Assert.notNull(predictServiceEndpointProvider, "PredictServiceEndpointProvider must not be null!");

        this.keyValueStore = keyValueStore;
        this.predictServiceEndpointProvider = predictServiceEndpointProvider;
    }

    @Override
    public boolean shouldHandleResponse(ResponseModel responseModel) {
        boolean isPredictUrl = responseModel.getRequestModel().getUrl().toString().startsWith(predictServiceEndpointProvider.provideEndpointHost());
        boolean hasVisitorIdCookie = responseModel.getCookies().get("cdv") != null;
        return isPredictUrl && hasVisitorIdCookie;
    }

    @Override
    public void handleResponse(ResponseModel responseModel) {
        String visitorId = responseModel.getCookies().get(CDV).getValue();
        keyValueStore.putString(DefaultPredictInternal.VISITOR_ID_KEY, visitorId);
    }
}
