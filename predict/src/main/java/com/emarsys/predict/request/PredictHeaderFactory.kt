package com.emarsys.predict.request;

import com.emarsys.core.util.Assert;
import com.emarsys.predict.DefaultPredictInternal;

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
        String xp = requestContext.getKeyValueStore().getString(DefaultPredictInternal.XP_KEY);
        String visitorId = requestContext.getKeyValueStore().getString(DefaultPredictInternal.VISITOR_ID_KEY);
        StringBuilder cookies = new StringBuilder();

        if (xp != null) {
            cookies.append("xp=").append(xp).append(";");
        }
        if (visitorId != null) {
            cookies.append("cdv=").append(visitorId);
        }
        if (xp != null || visitorId != null) {
            result.put("Cookie", cookies.toString());
        }
        return result;
    }
}
