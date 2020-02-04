package com.emarsys.core.util.log.entry

class AppEventLog(val eventName: String, eventAttributes: Map<String, String>? = null) : LogEntry {
    override val topic: String
        get() = "log_app_event"
    override val data: MutableMap<String, Any?>

    init {
        data = mutableMapOf(
                "eventName" to eventName
        )
        if (!eventAttributes.isNullOrEmpty()) {
            data["eventAttributes"] = eventAttributes
        }
    }
}