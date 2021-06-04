package com.emarsys.core.util.log.entry

import com.emarsys.core.util.log.LogLevel

interface LogEntry {
    val topic: String
    val data: Map<String, Any?>
}

fun LogEntry.dataWithLogLevel(logLevel: LogLevel, currentThreadName: String) =
    mutableMapOf<String, Any?>(
        "level" to logLevel.name,
        "thread" to currentThreadName
    ).apply { putAll(data) }

fun LogEntry.asString(): String {
    return "topic='$topic', data=$data"
}