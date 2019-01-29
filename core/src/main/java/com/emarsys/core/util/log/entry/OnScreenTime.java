package com.emarsys.core.util.log.entry;

import java.util.HashMap;
import java.util.Map;

public class OnScreenTime implements LogEntry {

    private final Map<String, Object> data;

    public OnScreenTime(long onScreenTime, String campaignId, String requestId) {
        data = new HashMap<>();
        data.put("campaign_id", campaignId);
        data.put("on_screen_time", onScreenTime);
        if (requestId == null) {
            data.put("source", "push");
        } else {
            data.put("source", "customEvent");
            data.put("request_id", requestId);
        }
    }

    @Override
    public String getTopic() {
        return "log_inapp_on_screen_time";
    }

    @Override
    public Map<String, Object> getData() {
        return data;
    }
}
