package com.emarsys.core.util.log.entry

import com.emarsys.core.util.log.LogLevel

interface LogEntry {
    val topic: String
    val data: Map<String, Any?>
}

fun LogEntry.toData(logLevel: LogLevel, currentThreadName: String, wrapperInfo: String?) =
        mutableMapOf<String, Any?>(
                "level" to logLevel.name,
                "thread" to currentThreadName
        ).apply {
            if (wrapperInfo != null) {
                put("wrapper", wrapperInfo)
            }
            putAll(data)
        }

fun LogEntry.asString(): String {
    return "topic='$topic', data=$data"
}