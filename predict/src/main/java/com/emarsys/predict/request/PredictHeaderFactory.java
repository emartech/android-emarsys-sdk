package com.emarsys.predict.request;

import com.emarsys.core.util.Assert;

import java.util.HashMap;
import java.util.Map;

public class PredictHeaderFactory {
    private final PredictRequestContext requestContext;

    public PredictHeaderFactory(PredictRequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        this.requestContext = requestContext;
    }

    public Map<String, String> createBaseHeader() {
        Map<String, String> result = new HashMap<>();
        result.put("User-Agent", "EmarsysSDK|osversion:" + requestContext.getDeviceInfo().getOsVersion() + "|platform:" + requestContext.getDeviceInfo().getPlatform());
        return result;
    }
}
