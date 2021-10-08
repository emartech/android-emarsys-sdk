package com.emarsys.core

import com.emarsys.core.api.ResponseErrorException
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import java.lang.Exception

open class DefaultCoreCompletionHandler(
        private val completionListenerMap: MutableMap<String, CompletionListener>
) : CoreCompletionHandler, Registry<RequestModel, CompletionListener?> {
    override fun register(model: RequestModel, listener: CompletionListener?) {
        if (listener != null) {
            completionListenerMap[model.id] = listener
        }
    }

    override fun onSuccess(id: String, responseModel: ResponseModel) {
        callCompletionListener(id, null)
    }

    override fun onError(id: String, cause: Exception) {
        callCompletionListener(id, cause)
    }

    override fun onError(id: String, responseModel: ResponseModel) {
        val exception: Exception = ResponseErrorException(
                responseModel.statusCode,
                responseModel.message,
                responseModel.body)
        callCompletionListener(id, exception)
    }

    private fun callCompletionListener(id: String, cause: Exception?) {
        val listener = completionListenerMap[id]
        if (listener != null) {
            listener.onCompleted(cause)
            completionListenerMap.remove(id)
        }
    }
}