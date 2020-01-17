package com.emarsys.core.util.log.entry

import com.emarsys.core.response.ResponseModel

class RequestLog(responseModel: ResponseModel, inDatabaseTimeEnd: Long) : LogEntry {
    override val topic: String
        get() = "log_request"
    override val data: Map<String, Any>

    init {
        val requestModel = responseModel.requestModel
        val inDatabaseTimeStart = requestModel.timestamp
        val inDatabaseTimeDuration = inDatabaseTimeEnd - inDatabaseTimeStart
        val networkingTimeEnd = responseModel.timestamp
        val networkingTimeDuration = networkingTimeEnd - inDatabaseTimeEnd
        data = mapOf(
                "request_id" to requestModel.id,
                "url" to requestModel.url,
                "status_code" to responseModel.statusCode,
                "in_db_start" to inDatabaseTimeStart,
                "in_db_end" to inDatabaseTimeEnd,
                "in_db_duration" to inDatabaseTimeDuration,
                "networking_start" to inDatabaseTimeEnd,
                "networking_end" to networkingTimeEnd,
                "networking_duration" to networkingTimeDuration
        )
    }
}