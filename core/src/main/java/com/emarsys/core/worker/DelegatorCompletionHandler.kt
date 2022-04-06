package com.emarsys.core.worker

import android.os.Handler
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Mockable
import com.emarsys.core.response.ResponseModel

@Mockable
class DelegatorCompletionHandler(val handler: Handler, val completionHandler: CoreCompletionHandler): CoreCompletionHandler {

    override fun onSuccess(id: String, responseModel: ResponseModel) {
        handler.post {
            completionHandler.onSuccess(id, responseModel)
        }
    }

    override fun onError(id: String, responseModel: ResponseModel) {
        handler.post {
            completionHandler.onError(id, responseModel)
        }
    }

    override fun onError(id: String, cause: Exception) {
        handler.post {
            completionHandler.onError(id, cause)
        }
    }
}