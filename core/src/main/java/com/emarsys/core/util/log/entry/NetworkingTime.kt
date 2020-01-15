package com.emarsys.core.util.log.entry

import com.emarsys.core.response.ResponseModel

class NetworkingTime(responseModel: ResponseModel, start: Long) : LogEntry {
    override val data: Map<String, Any>
    override val topic: String
        get() = "log_networking_time"

    init {
        val end = responseModel.timestamp
        data = mapOf(
                "request_id" to responseModel.requestModel.id,
                "start" to start,
                "end" to end,
                "duration" to end - start,
                "url" to responseModel.requestModel.url.toString(),
                "status_code" to responseModel.statusCode
        )
    }
}