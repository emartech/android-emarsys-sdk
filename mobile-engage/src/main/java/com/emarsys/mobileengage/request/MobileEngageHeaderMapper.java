package com.emarsys.mobileengage.request;

import com.emarsys.core.Mapper;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.storage.Storage;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.util.RequestUrlUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobileEngageHeaderMapper implements Mapper<List<RequestModel>, List<RequestModel>> {

    private final Storage<String> clientStateStorage;
    private final RequestContext requestContext;

    public MobileEngageHeaderMapper(Storage<String> clientStateStorage, RequestContext requestContext) {
        Assert.notNull(clientStateStorage, "ClientStateStorage must not be null!");
        Assert.notNull(requestContext, "RequestContext must not be null!");

        this.clientStateStorage = clientStateStorage;
        this.requestContext = requestContext;
    }

    @Override
    public List<RequestModel> map(List<RequestModel> requestModels) {
        Assert.notNull(requestModels, "RequestModels must not be null!");

        List<RequestModel> result = new ArrayList<>();

        Map<String, String> headersToInject = new HashMap<>();
        headersToInject.put("X-CLIENT-STATE", clientStateStorage.get());

        for (RequestModel requestModel : requestModels) {
            RequestModel updatedRequestModel = requestModel;
            if (RequestUrlUtils.isMobileEngageRequest(requestModel)) {

                Map<String, String> updatedHeaders = new HashMap<>(requestModel.getHeaders());
                updatedHeaders.putAll(headersToInject);

                updatedRequestModel = new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUUIDProvider())
                        .from(requestModel)
                        .headers(updatedHeaders)
                        .build();

            }
            result.add(updatedRequestModel);
        }
        return result;
    }
}
