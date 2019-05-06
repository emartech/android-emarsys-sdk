package com.emarsys.mobileengage.inbox;

import com.emarsys.core.request.RequestManager;
import com.emarsys.mobileengage.RequestContext;

public class InboxInternalProvider {

    public InboxInternal provideInboxInternal(
            RequestManager requestManager,
            RequestContext requestContext) {
        return new InboxInternal_V1(requestManager, requestContext);
    }
}
