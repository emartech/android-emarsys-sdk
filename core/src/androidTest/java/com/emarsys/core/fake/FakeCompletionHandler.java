package com.emarsys.core.fake;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.response.ResponseModel;

import java.util.concurrent.CountDownLatch;

public class FakeCompletionHandler implements CoreCompletionHandler {
    public CountDownLatch latch;

    private int onSuccessCount;
    private int onErrorCount;
    private String successId;
    private String errorId;
    private Exception exception;
    private ResponseModel successResponseModel;
    private ResponseModel failureResponseModel;

    public FakeCompletionHandler() {
        this.latch = new CountDownLatch(1);
    }

    public FakeCompletionHandler(CountDownLatch latch) {
        this.latch = latch;
    }

    public int getOnSuccessCount() {
        return onSuccessCount;
    }

    public int getOnErrorCount() {
        return onErrorCount;
    }

    public Exception getException() {
        return exception;
    }

    public String getSuccessId() {
        return successId;
    }

    public String getErrorId() {
        return errorId;
    }

    public ResponseModel getSuccessResponseModel() {
        return successResponseModel;
    }

    public ResponseModel getFailureResponseModel() {
        return failureResponseModel;
    }

    @Override
    public void onSuccess(String id, ResponseModel responseModel) {
        this.successResponseModel = responseModel;
        onSuccessCount++;
        successId = id;
        latch.countDown();
    }

    @Override
    public void onError(String id, Exception cause) {
        exception = cause;
        handleError(id);
    }

    @Override
    public void onError(String id, ResponseModel responseModel) {
        failureResponseModel = responseModel;
        handleError(id);
    }

    private void handleError(String id) {
        onErrorCount++;
        errorId = id;
        latch.countDown();
    }
}
