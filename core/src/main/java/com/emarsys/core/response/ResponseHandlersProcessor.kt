package com.emarsys.core.response;

import com.emarsys.core.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class ResponseHandlersProcessor {

    private final List<AbstractResponseHandler> responseHandlers;

    public ResponseHandlersProcessor(List<AbstractResponseHandler> responseHandlers) {
        Assert.notNull(responseHandlers, "ResponseHandlers must not be null!");

        this.responseHandlers = responseHandlers;
    }

    public ResponseHandlersProcessor() {
        this.responseHandlers = new ArrayList<>();
    }

    public List<AbstractResponseHandler> getResponseHandlers() {
        return responseHandlers;
    }

    public void process(ResponseModel responseModel) {
        for (AbstractResponseHandler responseHandler : responseHandlers) {
            responseHandler.processResponse(responseModel);
        }
    }

    public void addResponseHandlers(List<AbstractResponseHandler> responseHandlers) {
        Assert.notNull(responseHandlers, "ResponseHandlers must not be null!");

        this.responseHandlers.addAll(responseHandlers);
    }
}
