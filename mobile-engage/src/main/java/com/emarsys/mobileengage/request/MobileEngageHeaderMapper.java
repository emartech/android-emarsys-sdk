package com.emarsys.mobileengage.request;

import com.emarsys.core.Mapper;
import com.emarsys.core.endpoint.ServiceEndpointProvider;
import com.emarsys.core.request.model.CompositeRequestModel;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.MobileEngageRequestContext;
import com.emarsys.mobileengage.util.RequestModelUtils;

import java.util.HashMap;
import java.util.Map;

public class MobileEngageHeaderMapper implements Mapper<RequestModel, RequestModel> {

    private final MobileEngageRequestContext requestContext;
    private final ServiceEndpointProvider clientServiceProvider;
    private final ServiceEndpointProvider eventServiceProvider;

    public MobileEngageHeaderMapper(MobileEngageRequestContext requestContext, ServiceEndpointProvider clientServiceProvider, ServiceEndpointProvider eventServiceProvider) {
        Assert.notNull(requestContext, "RequestContext must not be null!");
        Assert.notNull(clientServiceProvider, "ClientServiceProvider must not be null!");
        Assert.notNull(eventServiceProvider, "EventServiceProvider must not be null!");

        this.requestContext = requestContext;
        this.clientServiceProvider = clientServiceProvider;
        this.eventServiceProvider = eventServiceProvider;
    }

    @Override
    public RequestModel map(RequestModel requestModel) {
        Assert.notNull(requestModel, "RequestModel must not be null!");

        Map<String, String> headersToInject = getHeadersToInject(requestModel);

        RequestModel updatedRequestModel = requestModel;
        if (RequestModelUtils.isMobileEngageV3Request(requestModel, eventServiceProvider, clientServiceProvider)) {

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
        if (contactToken != null && !RequestModelUtils.isRefreshContactTokenRequest(requestModel, clientServiceProvider)) {
            headersToInject.put("X-Contact-Token", contactToken);
        }
        headersToInject.put("X-Request-Order", String.valueOf(requestContext.getTimestampProvider().provideTimestamp()));
        return headersToInject;
    }
}
