package com.emarsys.mobileengage.inbox;

import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.RestClient;
import com.emarsys.mobileengage.RequestContext;

public class InboxInternalProvider {

    public InboxInternal provideInboxInternal(boolean experimental,
                                              RequestManager requestManager,
                                              RestClient restClient,
                                              RequestContext requestContext) {
        InboxInternal result;
        if (experimental) {
            result = new InboxInternal_V2(requestManager, restClient, requestContext);
        } else {
            result = new InboxInternal_V1(requestManager, restClient, requestContext);
        }
        return result;
    }

}
