package com.emarsys.core.util.log.entry

import java.util.*

class CrashLog(val throwable: Throwable?, additionalInformation: String? = null) : LogEntry {
    override val data: Map<String, Any?>
    override val topic: String
        get() = "log_crash"

    init {
        data = if (throwable != null) {
            mapOf(
                    "exception" to throwable.javaClass.name,
                    "reason" to throwable.message,
                    "additionalInformation" to additionalInformation,
                    "stackTrace" to getStackTrace(throwable)
            ).filterValues { it != null }
        } else {
            emptyMap()
        }
    }

    private fun getStackTrace(throwable: Throwable): List<String> {
        val stackTrace = throwable.stackTrace
        val size = stackTrace.size
        val result: MutableList<String> = ArrayList(size)
        for (element in stackTrace) {
            result.add(element.toString())
        }
        return result
    }
}