package com.emarsys.core;

import com.emarsys.core.api.ResponseErrorException;
import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.Assert;

import java.util.Map;

public class DefaultCoreCompletionHandler implements CoreCompletionHandler, Registry<RequestModel, CompletionListener> {
    private final Map<String, CompletionListener> completionListenerMap;

    public DefaultCoreCompletionHandler(Map<String, CompletionListener> completionListenerMap) {
        Assert.notNull(completionListenerMap, "CompletionListenerMap must not be null!");
        this.completionListenerMap = completionListenerMap;
    }

    public void register(RequestModel model, CompletionListener listener) {
        Assert.notNull(model, "RequestModel must not be null!");
        if (listener != null) {
            this.completionListenerMap.put(model.getId(), listener);
        }
    }

    @Override
    public void onSuccess(final String id, final ResponseModel responseModel) {
        callCompletionListener(id, null);
    }

    @Override
    public void onError(final String id, final Exception cause) {
        callCompletionListener(id, cause);
    }

    @Override
    public void onError(final String id, final ResponseModel responseModel) {
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
