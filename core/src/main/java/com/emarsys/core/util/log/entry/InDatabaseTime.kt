package com.emarsys.core.util.log.entry

import com.emarsys.core.request.model.RequestModel

class InDatabaseTime(requestModel: RequestModel, end: Long) : LogEntry {
    override val data: Map<String, Any>
    override val topic: String
        get() = "log_in_database_time"

    init {
        val start = requestModel.timestamp
        data = mapOf(
                "request_id" to requestModel.id,
                "start" to start,
                "end" to end,
                "duration" to end - start,
                "url" to requestModel.url.toString()
        )
    }
}