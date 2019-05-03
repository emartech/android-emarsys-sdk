package com.emarsys.core.util.log.entry;

import java.util.HashMap;
import java.util.Map;

public class InAppLoadingTime implements LogEntry {

    private final Map<String, Object> data;

    public InAppLoadingTime(long startTime, long endTime, String campaignId, String requestId) {
        data = new HashMap<>();
        data.put("duration", endTime - startTime);
        data.put("start", startTime);
        data.put("end", endTime);
        data.put("campaign_id", campaignId);
        if (requestId == null) {
            data.put("source", "push");
        } else {
            data.put("request_id", requestId);
            data.put("source", "customEvent");
        }
    }

    @Override
    public String getTopic() {
        return "log_inapp_loading_time";
    }

    @Override
    public Map<String, Object> getData() {
        return data;
    }
}
