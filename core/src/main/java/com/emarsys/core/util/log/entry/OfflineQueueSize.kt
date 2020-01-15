package com.emarsys.core.util.log.entry

class OfflineQueueSize(queueSize: Int) : LogEntry {
    override val data: Map<String, Any>
    override val topic: String
        get() = "log_offline_queue_size"

    init {
        data = mapOf(
                "queue_size" to queueSize
        )
    }
}