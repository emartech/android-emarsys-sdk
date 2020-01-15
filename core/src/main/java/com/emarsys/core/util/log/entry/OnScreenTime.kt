package com.emarsys.core.util.log.entry


class OnScreenTime(onScreenTime: Long, startScreenTime: Long, endScreenTime: Long, campaignId: String, requestId: String?) : LogEntry {
    override val data: Map<String, Any>
    override val topic: String
        get() = "log_inapp_on_screen_time"

    init {
        data = mutableMapOf(
                "campaign_id" to campaignId,
                "duration" to onScreenTime,
                "start" to startScreenTime,
                "end" to endScreenTime
        )
        if (requestId == null) {
            data["source"] = "push"
        } else {
            data["source"] = "customEvent"
            data["request_id"] = requestId
        }
    }
}