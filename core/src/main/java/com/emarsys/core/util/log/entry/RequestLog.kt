package com.emarsys.core.util.log.entry

import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel


class RequestLog(responseModel: ResponseModel, inDatabaseTimeEnd: Long, updatedRequestModel: RequestModel? = null) : LogEntry {
    override val topic: String
        get() = "log_request"
    override val data: Map<String, Any>

    init {
        val requestModel = responseModel.requestModel
        val inDatabaseTimeStart = requestModel.timestamp
        val inDatabaseTimeDuration = inDatabaseTimeEnd - inDatabaseTimeStart
        val networkingTimeEnd = responseModel.timestamp
        val networkingTimeDuration = networkingTimeEnd - inDatabaseTimeEnd
        data = mutableMapOf(
                "requestId" to requestModel.id,
                "url" to requestModel.url,
                "statusCode" to responseModel.statusCode,
                "inDbStart" to inDatabaseTimeStart,
                "inDbEnd" to inDatabaseTimeEnd,
                "inDbDuration" to inDatabaseTimeDuration,
                "networkingStart" to inDatabaseTimeEnd,
                "networkingEnd" to networkingTimeEnd,
                "networkingDuration" to networkingTimeDuration
        )
        if (updatedRequestModel != null) {
            data["header"] = updatedRequestModel.headers.toString()
            data["payload"] = updatedRequestModel.payload.toString()
        }
    }
}