package com.emarsys.mobileengage.util;

import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.util.Assert;

public class RequestModelUtils {

    public static boolean isMobileEngageV3Request(RequestModel requestModel) {
        Assert.notNull(requestModel, "RequestModel must not be null!");

        String url = requestModel.getUrl().toString();

        return RequestUrlUtils.isMobileEngageV3Url(url);
    }

    public static boolean isCustomEvent_V3(RequestModel requestModel) {
        Assert.notNull(requestModel, "RequestModel must not be null");

        String url = requestModel.getUrl().toString();
        return RequestUrlUtils.isCustomEvent_V3(url);
    }
}
