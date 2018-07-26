package com.emarsys.core.testUtil;

import com.emarsys.core.request.RequestIdProvider;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.timestamp.TimestampProvider;

import java.util.HashMap;
import java.util.Map;

public class RequestModelTestUtils {
    public static RequestModel createRequestModel(RequestMethod method) {
        Map<String, String> headers = new HashMap<>();
        TimestampProvider timestampProvider = new TimestampProvider();
        RequestIdProvider requestIdProvider = new RequestIdProvider();
        headers.put("accept", "application/json");
        headers.put("content", "application/x-www-form-urlencoded");
        return new RequestModel.Builder(timestampProvider, requestIdProvider).url("https://www.google.com").method(method).headers(headers).build();
    }
}
