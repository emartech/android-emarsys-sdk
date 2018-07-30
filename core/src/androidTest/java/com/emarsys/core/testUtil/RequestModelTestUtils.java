package com.emarsys.core.testUtil;

import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.provider.timestamp.TimestampProvider;

import java.util.HashMap;
import java.util.Map;

public class RequestModelTestUtils {
    public static RequestModel createRequestModel(RequestMethod method) {
        Map<String, String> headers = new HashMap<>();
        TimestampProvider timestampProvider = new TimestampProvider();
        UUIDProvider UUIDProvider = new UUIDProvider();
        headers.put("accept", "application/json");
        headers.put("content", "application/x-www-form-urlencoded");
        return new RequestModel.Builder(timestampProvider, UUIDProvider).url("https://www.google.com").method(method).headers(headers).build();
    }
}
