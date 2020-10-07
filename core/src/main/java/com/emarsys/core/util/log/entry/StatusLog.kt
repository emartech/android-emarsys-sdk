package com.emarsys.core.util.log.entry

class StatusLog(klass: Class<*>, callerMethodName: String, parameters: Map<String, Any?>?, status: Map<String, Any>? = null) : LogEntry {
    override val data: MutableMap<String, Any?>

    override val topic: String
        get() = "log_status"

    init {
        data = mutableMapOf(
                "className" to klass.simpleName,
                "methodName" to callerMethodName
        )
        if (parameters != null) {
            data["parameters"] = parameters
        }
        if (status != null) {
            data["status"] = status
        }
    }
}