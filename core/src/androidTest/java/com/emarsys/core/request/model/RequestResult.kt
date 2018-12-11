package com.emarsys.core.request.model

import com.emarsys.core.fake.FakeCompletionHandler

data class RequestResult(
        val successId: String? = null,
        val successCount: Int = 0,
        val errorId: String? = null,
        val errorCount: Int = 0,
        val failureStatusCode: Int? = null,
        val exceptionClass: Class<out Exception>? = null) {
    companion object {
        fun success(id: String, count: Int = 1) = RequestResult(successId = id, successCount = count)
        fun failure(id: String, failureStatusCode: Int, count: Int = 1) = RequestResult(errorId = id, errorCount = count, failureStatusCode = failureStatusCode)
        fun failure(id: String, exceptionClass: Class<out Exception>, count: Int = 1) = RequestResult(errorId = id, errorCount = count, exceptionClass = exceptionClass)
    }
}

fun FakeCompletionHandler.asRequestResult() = RequestResult(
        successId = this.successId,
        successCount = this.onSuccessCount,
        errorId = this.errorId,
        errorCount = this.onErrorCount,
        failureStatusCode = this.failureResponseModel?.statusCode,
        exceptionClass = this.exception?.javaClass
)