package com.emarsys.core.util.log.entry

class InAppLog(inAppLoadingTime: InAppLoadingTime, onScreenTime: OnScreenTime, campaignId: String, requestId: String?) : LogEntry {
    override val data: Map<String, Any>

    override val topic: String
        get() = "log_inapp_metrics"

    init {
        data = mutableMapOf(
                "loading_time_start" to inAppLoadingTime.startTime,
                "loading_time_end" to inAppLoadingTime.endTime,
                "loading_time_duration" to  inAppLoadingTime.endTime - inAppLoadingTime.startTime,
                "on_screen_time_start" to onScreenTime.startTime,
                "on_screen_time_end" to onScreenTime.endTime,
                "on_screen_time_duration" to  onScreenTime.duration,
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