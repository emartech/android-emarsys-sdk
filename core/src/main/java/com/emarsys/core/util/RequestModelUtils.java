package com.emarsys.core.util;

import android.net.Uri;

import com.emarsys.core.request.model.CompositeRequestModel;
import com.emarsys.core.request.model.RequestModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RequestModelUtils {

    public static List<String> extractIdsFromCompositeRequestModel(RequestModel requestModel) {
        List<String> ids = new ArrayList<>();
        if (requestModel instanceof CompositeRequestModel) {
            ids.addAll(Arrays.asList(((CompositeRequestModel) requestModel).getOriginalRequestIds()));
        } else {
            ids.add(requestModel.getId());
        }
        return ids;
    }

    public static Map<String, String> extractQueryParameters(RequestModel requestModel) {
        Assert.notNull(requestModel, "RequestModel must not be null!");

        Uri uri = Uri.parse(requestModel.getUrl().toString());

        Map<String, String> parameters = new HashMap<>();

        Set<String> queryParameterNames = uri.getQueryParameterNames();
        if (!queryParameterNames.isEmpty()) {
            for (String name : queryParameterNames) {
                parameters.put(name, uri.getQueryParameter(name));
            }
        }
        return parameters;
    }
}
