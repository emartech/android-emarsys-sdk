package com.emarsys.core.response

abstract class AbstractResponseHandler {

    fun processResponse(responseModel: ResponseModel) {
        if (shouldHandleResponse(responseModel)) {
            handleResponse(responseModel)
        }
    }

    abstract fun shouldHandleResponse(responseModel: ResponseModel): Boolean

    abstract fun handleResponse(responseModel: ResponseModel)

}
