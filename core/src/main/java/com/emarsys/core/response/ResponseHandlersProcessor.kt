package com.emarsys.core.response

import com.emarsys.core.Mockable

@Mockable
class ResponseHandlersProcessor(
    responseHandlers: List<AbstractResponseHandler> = emptyList()
) {
    private val responseHandlers: MutableList<AbstractResponseHandler> = responseHandlers.toMutableList()
    fun getResponseHandlers(): List<AbstractResponseHandler> = responseHandlers

    fun process(responseModel: ResponseModel) {
        responseHandlers.forEach { it.processResponse(responseModel) }
    }

    fun addResponseHandlers(responseHandlers: List<AbstractResponseHandler>) {
        this.responseHandlers.addAll(responseHandlers)
    }
}
