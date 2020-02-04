package com.emarsys.core.util.log.entry

class InAppLog(inAppLoadingTime: InAppLoadingTime, onScreenTime: OnScreenTime, campaignId: String, requestId: String?) : LogEntry {
    override val data: Map<String, Any>

    override val topic: String
        get() = "log_inapp_metrics"

    init {
        data = mutableMapOf(
                "loadingTimeStart" to inAppLoadingTime.startTime,
                "loadingTimeEnd" to inAppLoadingTime.endTime,
                "loadingTimeDuration" to  inAppLoadingTime.endTime - inAppLoadingTime.startTime,
                "onScreenTimeStart" to onScreenTime.startTime,
                "onScreenTimeEnd" to onScreenTime.endTime,
                "onScreenTimeDuration" to  onScreenTime.duration,
                "campaignId" to campaignId
        )

        if (requestId == null) {
            data["source"] = "push"
        } else {
            data["requestId"] = requestId
            data["source"] = "customEvent"
        }
    }
}