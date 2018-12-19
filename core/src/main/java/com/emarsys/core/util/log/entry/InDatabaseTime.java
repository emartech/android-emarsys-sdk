package com.emarsys.core.util.log.entry;

import com.emarsys.core.request.model.RequestModel;

import java.util.HashMap;
import java.util.Map;

public class InDatabaseTime implements LogEntry {

    private final Map<String, Object> data;

    public InDatabaseTime(RequestModel requestModel, long end) {
        long start = requestModel.getTimestamp();

        data = new HashMap<>();
        data.put("id", requestModel.getId());
        data.put("start", start);
        data.put("end", end);
        data.put("duration", end - start);
        data.put("url", requestModel.getUrl().toString());
    }

    @Override
    public Map<String, Object> getData() {
        return data;
    }

    @Override
    public String getTopic() {
        return "log_in_database_time";
    }
}
