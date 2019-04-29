package com.emarsys.mobileengage.request;

import com.emarsys.core.Mapper;
import com.emarsys.core.request.model.CompositeRequestModel;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.util.RequestModelUtils;

import java.util.HashMap;
import java.util.Map;

public class MobileEngageHeaderMapper implements Mapper<RequestModel, RequestModel> {

    private final RequestContext requestContext;

    public MobileEngageHeaderMapper(RequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        this.requestContext = requestContext;
    }

    @Override
    public RequestModel map(RequestModel requestModel) {
        Assert.notNull(requestModel, "RequestModel must not be null!");

        Map<String, String> headersToInject = getHeadersToInject(requestModel);

        RequestModel updatedRequestModel = requestModel;
        if (RequestModelUtils.isMobileEngageV3Request(requestModel)) {

            Map<String, String> updatedHeaders = new HashMap<>(requestModel.getHeaders());
            updatedHeaders.putAll(headersToInject);
            if (updatedRequestModel instanceof CompositeRequestModel) {
                updatedRequestModel = new CompositeRequestModel.Builder(requestModel)
                        .headers(updatedHeaders)
                        .build();
            } else {
                updatedRequestModel = new RequestModel.Builder(requestModel)
                        .headers(updatedHeaders)
                        .build();
            }

        }
        return updatedRequestModel;
    }

    private Map<String, String> getHeadersToInject(RequestModel requestModel) {
        Map<String, String> headersToInject = new HashMap<>();

        String clientState = requestContext.getClientStateStorage().get();
        if (clientState != null) {
            headersToInject.put("X-Client-State", clientState);
        }
        String contactToken = requestContext.getContactTokenStorage().get();
        if (contactToken != null && !RequestModelUtils.isRefreshContactTokenRequest(requestModel)) {
            headersToInject.put("X-Contact-Token", contactToken);
        }
        headersToInject.put("X-Request-Order", String.valueOf(requestContext.getTimestampProvider().provideTimestamp()));
        return headersToInject;
    }
}
