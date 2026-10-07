package com.emarsys.core.fake

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.response.ResponseModel
import java.util.concurrent.CountDownLatch

open class FakeCompletionHandler(var latch: CountDownLatch = CountDownLatch(1)) : CoreCompletionHandler {

    var onSuccessCount = 0
    var onErrorCount = 0
    var successId: String? = null
    var errorId: String? = null
    var exception: Exception? = null
    var successResponseModel: ResponseModel? = null
    var failureResponseModel: ResponseModel? = null

    override fun onSuccess(id: String, responseModel: ResponseModel) {
        successResponseModel = responseModel
        onSuccessCount++
        successId = id
        latch.countDown()
    }

    override fun onError(id: String, cause: Exception) {
        exception = cause
        handleError(id)
    }

    override fun onError(id: String, responseModel: ResponseModel) {
        failureResponseModel = responseModel
        handleError(id)
    }

    private fun handleError(id: String) {
        onErrorCount++
        errorId = id
        latch.countDown()
    }
}
