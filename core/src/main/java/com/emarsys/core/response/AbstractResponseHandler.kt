package com.emarsys.core.response

import com.emarsys.core.util.Assert

abstract class AbstractResponseHandler {

    fun processResponse(responseModel: ResponseModel) {
        Assert.notNull(responseModel, "ResponseModel must not be null")
        if (shouldHandleResponse(responseModel)) {
            handleResponse(responseModel)
        }
    }

    abstract fun shouldHandleResponse(responseModel: ResponseModel): Boolean

    abstract fun handleResponse(responseModel: ResponseModel)

}
