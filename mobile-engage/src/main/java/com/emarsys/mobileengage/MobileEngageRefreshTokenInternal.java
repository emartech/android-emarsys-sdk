package com.emarsys.mobileengage;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.api.ResponseErrorException;
import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.request.RequestModelFactory;
import com.emarsys.mobileengage.responsehandler.MobileEngageTokenResponseHandler;

public class MobileEngageRefreshTokenInternal implements RefreshTokenInternal {
    MobileEngageTokenResponseHandler tokenResponseHandler;
    private RequestManager requestManager;
    private RequestModelFactory requestModelFactory;

    public MobileEngageRefreshTokenInternal(MobileEngageTokenResponseHandler tokenResponseHandler, RequestManager requestManager, RequestModelFactory requestModelFactory) {
        Assert.notNull(tokenResponseHandler, "TokenResponseHandler must not be null!");
        Assert.notNull(requestManager, "RequestManager must not be null!");
        Assert.notNull(requestModelFactory, "RequestModelFactory must not be null!");
        this.tokenResponseHandler = tokenResponseHandler;
        this.requestManager = requestManager;
        this.requestModelFactory = requestModelFactory;
    }

    @Override
    public void refreshContactToken(final CompletionListener completionListener) {
        final RequestModel requestModel = requestModelFactory.createRefreshContactTokenRequest();

        requestManager.submitNow(requestModel, new CoreCompletionHandler() {
            @Override
            public void onSuccess(String id, ResponseModel responseModel) {
                tokenResponseHandler.processResponse(responseModel);
                completionListener.onCompleted(null);
            }

            @Override
            public void onError(String id, ResponseModel responseModel) {
                completionListener.onCompleted(new ResponseErrorException(
                        responseModel.getStatusCode(),
                        responseModel.getMessage(),
                        responseModel.getBody()));
            }

            @Override
            public void onError(String id, Exception cause) {
                completionListener.onCompleted(cause);
            }
        });
    }
}
