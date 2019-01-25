package com.emarsys.core.util.log.entry;

import java.util.HashMap;
import java.util.Map;

public class OfflineQueueSize implements LogEntry {
    private final int queueSize;

    public OfflineQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    @Override
    public String getTopic() {
        return "log_offline_queue_size";
    }

    @Override
    public Map<String, Object> getData() {
        Map<String, Object> data = new HashMap<>();
        data.put("queue_size", queueSize);
        return data;
    }
}
