package com.emarsys.core

import com.emarsys.core.response.ResponseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ScopeDelegatorCompletionHandler(
    private val completionHandler: CoreCompletionHandler,
    private val scope: CoroutineScope
) : CoreCompletionHandler {
    override fun onSuccess(id: String, responseModel: ResponseModel) {
        scope.launch {
            completionHandler.onSuccess(id, responseModel)
        }
    }

    override fun onError(id: String, responseModel: ResponseModel) {
        scope.launch {
            completionHandler.onError(id, responseModel)
        }
    }

    override fun onError(id: String, cause: Exception) {
        scope.launch {
            completionHandler.onError(id, cause)
        }
    }
}