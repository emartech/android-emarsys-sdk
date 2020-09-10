package com.emarsys.core.util.log

enum class LogLevel(val priority: Int) {
    TRACE(1),
    DEBUG(2),
    INFO(3),
    WARN(4),
    ERROR(5),
    METRIC(6)
}