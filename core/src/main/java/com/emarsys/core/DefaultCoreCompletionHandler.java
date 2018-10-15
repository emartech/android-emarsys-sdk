package com.emarsys.core;

import com.emarsys.core.api.ResponseErrorException;
import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.AbstractResponseHandler;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.log.CoreTopic;
import com.emarsys.core.util.log.EMSLogger;

import java.util.List;
import java.util.Map;

public class DefaultCoreCompletionHandler implements CoreCompletionHandler, Registry<RequestModel,CompletionListener> {
    private final Map<String, CompletionListener> completionListenerMap;
    List<AbstractResponseHandler> responseHandlers;

    public DefaultCoreCompletionHandler(List<AbstractResponseHandler> responseHandlers, Map<String, CompletionListener> completionListenerMap) {
        Assert.notNull(responseHandlers, "ResponseHandlers must not be null!");
        Assert.notNull(completionListenerMap, "CompletionListenerMap must not be null!");
        this.responseHandlers = responseHandlers;
        this.completionListenerMap = completionListenerMap;
    }

    public void addResponseHandlers(List<AbstractResponseHandler> additionalResponseHandlers) {
        this.responseHandlers.addAll(additionalResponseHandlers);
    }

    public List<AbstractResponseHandler> getResponseHandlers() {
        return responseHandlers;
    }

    public void register(RequestModel model, CompletionListener listener) {
        Assert.notNull(model, "RequestModel must not be null!");
        if (listener != null) {
            this.completionListenerMap.put(model.getId(), listener);
        }
    }

    @Override
    public void onSuccess(final String id, final ResponseModel responseModel) {
        EMSLogger.log(CoreTopic.NETWORKING, "Argument: %s", responseModel);

        for (AbstractResponseHandler responseHandler : responseHandlers) {
            responseHandler.processResponse(responseModel);
        }

        callCompletionListener(id, null);
    }

    @Override
    public void onError(final String id, final Exception cause) {
        EMSLogger.log(CoreTopic.NETWORKING, "Argument: %s", cause);

        callCompletionListener(id, cause);
    }

    @Override
    public void onError(final String id, final ResponseModel responseModel) {
        EMSLogger.log(CoreTopic.NETWORKING, "Argument: %s", responseModel);

        Exception exception = new ResponseErrorException(
                responseModel.getStatusCode(),
                responseModel.getMessage(),
                responseModel.getBody());

        callCompletionListener(id, exception);
    }

    private void callCompletionListener(String id, Exception cause) {
        final CompletionListener listener = completionListenerMap.get(id);
        if (listener != null) {
            listener.onCompleted(cause);
            completionListenerMap.remove(id);
        }
    }

}
