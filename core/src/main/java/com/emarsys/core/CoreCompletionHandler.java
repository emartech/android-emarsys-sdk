package com.emarsys.core;

import com.emarsys.core.response.ResponseModel;

public interface CoreCompletionHandler {
    void onSuccess(String id, ResponseModel responseModel);

    void onError(String id, ResponseModel responseModel);

    void onError(String id, Exception cause);
}
