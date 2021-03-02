package com.emarsys.core.util.log.entry

class MethodNotAllowed(klass: Class<*>, callerMethodName: String, parameters: Map<String, Any?>?) : LogEntry {

    override val data: MutableMap<String, Any>
    override val topic: String
        get() = "log_method_not_allowed"

    init {
        data = mutableMapOf(
                "className" to klass.simpleName,
                "methodName" to callerMethodName
        )
        if (parameters != null) {
            data["parameters"] = parameters
        }
    }
}