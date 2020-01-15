package com.emarsys.core.util.log.entry

import android.util.Log

class MethodNotAllowed(klass: Class<*>, callerMethodName: String, parameters: Map<String, Any?>?) : LogEntry {
    companion object {
        private const val TAG = "Emarsys SDK"
    }

    override val data: MutableMap<String, Any>
    override val topic: String
        get() = "log_method_not_allowed"

    init {
        data = mutableMapOf(
                "class_name" to klass.simpleName,
                "method_name" to callerMethodName
        )
        if (parameters != null) {
            data["parameters"] = parameters
        }
        Log.i(TAG, String.format("Feature disabled, Class: %s method: %s not allowed. Please check your config.", klass.simpleName, callerMethodName))
    }
}