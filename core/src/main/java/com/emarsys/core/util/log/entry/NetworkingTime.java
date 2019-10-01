package com.emarsys.core.util.log.entry;

import com.emarsys.core.response.ResponseModel;

import java.util.HashMap;
import java.util.Map;

public class NetworkingTime implements LogEntry {

    private final Map<String, Object> data;

    public NetworkingTime(ResponseModel responseModel, long start) {
        long end = responseModel.getTimestamp();

        data = new HashMap<>();
        data.put("request_id", responseModel.getRequestModel().getId());
        data.put("start", start);
        data.put("end", end);
        data.put("duration", end - start);
        data.put("url", responseModel.getRequestModel().getUrl().toString());
        data.put("status_code", responseModel.getStatusCode());
    }

    @Override
    public String getTopic() {
        return "log_networking_time";
    }

    @Override
    public Map<String, Object> getData() {
        return data;
    }
}
