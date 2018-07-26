package com.emarsys.mobileengage;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.responsehandler.AbstractResponseHandler;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MobileEngageCoreCompletionHandler implements CoreCompletionHandler {

    WeakReference<MobileEngageStatusListener> weakStatusListener;
    List<AbstractResponseHandler> responseHandlers;

    public MobileEngageCoreCompletionHandler(List<AbstractResponseHandler> responseHandlers, MobileEngageStatusListener listener) {
        Assert.notNull(responseHandlers, "ResponseHandlers must not be null!");
        this.responseHandlers = responseHandlers;
        this.weakStatusListener = new WeakReference<>(listener);
    }

    public MobileEngageCoreCompletionHandler(MobileEngageStatusListener listener) {
        this(new ArrayList<AbstractResponseHandler>(), listener);
    }

    MobileEngageStatusListener getStatusListener() {
        return weakStatusListener.get();
    }

    void setStatusListener(MobileEngageStatusListener listener) {
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Argument: %s", listener);
        this.weakStatusListener = new WeakReference<>(listener);
    }

    public void addResponseHandlers(List<AbstractResponseHandler> additionalResponseHandlers) {
        this.responseHandlers.addAll(additionalResponseHandlers);
    }

    @Override
    public void onSuccess(final String id, final ResponseModel responseModel) {
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Argument: %s", responseModel);

        for (AbstractResponseHandler responseHandler : responseHandlers) {
            responseHandler.processResponse(responseModel);
        }

        MobileEngageStatusListener listener = getStatusListener();
        if (listener != null) {
            EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Notifying statusListener");
            listener.onStatusLog(id, responseModel.getMessage());
        }

        MobileEngageUtils.decrementIdlingResource();
    }

    @Override
    public void onError(final String id, final Exception cause) {
        handleOnError(id, cause);
    }

    @Override
    public void onError(final String id, final ResponseModel responseModel) {
        Exception exception = new MobileEngageException(
                responseModel.getStatusCode(),
                responseModel.getMessage(),
                responseModel.getBody());
        handleOnError(id, exception);
    }

    private void handleOnError(String id, Exception cause) {
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Argument: %s", cause);
        MobileEngageStatusListener listener = getStatusListener();
        if (listener != null) {
            EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Notifying statusListener");
            listener.onError(id, cause);
        }
        MobileEngageUtils.decrementIdlingResource();
    }
}
