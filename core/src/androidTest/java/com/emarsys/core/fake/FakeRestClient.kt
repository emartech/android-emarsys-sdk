package com.emarsys.core.fake

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.connection.ConnectionProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.request.RestClient
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseHandlersProcessor
import org.mockito.kotlin.mock

class FakeRestClient(vararg fakeResults: Any) : RestClient(
    mock<ConnectionProvider>(),
    mock<TimestampProvider>(),
    mock<ResponseHandlersProcessor>(),
    mock<List<*>>() as List<com.emarsys.core.Mapper<RequestModel, RequestModel>>,
    ConcurrentHandlerHolderFactory.create()
) {
    private val fakeResults: MutableList<Any> = fakeResults.onEach {
        require(it is Int || it is Exception) {
            "FakeResults list can only contain Integers and Exceptions!"
        }
    }.toMutableList()

    override fun execute(model: RequestModel, completionHandler: CoreCompletionHandler) {
        val result = FakeRequestTask(model, fakeResults.removeAt(0)).execute()
            ?: throw IllegalStateException("No more predefined fake responses!")
        when {
            result.errorCause != null && result.result != null ->
                completionHandler.onError(model.id, result.result!!)
            result.errorCause != null ->
                completionHandler.onError(model.id, result.errorCause as Exception)
            else ->
                completionHandler.onSuccess(model.id, result.result!!)
        }
    }
}
