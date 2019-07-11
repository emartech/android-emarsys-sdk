package com.emarsys.core.util;

import com.emarsys.core.request.model.CompositeRequestModel;
import com.emarsys.core.request.model.RequestModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
}
