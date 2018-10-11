package com.emarsys.core;

import com.emarsys.core.api.ResponseErrorException;
import com.emarsys.core.response.AbstractResponseHandler;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.log.CoreTopic;
import com.emarsys.core.util.log.EMSLogger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class DefaultCompletionHandler implements CoreCompletionHandler {

    WeakReference<StatusListener> weakStatusListener;
    List<AbstractResponseHandler> responseHandlers;

    public DefaultCompletionHandler(List<AbstractResponseHandler> responseHandlers, StatusListener listener) {
        Assert.notNull(responseHandlers, "ResponseHandlers must not be null!");
        this.responseHandlers = responseHandlers;
        this.weakStatusListener = new WeakReference<>(listener);
    }

    public DefaultCompletionHandler(StatusListener listener) {
        this(new ArrayList<AbstractResponseHandler>(), listener);
    }

    StatusListener getStatusListener() {
        return weakStatusListener.get();
    }

    void setStatusListener(StatusListener listener) {
        EMSLogger.log(CoreTopic.NETWORKING, "Argument: %s", listener);
        this.weakStatusListener = new WeakReference<>(listener);
    }

    public void addResponseHandlers(List<AbstractResponseHandler> additionalResponseHandlers) {
        this.responseHandlers.addAll(additionalResponseHandlers);
    }

    public List<AbstractResponseHandler> getResponseHandlers() {
        return responseHandlers;
    }

    @Override
    public void onSuccess(final String id, final ResponseModel responseModel) {
        EMSLogger.log(CoreTopic.NETWORKING, "Argument: %s", responseModel);

        for (AbstractResponseHandler responseHandler : responseHandlers) {
            responseHandler.processResponse(responseModel);
        }

        StatusListener listener = getStatusListener();
        if (listener != null) {
            EMSLogger.log(CoreTopic.NETWORKING, "Notifying statusListener");
            listener.onStatusLog(id, responseModel.getMessage());
        }

    }

    @Override
    public void onError(final String id, final Exception cause) {
        handleOnError(id, cause);
    }

    @Override
    public void onError(final String id, final ResponseModel responseModel) {
        Exception exception = new ResponseErrorException(
                responseModel.getStatusCode(),
                responseModel.getMessage(),
                responseModel.getBody());
        handleOnError(id, exception);
    }

    private void handleOnError(String id, Exception cause) {
        EMSLogger.log(CoreTopic.NETWORKING, "Argument: %s", cause);
        StatusListener listener = getStatusListener();
        if (listener != null) {
            EMSLogger.log(CoreTopic.NETWORKING, "Notifying statusListener");
            listener.onError(id, cause);
        }
    }
}
