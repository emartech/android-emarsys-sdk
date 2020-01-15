package com.emarsys.core.util.log.entry

interface LogEntry {
    val topic: String
    val data: Map<String, Any?>
}