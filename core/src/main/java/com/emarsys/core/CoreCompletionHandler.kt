package com.emarsys.core

import com.emarsys.core.response.ResponseModel

interface CoreCompletionHandler {
    fun onSuccess(id: String, responseModel: ResponseModel)
    fun onError(id: String, responseModel: ResponseModel)
    fun onError(id: String, cause: Exception)
}