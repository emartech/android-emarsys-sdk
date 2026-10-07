package com.emarsys.core.fake

import com.emarsys.core.api.result.Try
import com.emarsys.core.connection.ConnectionProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.request.RequestTask
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import org.mockito.kotlin.mock

class FakeRequestTask(
    private val requestModel: RequestModel,
    private val fakeResult: Any?
) : RequestTask(requestModel, mock<ConnectionProvider>(), mock<TimestampProvider>()) {

    @Suppress("UNCHECKED_CAST")
    override fun execute(): Try<ResponseModel> {
        Thread.sleep(20)
        return when (fakeResult) {
            is Exception -> Try.failure(fakeResult)
            is Int -> {
                val responseModel = ResponseModel.Builder()
                    .statusCode(fakeResult)
                    .message("Fake message")
                    .headers(emptyMap())
                    .requestModel(requestModel)
                    .build()
                if (fakeResult in 200..399) {
                    Try.success(responseModel)
                } else {
                    Try(responseModel, Exception("Error"))
                }
            }
            else -> throw IllegalStateException("Unexpected fake result type")
        }
    }
}
