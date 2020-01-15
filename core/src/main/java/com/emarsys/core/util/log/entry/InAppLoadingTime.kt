package com.emarsys.core.util.log.entry

class InAppLoadingTime(startTime: Long, endTime: Long, campaignId: String, requestId: String?) : LogEntry {
    override val data: Map<String, Any>
    override val topic: String
        get() = "log_inapp_loading_time"

    init {
        data = mutableMapOf(
                "duration" to endTime - startTime,
                "start" to startTime,
                "end" to endTime,
                "campaign_id" to campaignId
        )
        if (requestId == null) {
            data["source"] = "push"
        } else {
            data["request_id"] = requestId
            data["source"] = "customEvent"
        }
    }
}