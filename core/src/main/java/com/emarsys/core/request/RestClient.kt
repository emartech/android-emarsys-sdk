package com.emarsys.core.request

import android.os.Handler
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Mapper
import com.emarsys.core.api.result.Try
import com.emarsys.core.connection.ConnectionProvider
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseHandlersProcessor
import com.emarsys.core.response.ResponseModel
import kotlinx.coroutines.*
import kotlinx.coroutines.android.asCoroutineDispatcher

open class RestClient(
    private val connectionProvider: ConnectionProvider,
    private val timestampProvider: TimestampProvider,
    private val responseHandlersProcessor: ResponseHandlersProcessor,
    private val requestModelMappers: List<Mapper<RequestModel, RequestModel>>,
    private val uiHandler: Handler,
    coreSdkHandler: CoreSdkHandler
) {
    private val coreSdkHandlerDispatcher = coreSdkHandler.handler.asCoroutineDispatcher()

    private val sdkScope: CoroutineScope = CoroutineScope(Job() + coreSdkHandlerDispatcher)
    private val defaultScope = CoroutineScope(Job() + Dispatchers.Default)

    open fun execute(model: RequestModel, completionHandler: CoreCompletionHandler) {
        val updatedRequestModel = mapRequestModel(model)
        val task = RequestTask(
            updatedRequestModel,
            connectionProvider,
            timestampProvider
        )

        defaultScope.launch {
            val responseModel = async(context = sdkScope.coroutineContext) {
                task.execute()
            }

            sdkScope.launch {
                onPostExecute(model.id, responseModel.await(), completionHandler)
            }
        }
    }

    private fun onPostExecute(
        requestId: String,
        result: Try<ResponseModel>,
        completionHandler: CoreCompletionHandler
    ) {

        if (result.errorCause != null) {
            completionHandler.onError(requestId, result.errorCause as Exception)
        } else {
            val responseModel = result.result!!
            responseHandlersProcessor.process(result.result)
            if (isStatusCodeOK(responseModel.statusCode)) {
                completionHandler.onSuccess(requestId, responseModel)
            } else {
                completionHandler.onError(requestId, responseModel)
            }
        }
    }

    private fun mapRequestModel(requestModel: RequestModel): RequestModel {
        var updatedRequestModel = requestModel
        for (mapper in requestModelMappers) {
            updatedRequestModel = mapper.map(updatedRequestModel)
        }
        return updatedRequestModel
    }

    private fun isStatusCodeOK(responseCode: Int): Boolean {
        return responseCode in 200..299
    }
}